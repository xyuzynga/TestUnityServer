package com.kakapo.unity.message.kempcodec;

import com.kakapo.unity.message.Message;

public abstract interface MessageCodec {

    public abstract void encode(Message paramMessage, Appendable paramAppendable);

    public abstract DecodeResult decode(CharSequence paramCharSequence);

    public static class DecodeResult {

        public Message message;
        public int chars;

        public DecodeResult(Message message, int position) {
            this.message = message;
            this.chars = position;
        }
    }
}