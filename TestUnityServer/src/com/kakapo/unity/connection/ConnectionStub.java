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
import com.kakapo.unity.network.ByteBufferFactory;
import com.kakapo.unity.network.ByteBufferPool;
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

    public void setGroupOrServerType(CharSequence groupOrServerType) {
        this.groupOrServerType = groupOrServerType;
    }
    private final Server server;
    private BufferCharSequence _input = new BufferCharSequence();
    private final ByteBufferFactory objByteBufferPool;    
    private final AppendableBuffers _output;
    private final MessageCodec codec;
    private final Listener listener;
    private final SocketChannel channel;
    private boolean disconnect;
    private boolean registered;
    public final AtomicInteger sent;
    public final AtomicInteger received;
    private CharSequence loginIdOrServerName;
    private CharSequence groupOrServerType;
    private Date lastMessageTime = new Date();
    Logger logger = Logger.getLogger(ConnectionStub.class.getName());

    Server getServer() {
        return server;
    }

    public CharSequence getLoginIdOrServerName() {
        return loginIdOrServerName;
    }

    public CharSequence getGroupOrServerType() {
        return groupOrServerType;
    }

    public ConnectionStub(SocketChannel channel, Server server, MessageCodec codec, Listener listener) {
        sent = new AtomicInteger();
        received = new AtomicInteger();
        this.channel = channel;
        this.codec = codec;
        this.listener = listener;
        this.server = server;
        this.objByteBufferPool = new ByteBufferPool((((UnityIMPServer)server).getDEFAULT_BUFFER_SIZE()),false);
        this._output = new AppendableBuffers(this.objByteBufferPool);
    }

    public ConnectionStub(SocketChannel channel, Server server, MessageCodec codec, Listener listener, boolean registered, CharSequence servername) {
        sent = new AtomicInteger();
        received = new AtomicInteger();
        this.channel = channel;
        this.codec = codec;
        this.listener = listener;
        this.server = server;
        this.registered = registered;
        this.loginIdOrServerName = servername;
        this.objByteBufferPool = new ByteBufferPool((((UnityIMPServer)server).getDEFAULT_BUFFER_SIZE()),false);
        this._output = new AppendableBuffers(this.objByteBufferPool);
    }

    @Override
    public void read(ByteBuffer buffer)
            throws IOException {
        this._input.addBuffer(buffer);

        Message message;
        while ((message = readMessage()) != null) {
            send(message);
        }
        message = null;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    @Override
    public void send(Message message) {
        received.incrementAndGet();
        lastMessageTime = new Date();
        UnityIMPServer objUnityIMPServerReference = (UnityIMPServer) this.server;
        try {

            //<editor-fold defaultstate="collapsed" desc="ProcessKeepAlive">

            if ((message instanceof KeepAliveMessage)) {
                if (!this.registered) {
                    throw new IllegalStateException("Client is not registered " + this);
                }

                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="ProcessRegistration">

            } else if ((message instanceof RegisterMessage)) {
                RegisterMessage rm = (RegisterMessage) message;
                this.loginIdOrServerName = rm.getExtension().toString();
                this.groupOrServerType = rm.getGroup().toString();
                ConnectedClient objConnectedClientLegacy = new ConnectedClient(null, this);
                //TODO - FRV <Done> call getBlackListedRegistration(objConnectedClientLegacy) CANNOT BLACKLIST LEGACY CLIENT (NO CHECKSUM)
                //TODO - FRV <Done> call checkBlackListMessageFrequency()
                if (!(objConnectedClientLegacy.checkBlackListMessageFrequency(loginIdOrServerName.toString()))) {
                    objUnityIMPServerReference.processClientRegisteration(objConnectedClientLegacy);
                    this.registered = true;
                }
            } else if ((message instanceof RegisterMessage2)) {

                RegisterMessage2 objRegisterMessage2 = (RegisterMessage2) message;
                this.loginIdOrServerName = (objRegisterMessage2.getLoginID()).toString();
                this.groupOrServerType = (objRegisterMessage2.getGroup()).toString();
                ConnectedClient objConnectedClientNew = new ConnectedClient(objRegisterMessage2.getProductName(), this);
                //TODO - FRV <Done> call getBlackListedRegistration()
                objConnectedClientNew.getBlackListedRegistration(objRegisterMessage2.getCheckSum().toString());
                objUnityIMPServerReference.processClientRegisteration(objConnectedClientNew);
                this.registered = true;
            } else if ((message instanceof ServerRegisterMessage)) {
                ServerRegisterMessage objServerRegisterMessage = (ServerRegisterMessage) message;
                this.loginIdOrServerName = (objServerRegisterMessage.getServerName()).toString();
                this.groupOrServerType = ServerGroup.SERVER_TYPE.INBOUND_SERVER_ENUM.toString();
                ConnectedServer objConnectedServer = new ConnectedServer(this);
                objUnityIMPServerReference.processServerRegisteration(objConnectedServer);
                this.registered = true;
                //</editor-fold>

            } else if ((message instanceof ClientMessage || message instanceof PeerMessage)) {
                if (!this.registered) {

                    throw new IllegalStateException("Client is not registered " + this);
                }
                //TODO - FRV <Done> getBlackListedMessageFrequency()
                objUnityIMPServerReference.getGroups().get(groupOrServerType.toString()).getConnectedClient(loginIdOrServerName.toString()).getBlackListedMessageFrequency();

                objUnityIMPServerReference.getGroups().get(groupOrServerType.toString()).getConnectedClient(loginIdOrServerName.toString()).send(message);

            } else if ((message instanceof InterServerMessage)) {
                if (!this.registered) {
                    throw new IllegalStateException("Server is not registered ");
                }
                // objUnityIMPServerReference.getGroups().get(groupOrServerType.toString()).getConnectedClient(loginIdOrServerName.toString()).send(message);
                if (objUnityIMPServerReference.getINBOUND_SERVERS().containsConnectedServer(loginIdOrServerName.toString())) {
                    objUnityIMPServerReference.getINBOUND_SERVERS().getConnectedServer(loginIdOrServerName.toString()).send((InterServerMessage) message);
                } else {
                    objUnityIMPServerReference.getOUTBOUND_SERVERS().getConnectedServer(loginIdOrServerName.toString()).send((InterServerMessage) message);
                }

            } else {
                throw new IllegalArgumentException("Unknown message" + message);
            }

        } catch (IllegalStateException | IllegalArgumentException e) {
            try {
                this.channel.close();
            } catch (IOException ex) {
                Logger.getLogger(ConnectionStub.class.getName()).log(Level.SEVERE, null, ex);
            }
            logger.log(Level.WARNING, "Problem processing message " + message, e);
        }
        objUnityIMPServerReference = null;
    }

    private Message readMessage() {
        if (this._input.length() > 0) {

            //TODO - PP  <Done> Kemp decode
            MessageCodec.DecodeResult result = this.codec.decode(this._input);

            if (result != null) {
                Message message = result.message;

                this._input = ((BufferCharSequence) this._input.subSequence(result.chars, this._input.length()));
                return message;
            }
            result = null;
        }

        return null;
    }

    void receive(Message message) {
        try {
            if ((message instanceof PeerMessage)) {
                CharSequence input = ((PeerMessage) message).getInput();

                if (input != null) {
                    assert ("Command: Message".contentEquals(input.subSequence(0, "Command: Message".length())));

                    this._output.append(input);
                } else {
                    //TODO - PP & FRV  <Done> Kemp Encode
                    this.codec.encode(message, this._output);
                }
                input = null;
            } else {
                this.codec.encode(message, this._output);
            }

            sent.incrementAndGet();
            if (sent.get() % 10000 == 0) {
            }

        } catch (Exception e) {
            this._output.clear();

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
        ByteBuffer[] buffers = this._output.getBuffersForReading();
        channel.write(buffers);
        boolean finished = this._output.isComplete();

        if (finished) {
            this._output.clear();
        }
        if (isDisconnect()) {
            try {
                this.channel.configureBlocking(true);
                this.channel.close();
            } catch (IOException iOException) {
                Logger.getLogger(ConnectionStub.class.getName()).log(Level.SEVERE, "Marked to disconnect - Closing channel", iOException);
            }
        }
        
        buffers = null;
        return finished;
    }

    @Override
    public void disconnect() {
        this.server.unregister(this);
        this.registered = false;
        try {
            this.channel.close();
        } catch (IOException ex) {
            Logger.getLogger(ConnectionStub.class.getName()).log(Level.SEVERE, "Problem while Disconnecting - Closing channel", ex);
        }

    }

    @Override
    public String toString() {
        return "ConnectionStub{\n\t " + " loginIdOrServerName = " + loginIdOrServerName + "\n\t groupOrServerType = " + groupOrServerType + "\n\t lastMessageTime = " + lastMessageTime + "\n\t}";
    }

    public SocketChannel getChannel() {
        return channel;
    }
}
