package com.kakapo.unity.message.server;

import com.kakapo.unity.message.ScheduledStatus;
import java.util.Collection;

public class ScheduledStatusListMessage extends ServerMessage {

    public final String COMMAND = "ScheduledStatusList";
    private final Collection<ScheduledStatus> statuses;

    public ScheduledStatusListMessage(Collection<ScheduledStatus> statuses) {
        super("ScheduledStatusList");
        this.statuses = statuses;
    }

    public Collection<ScheduledStatus> getStatuses() {
        return this.statuses;
    }
}