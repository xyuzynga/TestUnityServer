package com.kakapo.unity.message.server;

import com.kakapo.unity.message.Message;

public class ServerMessage extends Message {

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
}
