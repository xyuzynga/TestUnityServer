package com.kakapo.unity.message.redundantserver;

import com.kakapo.unity.message.Message;

public class ServerMessage extends Message {

    /**
     * Constructor for ServerMessage
     *
     * @param command - command associated with the ServerMessage
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
}
