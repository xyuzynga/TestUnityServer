package com.kakapo.unity.message.kempcodec.legacy;

import java.nio.ByteBuffer;

public abstract interface ByteBufferFactory {

    public abstract ByteBuffer getByteBuffer();

    public abstract void returnByteBuffer(ByteBuffer paramByteBuffer);
}