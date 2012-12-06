package com.kakapo.unity.message.client;

import com.kakapo.unity.message.Message;

public abstract class ClientMessage extends Message {

    public ClientMessage(String command, boolean disconnect) {
        super(command, disconnect);
    }

    public ClientMessage(String command) {
        super(command);
    }
}