package com.kakapo.unity.message.kempcodec;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.network.AppendableBuffers;
import com.kakapo.unity.network.BufferCharSequence;
import com.kakapo.unity.network.ByteBufferFactory;
import com.kakapo.unity.network.ByteBufferPool;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KempCodec {

    private final MessageCodec messageCodec = new LegacyPatternCodec();
    private final ByteBufferFactory objByteBufferPool = new ByteBufferPool(512, true);
    private ByteBuffer _input = objByteBufferPool.getByteBuffer();
    private final AppendableBuffers _output = new AppendableBuffers(objByteBufferPool);
    private final BufferCharSequence messageCharSequence = new BufferCharSequence();

    public MessageCodec getMessageCodec() {
        return messageCodec;
    }

    public Message decode(ByteBuffer inputBuffer) throws Exception {
        try {
            _input = inputBuffer;
            MessageCodec.DecodeResult dr;
            messageCharSequence.addBuffer(_input);
            dr = messageCodec.decode(messageCharSequence);
            return dr.message;
        } catch (Exception ex) {
            Logger.getLogger(KempCodec.class.getName()).log(Level.WARNING, "Could not KEMP decode given message!", ex);
            return null;
        } finally {
            objByteBufferPool.returnByteBuffer(_input);
        }
    }

    public Message decode(CharSequence kempEncodedCharSeq) throws Exception {
        MessageCodec.DecodeResult dr;
        dr = messageCodec.decode(kempEncodedCharSeq);
        return dr.message;
    }

    public ByteBuffer[] kemp1Encode(Message messageObj) throws Exception {
        this.messageCodec.encode(messageObj, this._output);
        return this._output.getBuffersForReading();
    }

    public CharSequence kemp2Encode(Message messageObj) throws Exception {
        throw new UnsupportedOperationException("Method kemp2Encode(Message messageObj) not yet implemented");
    }

    public CharSequence serverEncode(Message messageObj) throws Exception {
        throw new UnsupportedOperationException("Method serverEncode(Message messageObj) not yet implemented");
    }
}
