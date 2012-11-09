package com.kakapo.unity.message;

import java.util.Date;

public class ManualStatus extends Status {

    private final boolean override;

    public ManualStatus(String name, Date startDate, boolean override) {
        super(name, startDate);
        this.override = override;
    }

    public boolean isOverride() {
        return this.override;
    }
}