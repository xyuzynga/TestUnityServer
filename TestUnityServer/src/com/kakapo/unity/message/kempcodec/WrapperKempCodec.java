package com.kakapo.unity.message.kempcodec;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.kempcodec.oldcodec.SimpleMessageCodec;

public class WrapperKempCodec {

    public static WrapperKempCodec uniqueInstance;
    // other useful instance variables here
    private static final SimpleMessageCodec simpleMessageCodec = new SimpleMessageCodec();

    private WrapperKempCodec() {
    }
    
    public static WrapperKempCodec getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new WrapperKempCodec();
        }
        return uniqueInstance;
    }
    
    // other useful methods here
    public Message decode(CharSequence kempEncodedCharSeq) throws Exception {
        CharSequence s = (CharSequence) kempEncodedCharSeq;
        SimpleMessageCodec.DecodeResult dr;
        dr = simpleMessageCodec.decode(s);
        return dr.message;
    }
}