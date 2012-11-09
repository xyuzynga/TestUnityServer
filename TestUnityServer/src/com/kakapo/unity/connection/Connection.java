package com.kakapo.unity.connection;

import com.kakapo.unity.message.Message;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract interface Connection {

    public void read(ByteBuffer buffer) throws IOException;

    public abstract void send(Message paramMessage);

    public abstract void disconnect();
}