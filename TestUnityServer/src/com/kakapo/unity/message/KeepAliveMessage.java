package com.kakapo.unity.message;

import com.kakapo.unity.message.Message;

public class KeepAliveMessage extends Message {

    public static final String COMMAND = "KeepAlive";

    public KeepAliveMessage() {
        super("KeepAlive");
    }
}