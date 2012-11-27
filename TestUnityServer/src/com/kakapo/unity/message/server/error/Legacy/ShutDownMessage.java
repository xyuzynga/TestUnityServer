package com.kakapo.unity.message.server.error.Legacy;

public class ShutDownMessage extends RedirectErrorMessage {

    public ShutDownMessage() {
        super(8, "SHUTDOWN", true, true, null);
    }

    public ShutDownMessage(String redirect) {
        super(7, "SHUTDOWN_REDIRECT", false, true, redirect);
    }
}