package com.kakapo.unity.message;

public abstract class Message implements MessagePattern {

    private String _Command;
    private final boolean _disconnect;

    public Message(String command) {
        this(command, false);
    }

    public Message(String command, boolean disconnect) {
        this._Command = command;
        this._disconnect = disconnect;
    }

//    @Override
//    public abstract String toString();
    @Override
    public String getCommand() {
        return this._Command;
    }

    @Override
    public boolean isDisconnect() {
        return this._disconnect;
    }
}