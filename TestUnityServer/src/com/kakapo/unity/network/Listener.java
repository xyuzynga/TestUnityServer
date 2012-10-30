package com.kakapo.unity.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kakapo.unity.client.Client;
import com.kakapo.unity.network.codec.MessageCodec;
import com.kakapo.unity.network.codec.SimpleMessageCodec;
import com.kakapo.unity.server.MemoryServer;

public class Listener extends Thread
  implements ByteBufferFactory
{
  private boolean _running = true;
  private Selector selector;
  private ServerSocketChannel channel;
  private final MemoryServer server;
  private final MessageCodec _codec = new SimpleMessageCodec();
  private final int bufferSize;
  private static final Logger logger = Logger.getLogger(Listener.class.getName());
  private final int period;

  public Listener(InetSocketAddress address, int bufferSize, int period, MemoryServer server)
  {
    this.bufferSize = bufferSize;
    this.period = period;
    this.server = server;
    try
    {
      this.selector = Selector.open();

      this.channel = ServerSocketChannel.open();
      this.channel.configureBlocking(false);
      this.channel.socket().bind(address);
      this.channel.register(this.selector, 16);
    }
    catch (IOException e)
    {
      throw new RuntimeException("Could not register listener", e);
    }
  }

  public void run()
  {
    while (this._running)
    {
      try
      {
        this.selector.select(this.period);
      }
      catch (IOException e)
      {
        logger.log(Level.SEVERE, "Problem during select", e);
      }

      Set<SelectionKey> keys = this.selector.selectedKeys();
      Iterator<SelectionKey> iterator = keys.iterator();
      while (iterator.hasNext())
      {
        SelectionKey key = iterator.next();
        iterator.remove();
        try
        {
          processSelection(key);
        }
        catch (Throwable e)
        {
          logger.log(Level.WARNING, "Problem during key processing", e);
          if (key != null)
          {
            key.cancel();
          }
        }

      }

      try
      {
        this.server.processScheduledStatuses();
      }
      catch (Throwable t)
      {
        logger.log(Level.SEVERE, "Problem while processing scheduled statuses", t);
      }
    }
  }

  public void close() throws IOException
  {
    this._running = false;
    this.selector.wakeup();
    this.channel.close();
  }

  private void processSelection(SelectionKey key) throws IOException
  {
    if (key.isAcceptable())
    {
      SocketChannel socket = this.channel.accept();
      socket.configureBlocking(false);
      SelectionKey readKey = socket.register(this.selector, 1);

      Client client = new ClientStub(socket, this.server, this._codec, this);

      readKey.attach(client);
    }
    else if (key.isReadable())
    {
      ClientStub client = (ClientStub)key.attachment();
      SocketChannel channel = (SocketChannel)key.channel();

      ByteBuffer buffer = getByteBuffer();
      int read = -1;
      try
      {
        read = channel.read(buffer);
      }
      catch (Exception e)
      {
        if (!e.getMessage().contains("Connection reset"))
        {
          logger.log(Level.INFO, "Problem during read", e);
        }
      }

      if (read > 0)
      {
        try
        {
          buffer.flip();
          client.read(buffer);
        }
        catch (Throwable t)
        {
          logger.log(Level.INFO, "Problem while parsing messages", t);

          client.disconnect();
          channel.close();
        }

      }
      else
      {
        client.disconnect();
        channel.close();
      }

    }
    else if (key.isWritable())
    {
      ClientStub client = (ClientStub)key.attachment();
      SocketChannel channel = (SocketChannel)key.channel();
      try
      {
        if (client.write(channel))
        {
          key.interestOps(1);

          if (client.isDisconnect())
          {
            client.disconnect();
            channel.close();
          }
        }

      }
      catch (Throwable e)
      {
        if (!e.getMessage().equals("Broken pipe"))
        {
          logger.log(Level.INFO, "Could not write to client", e);
        }
        client.disconnect();
        channel.close();
      }
    }
  }

  public ByteBuffer getByteBuffer()
  {
    return ByteBuffer.allocate(this.bufferSize);
  }

  public void returnByteBuffer(ByteBuffer buffer)
  {
  }

  Selector getSelector()
  {
    return this.selector;
  }

  public MemoryServer getServer()
  {
    return this.server;
  }
}