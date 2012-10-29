package com.kakapo.unity.message.kempcodec;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.kempcodec.oldcodec.SimpleMessageCodec;

public class WrapperCodec {

    private static final SimpleMessageCodec simpleMessageCodec = new SimpleMessageCodec();

    public void encode(Message paramMessage, Appendable paramAppendable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Message decode(CharSequence paramCharSequence) {
        SimpleMessageCodec.DecodeResult dr;
        dr = simpleMessageCodec.decode(paramCharSequence);
        return dr.message;
    }
}
