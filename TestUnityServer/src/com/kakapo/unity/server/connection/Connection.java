package com.kakapo.unity.server.connection;

import com.kakapo.unity.message.Message;

public abstract interface Connection {

    public abstract void receive(Message paramMessage);
    
    public abstract void processMessage(Message paramMessage);
}
