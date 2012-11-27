package com.kakapo.unity.message;

public class KeepAliveMessage extends Message {

    public final String COMMAND = "KeepAlive";

    public KeepAliveMessage() {
        super("KeepAlive");
    }
}