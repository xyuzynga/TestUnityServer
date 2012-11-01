package com.kakapo.unity.message.kempcodec.nettyhandlers;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.kempcodec.KempCodec;
import com.kakapo.unity.message.kempcodec.legacy.ByteBufferFactory;
import java.nio.ByteBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

@Sharable
public class DecoderMessage extends OneToOneDecoder implements ByteBufferFactory {

    private static int DEFAULT_BUFFER_SIZE = 512;
    
    private static DecoderMessage uniqueInstance;
        // other useful instance variables here
        private DecoderMessage() {
        }

        public static DecoderMessage getInstance() {
            if (uniqueInstance == null) {
                uniqueInstance = new DecoderMessage();
            }
            return uniqueInstance;
        }
        // other useful methods here

    @Override
    protected Object decode(ChannelHandlerContext chc, Channel chnl, Object o) throws Exception {
        ByteBufferFactory bbf = DecoderMessage.getInstance();
        CharSequence ch = (CharSequence) o;
        return KempCodec.getInstance(bbf).decode(ch);
         
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    }

    @Override
    public void returnByteBuffer(ByteBuffer paramByteBuffer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }    
}    