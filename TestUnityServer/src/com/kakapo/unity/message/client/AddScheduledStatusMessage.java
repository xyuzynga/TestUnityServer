package com.kakapo.unity.message.client;

import com.kakapo.unity.message.ScheduledStatus;
import java.util.Date;
import org.jboss.netty.buffer.ChannelBuffer;

public class AddScheduledStatusMessage extends ClientMessage {

    public final String _Command = "AddScheduledStatus";
    private final ScheduledStatus scheduledStatus;
    private final String extension;

    public AddScheduledStatusMessage(String extension, String status, Date start, Date end, String id) {
        this(extension, new ScheduledStatus(status, start, end, id));
    }

    public AddScheduledStatusMessage(String extension, ScheduledStatus scheduledStatus) {
        super("AddScheduledStatus");
        this.extension = extension;
        this.scheduledStatus = scheduledStatus;
    }

    public ScheduledStatus getScheduledStatus() {
        return this.scheduledStatus;
    }

    public String getExtension() {
        return this.extension;
    }
}