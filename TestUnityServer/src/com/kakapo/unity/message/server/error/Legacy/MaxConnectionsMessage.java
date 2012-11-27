package com.kakapo.unity.message.server.error.Legacy;

public class MaxConnectionsMessage extends RedirectErrorMessage {

    public MaxConnectionsMessage() {
        super(3, "MAX_CONNECTIONS", true, true, null);
    }

    public MaxConnectionsMessage(String redirect) {
        super(2, "MAX_CONNECTIONS_REDIRECT", false, true, redirect);
    }
}