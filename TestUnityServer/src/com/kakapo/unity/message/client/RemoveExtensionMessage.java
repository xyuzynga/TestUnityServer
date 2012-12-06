package com.kakapo.unity.message.client;

public class RemoveExtensionMessage extends ClientMessage {

    public final String _Command = "RemoveExtension";
    private final String extension;

    public RemoveExtensionMessage(String extension) {
        super("RemoveExtension");
        this.extension = extension;
    }

    public String getExtension() {
        return this.extension;
    }
}