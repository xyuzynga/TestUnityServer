package com.kakapo.unity.message.server.error.Legacy;

import com.kakapo.unity.message.server.error.ErrorMessage;

public class ExtensionInUseMessage extends ErrorMessage {

    public ExtensionInUseMessage() {
        super(5, "EXTENSION_IN_USE", false, true);
    }
}