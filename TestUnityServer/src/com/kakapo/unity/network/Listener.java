package com.kakapo.unity.network;

import com.kakapo.unity.connection.ConnectionStub;
import com.kakapo.unity.connection.Connection;
import com.kakapo.unity.message.kempcodec.MessageCodec;
import com.kakapo.unity.message.kempcodec.legacy.SimpleMessageCodec;
import com.kakapo.unity.server.Server;
import com.kakapo.unity.server.UnityIMPServer;
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

public class Listener extends Thread
        implements ByteBufferFactory {

    private boolean _running = true;
    private Selector selector;
    private ServerSocketChannel channel[];
    private final UnityIMPServer server;
    private final MessageCodec _codec = new SimpleMessageCodec();
    private final int bufferSize;
    private static final Logger logger = Logger.getLogger(Listener.class.getName());
    private final int period;

    public Listener(String ports[], int bufferSize, int period, UnityIMPServer server) {
        this.bufferSize = bufferSize;
        this.period = period;
        this.server = server;
        try {
            this.selector = Selector.open();
            channel = new ServerSocketChannel[ports.length];
            int i = 0;
            for (String port : ports) {

                this.channel[i] = ServerSocketChannel.open();
                this.channel[i].configureBlocking(false);
                this.channel[i].socket().bind(new InetSocketAddress(Integer.parseInt(port)));
                this.channel[i].register(this.selector, 16);

            }
        } catch (IOException e) {
            throw new RuntimeException("Could not register listener", e);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    @Override
    public void run() {
        while (this._running) {
            try {
                this.selector.select(this.period);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Problem during select", e);
            }

            Set<SelectionKey> keys = this.selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {

                SelectionKey key = iterator.next();

                iterator.remove();
                try {
                    processSelection(key);
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.log(Level.WARNING, "Problem during key processing", e);
                    if (key != null) {
                        key.cancel();
                    }
                }
            }
            try {
                this.server.processScheduledStatuses();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Problem while processing scheduled statuses", t);
            }

            try {

                this.server.sendServerKeepAliveMessage();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Problem while sending Keep alive messages to all servers", t);
            }

            try {
                this.server.processDisconnection();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Problem while processing disconnection events", t);
            }

        }
    }

    public void close() throws IOException {
        this._running = false;
        this.selector.wakeup();
        for (ServerSocketChannel serverSocketChannel : channel) {
            serverSocketChannel.close();
        }
    }

    private void processSelection(SelectionKey key) throws IOException {

        if (key.isAcceptable()) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel socket = serverSocketChannel.accept();
            socket.configureBlocking(false);
            SelectionKey readKey = socket.register(this.selector, 1);
            Connection client = new ConnectionStub(socket, this.server, this._codec, this);
            readKey.attach(client);

        } else if (key.isReadable()) {
            ConnectionStub client = (ConnectionStub) key.attachment();
            SocketChannel socketChannel = (SocketChannel) key.channel();

            ByteBuffer buffer = getByteBuffer();
            System.out.println("bb" + buffer.toString());
            int read = -1;
            try {
                read = socketChannel.read(buffer);
            } catch (Exception e) {
                if (!e.getMessage().contains("Connection reset")) {
                    logger.log(Level.INFO, "Problem during read", e);
                }
            }

            if (read > 0) {
                try {
                    buffer.flip();
                    client.read(buffer);
                } catch (Throwable t) {
                    logger.log(Level.INFO, "Problem while parsing messages", t);

                    client.disconnect();
                    socketChannel.close();
                }

            } else {
                client.disconnect();
                socketChannel.close();
            }

        } else if (key.isWritable()) {
            ConnectionStub client = (ConnectionStub) key.attachment();
            SocketChannel channel = (SocketChannel) key.channel();
            try {
                if (client.write(channel)) {
                    key.interestOps(1);

                    if (client.isDisconnect()) {
                        client.disconnect();
                        channel.close();
                    }
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                if (!e.getMessage().equals("Broken pipe")) {
                    logger.log(Level.INFO, "Could not write to client", e);
                }
                client.disconnect();
                channel.close();
            }
        }
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return ByteBuffer.allocate(this.bufferSize);
    }

    @Override
    public void returnByteBuffer(ByteBuffer buffer) {
    }

    /**
     *
     * @return
     */
    public Selector getSelector() {
        return this.selector;
    }

    public UnityIMPServer getServer() {
        return this.server;
    }
}