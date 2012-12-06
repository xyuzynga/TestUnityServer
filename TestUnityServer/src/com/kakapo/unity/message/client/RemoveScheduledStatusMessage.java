package com.kakapo.unity.message.client;

public class RemoveScheduledStatusMessage extends ClientMessage {

    public final String _Command = "RemoveScheduledStatus";
    private final String id;
    private final String extension;

    public RemoveScheduledStatusMessage(String id, String extention) {
        super("RemoveScheduledStatus");
        this.id = id;
        this.extension = extention;
    }

    public String getId() {
        return this.id;
    }

    public String getExtension() {
        return this.extension;
    }
}