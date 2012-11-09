package com.kakapo.unity.message;

public abstract class Message {

    private String _command;
    private final boolean _disconnect;

    public Message(String command) {
        this(command, false);
    }

    public Message(String command, boolean disconnect) {
        this._command = command;
        this._disconnect = disconnect;
    }

    public String getCommand() {
        return this._command;
    }

    public boolean isDisconnect() {
        return this._disconnect;
    }
}