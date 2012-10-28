package com.kakapo.unity.server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Handler {

    private static final int messagesReceived = 0;
    private static final SocketChannel channelConnected = null;
    private boolean isDisconnected = true;

    public Handler() {
        /*TODO-GK Consturctor of Handler*/
    }

    private ByteBuffer read() {
        /*TODO-GK Handle read event*/
        return null;
    }
    
    private void write(ByteBuffer bb) {
        /*TODO-GK Handle write event */
    }

    /**
     * Get the value of messagesReceived
     *
     * @return the value of messagesReceived
     */
    public static int getMessagesReceived() {
        return messagesReceived;
    }

    /**
     * Get the value of channelConnected
     *
     * @return the value of channelConnected
     */
    public static SocketChannel getChannelConnected() {
        return channelConnected;
    }

    /**
     * Get the value of isDisconnected
     *
     * @return the value of isDisconnected
     */
    public boolean isIsDisconnected() {
        return isDisconnected;
    }
}
