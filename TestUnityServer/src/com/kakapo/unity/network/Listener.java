package com.kakapo.unity.network;

import com.kakapo.unity.connection.ConnectedClient;
import com.kakapo.unity.connection.Connection;
import com.kakapo.unity.connection.ConnectionStub;
import com.kakapo.unity.message.kempcodec.MessageCodec;
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

public class Listener extends Thread {

    private boolean _running = true;
    private Selector selector;
    private ServerSocketChannel channel[];
    private final UnityIMPServer server;
    private final MessageCodec _codec;
    private final Logger logger = Logger.getLogger(Listener.class.getName());
    private final int period;
    private final ByteBufferFactory objByteBufferPool;
    private final Runtime rt = Runtime.getRuntime();

    public Listener(String ports[], int bufferSize, int period, UnityIMPServer server) {
        this.period = period;
        this.server = server;
        this._codec = server.getObjKempCodec().getMessageCodec();
        this.objByteBufferPool = new ByteBufferPool(bufferSize, false);
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
        long before = System.currentTimeMillis();
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
                key = null;
            }

            try {
                this.server.processScheduledStatuses();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Problem while processing scheduled statuses", t);
            }

            try {
                //TODO - FRV  <Done> Enable after testing
                this.server.sendServerKeepAliveMessage();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Problem while sending Keep alive messages to all servers", t);
            }

            try {
                //TODO - FRV  <Done> Enable after testing
                this.server.processDisconnection();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Problem while processing disconnection events", t);
            }

            keys = null;
            iterator = null;

            System.gc();
            long now = System.currentTimeMillis();
            if (now - before > 10000) {
                System.out.println();
                logger.log(Level.WARNING, "***** totalMemory = {0} \t***** freeMemory = {1} *****", new Object[]{rt.totalMemory(), rt.freeMemory()});
                before = now;
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
            ConnectionStub objConnectionStubRef = (ConnectionStub) key.attachment();
            SocketChannel socketChannel = (SocketChannel) key.channel();

            ByteBuffer buffer = objByteBufferPool.getByteBuffer();

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
                    objConnectionStubRef.read(buffer);
                } catch (Throwable t) {
                    logger.log(Level.INFO, "Problem while parsing messages", t);
                    //TODO - FRV  <Done> Enable after unit testing
                    if (!((objConnectionStubRef.getGroupOrServerType().toString()).contains("BOUND_SERVER_ENUM"))) { /* Ensures that connection is not server but client*/
                        Logger.getLogger(ConnectedClient.class.getName()).log(Level.FINE, "\n\ndisconnect() called from processSelection()");
                        objConnectionStubRef.disconnect();
                    }
//                    socketChannel.close(); /*Already called indisconnect()*/
                }

            } else {
                Logger.getLogger(ConnectedClient.class.getName()).log(Level.FINE, "\n\ndisconnect() called from processSelection()");
                objConnectionStubRef.disconnect();
//                socketChannel.close(); /*Already called indisconnect()*/
            }
            objConnectionStubRef = null;
            socketChannel = null;
            buffer = null;

        } else if (key.isWritable()) {
            ConnectionStub objConnectionStubRef = (ConnectionStub) key.attachment();
            SocketChannel channel = (SocketChannel) key.channel();
            try {
                if (objConnectionStubRef.write(channel)) {
                    key.interestOps(1);

                    if (objConnectionStubRef.isDisconnect()) {
                        Logger.getLogger(ConnectedClient.class.getName()).log(Level.FINE, "\n\ndisconnect() called from processSelection() - key.isWritable");
                        objConnectionStubRef.disconnect();
//                        channel.close(); /*Already called indisconnect()*/
                    }
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                if (!e.getMessage().equals("Broken pipe")) {
                    logger.log(Level.INFO, "Could not write to client", e);
                }
                Logger.getLogger(ConnectedClient.class.getName()).log(Level.FINE, "\n\ndisconnect() called from processSelection() - key.isWritable");
                objConnectionStubRef.disconnect();
//                channel.close(); /*Already called indisconnect()*/
            }
            objConnectionStubRef = null;
            channel = null;
        }
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