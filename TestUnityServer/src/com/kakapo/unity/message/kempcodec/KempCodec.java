package com.kakapo.unity.message.kempcodec;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.network.AppendableBuffers;
import com.kakapo.unity.network.BufferCharSequence;
import com.kakapo.unity.network.ByteBufferFactory;
import com.kakapo.unity.network.ByteBufferPool;
import java.nio.ByteBuffer;
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
            _input.clear();
            return dr.message;
        } catch (Exception ex) {
            Logger.getLogger(KempCodec.class.getName()).log(Level.WARNING, "Could not KEMP decode given message!"+ ex);
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
        ((LegacyPatternCodec)this.messageCodec).encodeKemp1(messageObj, this._output);
        return this._output.getBuffersForReading();
    }

    public ByteBuffer[] kemp1Encode(Message[] messageArrayObj) throws Exception {
        for (Message messageObj : messageArrayObj) {
            ((LegacyPatternCodec)this.messageCodec).encodeKemp1(messageObj, this._output);
        }
        return this._output.getBuffersForReading();
    }
    
    public ByteBuffer[] kemp2Encode(Message messageObj) throws Exception {
         ((LegacyPatternCodec)this.messageCodec).encodeKemp2(messageObj, this._output);
        return this._output.getBuffersForReading();
    }

    public ByteBuffer[] kemp2Encode(Message[] messageArrayObj) throws Exception {
        for (Message messageObj : messageArrayObj) {
            ((LegacyPatternCodec)this.messageCodec).encodeKemp2(messageObj, this._output);
        }
        return this._output.getBuffersForReading();
    }
    
    public ByteBuffer[] serverEncode(Message messageObj) throws Exception {
         ((LegacyPatternCodec)this.messageCodec).encodeServer(messageObj, this._output);
        return this._output.getBuffersForReading();
    }
    
    public ByteBuffer[] serverEncode(Message[] messageArrayObj) throws Exception {
        for (Message messageObj : messageArrayObj) {
            ((LegacyPatternCodec)this.messageCodec).encodeServer(messageObj, this._output);
        }
        return this._output.getBuffersForReading();
    }
}
