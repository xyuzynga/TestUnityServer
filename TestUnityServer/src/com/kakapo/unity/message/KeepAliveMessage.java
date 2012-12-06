package com.kakapo.unity.message;

public class KeepAliveMessage extends Message {

    public final String _Command = "KeepAlive";

    public KeepAliveMessage() {
        super("KeepAlive");
    }
}