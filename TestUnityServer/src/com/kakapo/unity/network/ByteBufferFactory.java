package com.kakapo.unity.network;

import java.nio.ByteBuffer;

public abstract interface ByteBufferFactory {

    public abstract ByteBuffer getByteBuffer();

    public abstract void returnByteBuffer(ByteBuffer paramByteBuffer);
}