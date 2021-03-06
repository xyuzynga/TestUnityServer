package com.kakapo.unity.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Iterator;
import java.util.LinkedList;

public class AppendableBuffers implements Appendable {

    private LinkedList<ByteBuffer> buffers = new LinkedList<ByteBuffer>();
    private ByteBuffer last;
    private boolean read;
    private final ByteBufferFactory factory;
    private final Charset _charset = Charset.forName("UTF-8");
    private final CharsetEncoder _encoder = this._charset.newEncoder();

    public AppendableBuffers(ByteBufferFactory factory) {
        this.factory = factory;
    }

    public ByteBuffer[] getBuffersForReading() {
        if (this.last != null) {
            this.last.flip();
            this.last = null;
        }
        this.read = true;
        return this.buffers.toArray(new ByteBuffer[this.buffers.size()]);
    }

    public boolean isComplete() {
        for (ByteBuffer buffer : this.buffers) {
            if (buffer.hasRemaining()) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        Iterator<ByteBuffer> iterator = this.buffers.iterator();
        while (iterator.hasNext()) {
            this.factory.returnByteBuffer(iterator.next());
            iterator.remove();
        }
        this.last = null;
        this.read = false;
        iterator = null;
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        return append(csq, 0, csq.length());
    }

    @Override
    public Appendable append(char c) throws IOException {
        return append(Character.valueOf(c).toString());
    }

    @Override
    public Appendable append(CharSequence input, int start, int end) throws IOException {
        if ((input instanceof BufferCharSequence)) {
            if (this.last != null) {
                this.last.flip();
                this.last = null;
            }
            this.buffers.addAll(((BufferCharSequence) input).getBuffers());
        } else {
            CharBuffer chars = CharBuffer.wrap(input);
            CoderResult result;
            do {
                result = this._encoder.encode(chars, getBuffer(), false);
            } while (result.equals(CoderResult.OVERFLOW));
            result = null;
            chars = null;
        }
        return this;
    }

    @Override
    public String toString() {
        return toCharSequence().toString();
    }

    private ByteBuffer getBuffer() {
        if ((this.last != null) && (this.last.hasRemaining())) {
            return this.last;
        }

        ByteBuffer result = this.factory.getByteBuffer();
        this.buffers.add(result);
        if (this.last != null) {
            this.last.flip();
        }

        this.last = result;

        return result;
    }

    private CharSequence toCharSequence() {
        try {
            BufferCharSequence cs = new BufferCharSequence();
            for (ByteBuffer buffer : this.buffers) {
                ByteBuffer duplicate = buffer.duplicate();
                cs.addBuffer(duplicate);
                duplicate = null;
            }

            if (this.last != null) {
                ByteBuffer duplicate = this.last.duplicate();
                duplicate.flip();
                cs.addBuffer(duplicate);
                duplicate = null;
            }
            return cs;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}