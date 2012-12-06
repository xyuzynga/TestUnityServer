package com.kakapo.unity.message.client;

public class ClearStatusMessage extends ClientMessage {

    public final String _Command = "ClearStatus";
    private final String extension;

    public ClearStatusMessage(String extension) {
        super("ClearStatus");
        this.extension = extension;
    }

    public String getExtension() {
        return this.extension;
    }
}