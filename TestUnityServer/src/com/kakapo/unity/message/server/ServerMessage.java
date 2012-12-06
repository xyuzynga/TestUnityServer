package com.kakapo.unity.message.server;

import com.kakapo.unity.message.Message;
import org.jboss.netty.buffer.ChannelBuffer;

public abstract class ServerMessage extends Message {

    /**
     * Constructor for ServerMessage
     *
     * @param command - command associated with the InterServerMessage
     */
    public ServerMessage(String command) {
        super(command);
    }

    /**
     * Constructor for ServerMessage
     *
     * @param command
     * @param disconnect
     */
    public ServerMessage(String command, boolean disconnect) {
        super(command, disconnect);
    }
    
    /**
     *To ensure getEncodedMessage() is implemented in subclasses
     * @return ChannelBuffer
     */
    public abstract ChannelBuffer getEncodedMessage();
}
