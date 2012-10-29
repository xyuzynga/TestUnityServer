package com.kakapo.unity.server;

import com.kakapo.unity.message.kempcodec.WrapperKempCodec;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

public class MessageDecoder extends OneToOneDecoder {
    
    private static MessageDecoder uniqueInstance;
        // other useful instance variables here
        private MessageDecoder() {
        }

        public static MessageDecoder getInstance() {
            if (uniqueInstance == null) {
                uniqueInstance = new MessageDecoder();
            }
            return uniqueInstance;
        }
        // other useful methods here

    @Override
    protected Object decode(ChannelHandlerContext chc, Channel chnl, Object o) throws Exception {
        CharSequence ch = (CharSequence) o;
        return WrapperKempCodec.getInstance().decode(ch);
    }
}    