package com.kakapo.unity.message.client;

import com.kakapo.unity.message.ManualStatus;
import java.util.Date;

public class SetStatusMessage extends ClientMessage {

    public final String COMMAND = "SetStatus";
    private final ManualStatus status;
    private final String extension;

    public SetStatusMessage(String extension, String name, boolean override) {
        super("SetStatus");
        this.extension = extension;
        this.status = new ManualStatus(name, new Date(), override);
    }

    public ManualStatus getStatus() {
        return this.status;
    }

    public String getExtension() {
        return this.extension;
    }
}