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

    public static void main(String[] args) {
        KempCodec objKempCodec = new KempCodec();
        objKempCodec.testMessageCodec();
    }

    private void testMessageCodec() {
        try {

            String messageTemplate = "StatusList";

            int ch;
            StringBuilder strContent = new StringBuilder("");
            try {
                FileInputStream fis = new FileInputStream("src/com/kakapo/unity/message/samplemessages/" + messageTemplate + ".txt");
                while ((ch = fis.read()) != -1) {
                    strContent.append((char) ch);
                }
                fis.close();
            } catch (Exception e) {
                System.out.println(e);
            }

            ByteBuffer bbf = ByteBuffer.wrap((strContent.toString()).getBytes());
            Message messageObj = decode(bbf);
            System.out.println("messageObj = " + messageObj);
//***********************************************************************************************************
            ByteBuffer[] bba = kemp1Encode(messageObj);

            Charset charset = Charset.forName("UTF-8");
            CharsetDecoder decoder = charset.newDecoder();

            String data = "";
            for (ByteBuffer byteBuffer : bba) {
                int old_position = byteBuffer.position();
                data = decoder.decode(byteBuffer).toString();
                // reset buffer's position to its original so it is not altered:
                System.out.print(data);
                byteBuffer.position(old_position);
            }
        } catch (Exception ex) {
            Logger.getLogger(KempCodec.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
        ((LegacyPatternCodec)this.messageCodec).encodeKemp1(messageObj, this._output);
        return this._output.getBuffersForReading();
    }

    public ByteBuffer[] kemp2Encode(Message messageObj) throws Exception {
         ((LegacyPatternCodec)this.messageCodec).encodeKemp1(messageObj, this._output);
        return this._output.getBuffersForReading();
    }

    public ByteBuffer[] serverEncode(Message messageObj) throws Exception {
         ((LegacyPatternCodec)this.messageCodec).encodeKemp1(messageObj, this._output);
        return this._output.getBuffersForReading();
    }
}
