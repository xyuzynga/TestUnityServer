package com.kakapo.unity.message;

import java.util.Date;

public class ScheduledStatus extends Status {

    private final Date end;
    private final String id;
    private boolean valid = true;

    public ScheduledStatus(String status, Date start, Date end, String id) {
        super(status, start);
        this.end = end;
        this.id = id;
    }

    public Date getEnd() {
        return this.end;
    }

    public String getId() {
        return this.id;
    }

    public void invalidate() {
        this.valid = false;
    }

    public boolean isValid() {
        return this.valid;
    }
}