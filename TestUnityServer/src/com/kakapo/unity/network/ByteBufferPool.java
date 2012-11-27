package com.kakapo.unity.network;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ByteBufferPool implements ByteBufferFactory {

    private final int initialCapacity;
    private final boolean direct;
    // Used to store soft references to byte buffers
    private final Queue<SoftReference<ByteBuffer>> pool =
            new ConcurrentLinkedQueue<SoftReference<ByteBuffer>>();

    public ByteBufferPool(int initialCapacity, boolean direct) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity cannot be "
                    + "smaller than 0.");
        }

        this.initialCapacity = initialCapacity;
        this.direct = direct;
    }

    public void clear() {
        pool.clear();
    }

    @Override
    public ByteBuffer getByteBuffer() {
        // Poll the oldest buffer ref
        SoftReference<ByteBuffer> byteBufferRef = pool.poll();

        // Tmp
        ByteBuffer buffer;

        if (byteBufferRef == null || (buffer = byteBufferRef.get()) == null) {
            // Allocate a new byte buffer
            buffer =
                    direct ? ByteBuffer.allocateDirect(initialCapacity)
                    : ByteBuffer.allocate(initialCapacity);
        } else {
            // Clear the old buffer
            buffer.clear();
        }

        return buffer;
    }

    @Override
    public void returnByteBuffer(ByteBuffer paramByteBuffer) {
        if (paramByteBuffer == null) {
            throw new NullPointerException("byteBuffer");
        }

        // Queue a new soft reference to the byte buffer
        pool.offer(new SoftReference<ByteBuffer>(paramByteBuffer));
    }
}
