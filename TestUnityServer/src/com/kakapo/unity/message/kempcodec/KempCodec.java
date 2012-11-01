package com.kakapo.unity.message.kempcodec;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.kempcodec.legacy.AppendableBuffers;
import com.kakapo.unity.message.kempcodec.legacy.ByteBufferFactory;
import com.kakapo.unity.message.kempcodec.legacy.SimpleMessageCodec;

public class KempCodec {

    public static KempCodec uniqueInstance;
    // other useful instance variables here
    private static final MessageCodec messageCodec = new SimpleMessageCodec();
    private AppendableBuffers output;
    

    private KempCodec(ByteBufferFactory bbf) {
       this.output = new AppendableBuffers(bbf); 
    }

    public static KempCodec getInstance(ByteBufferFactory bbf) {
        if (uniqueInstance == null) {
            uniqueInstance = new KempCodec(bbf);
        }
        return uniqueInstance;
    }

    // other useful methods here
    public Message decode(CharSequence kempEncodedCharSeq) throws Exception {
//        CharSequence s = (CharSequence) kempEncodedCharSeq;
        SimpleMessageCodec.DecodeResult dr;
        dr = messageCodec.decode(kempEncodedCharSeq);
        return dr.message;
    }

    public AppendableBuffers kemp1Encode(Message messageObj) throws Exception {
        
        KempCodec.messageCodec.encode(messageObj, this.output);
        return this.output;
    }

    public CharSequence kemp2Encode(Message message) throws Exception {

        return null;
    }

    public CharSequence serverEncode(Message message) throws Exception {

        return null;
    }

    
}