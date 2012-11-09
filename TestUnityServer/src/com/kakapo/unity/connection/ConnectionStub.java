package com.kakapo.unity.connection;

import com.kakapo.unity.message.KeepAliveMessage;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.client.ClientMessage;
import com.kakapo.unity.message.client.RegisterMessage;
import com.kakapo.unity.message.client.RegisterMessage2;
import com.kakapo.unity.message.interserver.InterServerMessage;
import com.kakapo.unity.message.interserver.ServerRegisterMessage;
import com.kakapo.unity.message.kempcodec.MessageCodec;
import com.kakapo.unity.message.peer.PeerMessage;
import com.kakapo.unity.network.AppendableBuffers;
import com.kakapo.unity.network.BufferCharSequence;
import com.kakapo.unity.network.Listener;
import com.kakapo.unity.server.Server;
import com.kakapo.unity.server.ServerGroup;
import com.kakapo.unity.server.UnityIMPServer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionStub implements Connection {

    private final Server server;
    private BufferCharSequence _input = new BufferCharSequence();
    private AppendableBuffers output;
    private MessageCodec codec;
    private final Listener listener;
    private final SocketChannel channel;
    private boolean disconnect;
    private boolean registered;

    public CharSequence getLoginIdOrServerName() {
        return loginIdOrServerName;
    }

    public CharSequence getGroupOrServerType() {
        return groupOrServerType;
    }
    public final AtomicInteger sent;
    public final AtomicInteger received;
    private static final Logger logger;
    private CharSequence loginIdOrServerName;
    private CharSequence groupOrServerType;
    private Date lastMessageTime;

    public ConnectionStub(SocketChannel channel, Server server, MessageCodec codec, Listener listener) {
        sent = new AtomicInteger();
        received = new AtomicInteger();
        this.channel = channel;
        this.codec = codec;
        this.listener = listener;
        this.server = server;
        this.output = new AppendableBuffers(listener);
    }

    @Override
    public void read(ByteBuffer buffer)
            throws IOException {
        this._input.addBuffer(buffer);

        Message message;
        while ((message = readMessage()) != null) {
            send(message);
        }
    }

    @Override
    public void send(Message message) {
        received.incrementAndGet();
        lastMessageTime=new Date();
        UnityIMPServer objUnityIMPServerReference = (UnityIMPServer) this.server;
        try {

            //<editor-fold defaultstate="collapsed" desc="ProcessKeepAlive">
            
            if ((message instanceof KeepAliveMessage)) {
                if (!this.registered) {
                    throw new IllegalStateException("Client is not registered " + this);
                }
                // TODO - AJ Process KeepAlive message both for Server & Client

                //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="ProcessRegistration">

            } else if ((message instanceof RegisterMessage)) {
                RegisterMessage rm = (RegisterMessage) message;
                this.groupOrServerType = (String) rm.getGroup();
                this.loginIdOrServerName = (String) rm.getExtension();
                ConnectedClient objConnectedClientLegacy = new ConnectedClient(null, this);
                objUnityIMPServerReference.processClientRegisteration(objConnectedClientLegacy);
                this.registered = true;
            } else if ((message instanceof RegisterMessage2)) {
                RegisterMessage2 objRegisterMessage2 = (RegisterMessage2) message;
                this.groupOrServerType = (String) objRegisterMessage2.getGroup();
                this.loginIdOrServerName = (String) objRegisterMessage2.getLoginID();
                ConnectedClient objConnectedClientNew = new ConnectedClient(objRegisterMessage2.getProductName(), this);
                objUnityIMPServerReference.processClientRegisteration(objConnectedClientNew);
                this.registered = true;
            } else if ((message instanceof ServerRegisterMessage)) {
                ServerRegisterMessage objServerRegisterMessage = (ServerRegisterMessage) message;
                this.loginIdOrServerName = (String) objServerRegisterMessage.getServerName();
                this.groupOrServerType = ServerGroup.SERVER_TYPE.INBOUND_SERVER_ENUM.toString();
                ConnectedServer objConnectedServer = new ConnectedServer(this);
                objUnityIMPServerReference.processServerRegisteration(objConnectedServer);
                this.registered = true;
                //</editor-fold>

            } else if ((message instanceof ClientMessage || message instanceof PeerMessage)) {
                if (!this.registered) {
                    throw new IllegalStateException("Client is not registered " + this);
                }
                objUnityIMPServerReference.getGroups().get(groupOrServerType.toString()).getConnectedClient(loginIdOrServerName.toString()).send(message);

            } else if ((message instanceof InterServerMessage)) {
                if (!this.registered) {
                    throw new IllegalStateException("Server is not registered " + this);
                }
                objUnityIMPServerReference.getGroups().get(groupOrServerType.toString()).getConnectedClient(loginIdOrServerName.toString()).send(message);
            } else {
                throw new IllegalArgumentException("Unknown message" + message);
            }

        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.log(Level.WARNING, "Problem processing message " + message, e);
        }
    }

    private Message readMessage() {
        if (this._input.length() > 0) {

            //TODO - PP Kemp decode
            MessageCodec.DecodeResult result = this.codec.decode(this._input);

            if (result != null) {
                Message message = result.message;

                this._input = ((BufferCharSequence) this._input.subSequence(result.chars, this._input.length()));
                return message;
            }
        }

        return null;
    }

    void receive(Message message) {
        try {
            if ((message instanceof PeerMessage)) {
                CharSequence input = ((PeerMessage) message).getInput();

                if (input != null) {
                    assert ("Command: Message".contentEquals(input.subSequence(0, "Command: Message".length())));

                    this.output.append(input);
                } else {
                    //TODO - PP & FRV Kemp Encode
                    this.codec.encode(message, this.output);
                }
            } else {
                this.codec.encode(message, this.output);
            }

            sent.incrementAndGet();
            if (sent.get() % 10000 == 0) {
                System.out.println("Sent: " + sent.get());
                System.out.println("Received: " + received.get());
            }

        } catch (Exception e) {
            this.output.clear();

            throw new RuntimeException(e);
        }

        try {
            this.channel.register(this.listener.getSelector(), 4, this);
        } catch (ClosedChannelException e) {
            logger.info("Channel was closed so message not sent");
        }

        if (message.isDisconnect()) {
            this.disconnect = true;
        }
    }

    public boolean isDisconnect() {
        return this.disconnect;
    }

    public boolean write(SocketChannel channel)
            throws IOException {
        if (logger.isLoggable(Level.FINE)) {
            String writing = this.output.toString();
            logger.fine(writing);
        }

        ByteBuffer[] buffers = this.output.getBuffersForReading();

        channel.write(buffers);

        boolean finished = this.output.isComplete();

        if (finished) {
            this.output.clear();
        }

        return finished;
    }

    @Override
    public void disconnect() {
        this.server.unregister(this);
        this.registered = false;
    }

    static {
        logger = Logger.getLogger(ConnectionStub.class.getName());
    }

    
}
