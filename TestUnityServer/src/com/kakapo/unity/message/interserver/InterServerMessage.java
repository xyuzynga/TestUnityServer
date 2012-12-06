package com.kakapo.unity.message.interserver;

import com.kakapo.unity.message.Message;
import org.jboss.netty.buffer.ChannelBuffer;

public abstract class InterServerMessage extends Message {

    /**
     * Constructor for InterServerMessage
     *
     * @param command - command associated with the InterServerMessage
     */
    public InterServerMessage(String command) {
        super(command);
    }

    /**
     * Constructor for InterServerMessage
     *
     * @param command
     * @param disconnect
     */
    public InterServerMessage(String command, boolean disconnect) {
        super(command, disconnect);
    }
    
    /**
     *To ensure getEncodedMessage() is implemented in subclasses
     * @return ChannelBuffer
     */
    public abstract ChannelBuffer getEncodedMessage();
   
}
