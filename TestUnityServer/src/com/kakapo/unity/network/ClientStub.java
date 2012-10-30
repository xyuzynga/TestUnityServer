package com.kakapo.unity.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kakapo.unity.client.Client;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.client.ClientMessage;
import com.kakapo.unity.message.client.KeepAliveMessage;
import com.kakapo.unity.message.client.PeerMessage;
import com.kakapo.unity.message.client.RegisterMessage;
import com.kakapo.unity.network.codec.MessageCodec;
import com.kakapo.unity.server.Server;

public class ClientStub
  implements Client
{
  private final Server server;
  private BufferCharSequence _input = new BufferCharSequence();
  private AppendableBuffers output;
  private String group;
  private String extension;
  private MessageCodec codec;
  private final Listener listener;
  private final SocketChannel channel;
  private boolean disconnect;
  private boolean registered;
  public static final AtomicInteger sent;
  public static final AtomicInteger received;
  private static final Logger logger;

  public ClientStub(SocketChannel channel, Server server, MessageCodec codec, Listener listener)
  {
    this.channel = channel;
    this.codec = codec;
    this.listener = listener;
    this.server = server;
    this.output = new AppendableBuffers(listener);
  }

  public void read(ByteBuffer buffer)
    throws IOException
  {
    this._input.addBuffer(buffer);

    Message message = null;
    while ((message = readMessage()) != null)
    {
      processMessage(message);
    }
  }

  private void processMessage(Message message)
  {
    received.incrementAndGet();
    try
    {
      if (!(message instanceof KeepAliveMessage))
      {
        if ((message instanceof RegisterMessage))
        {
          RegisterMessage rm = (RegisterMessage)message;
          this.group = rm.getGroup().toString();
          this.extension = rm.getExtension().toString();
          this.server.register(this, rm.isOverride());
          this.registered = true;
        }
        else if ((message instanceof ClientMessage))
        {
          if (!this.registered)
          {
            throw new IllegalStateException("Client is not registered " + this);
          }
          this.server.send(this, (ClientMessage)message);
        }
        else
        {
          throw new IllegalArgumentException("Unknown message" + message);
        }
      }
    }
    catch (Throwable e) {
      logger.log(Level.WARNING, "Problem processing message " + message, e);
    }
  }

  private Message readMessage()
  {
    if (this._input.length() > 0)
    {
      MessageCodec.DecodeResult result = this.codec.decode(this._input);

      if (result != null)
      {
        Message message = result.message;

        this._input = ((BufferCharSequence)this._input.subSequence(result.chars, this._input.length()));
        return message;
      }
    }

    return null;
  }

  public boolean isDisconnect()
  {
    return this.disconnect;
  }

  public String getGroup()
  {
    return this.group;
  }

  public String getExtension()
  {
    return this.extension;
  }

  public void receive(Message message)
  {
    try
    {
      if ((message instanceof PeerMessage))
      {
        CharSequence input = ((PeerMessage)message).getInput();

        if (input != null)
        {
          assert ("Command: Message".contentEquals(input.subSequence(0, "Command: Message".length())));

          this.output.append(input);
        }
        else
        {
          this.codec.encode(message, this.output);
        }
      }
      else
      {
        this.codec.encode(message, this.output);
      }

      sent.incrementAndGet();
      if (sent.get() % 10000 == 0)
      {
        System.out.println("Sent: " + sent.get());
        System.out.println("Received: " + received.get());
      }

    }
    catch (Exception e)
    {
      this.output.clear();

      throw new RuntimeException(e);
    }

    try
    {
      this.channel.register(this.listener.getSelector(), 4, this);
    }
    catch (ClosedChannelException e)
    {
      logger.info("Channel was closed so message not sent");
    }

    if (message.isDisconnect())
    {
      this.disconnect = true;
    }
  }

  public boolean write(SocketChannel channel)
    throws IOException
  {
    if (logger.isLoggable(Level.FINE))
    {
      String writing = this.output.toString();
      logger.fine(writing);
    }

    ByteBuffer[] buffers = this.output.getBuffersForReading();

    channel.write(buffers);

    boolean finished = this.output.isComplete();

    if (finished)
    {
      this.output.clear();
    }

    return finished;
  }

  public void disconnect()
  {
    this.server.unregister(this);
    this.registered = false;
  }

  public String toString()
  {
    return getClass().getSimpleName() + " Group: " + this.group + " Extn: " + this.extension;
  }

  static
  {
    sent = new AtomicInteger();
    received = new AtomicInteger();

    logger = Logger.getLogger(ClientStub.class.getName());
  }
}