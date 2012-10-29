/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.server;

import com.kakapo.unity.message.kempcodec.WrapperCodec;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

/**
 *
 * @author felix.vincent
 */
public class mdecoder extends OneToOneDecoder{

    @Override
    protected Object decode(ChannelHandlerContext chc, Channel chnl, Object o) throws Exception {
        String s=(String) o;
        /*TODO change into singleton*/
        return new WrapperCodec().decode(s);
    }
    
}
