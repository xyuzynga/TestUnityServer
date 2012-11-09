package com.kakapo.unity.message.client;

import java.util.Date;

public class ListScheduledStatusesMessage extends ClientMessage {

    public static final String COMMAND = "ListScheduledStatuses";
    private final String extension;
    private final Date start;
    private final Date end;

    public ListScheduledStatusesMessage(String extension, Date start, Date end) {
        super("ListScheduledStatuses");
        this.extension = extension;
        this.start = start;
        this.end = end;
    }

    public String getExtension() {
        return this.extension;
    }

    public Date getStart() {
        return this.start;
    }

    public Date getEnd() {
        return this.end;
    }
}