package com.kakapo.unity.message.server.error;

public class OverrideMessage extends ErrorMessage {

    public OverrideMessage() {
        super(6, "OVERRIDE", true, true);
    }
}