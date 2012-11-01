/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.message.kempcodec.nettyhandlers;

import com.kakapo.unity.message.Message;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 *
 * @author felix.vincent
 */
@Sharable
public class EncoderKEMP2 extends OneToOneEncoder {

    private static EncoderKEMP2 uniqueInstance;
    // other useful instance variables here
    
    private EncoderKEMP2() {
    }

    public static EncoderKEMP2 getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new EncoderKEMP2();
        }
        return uniqueInstance;
    }
    
    // other useful methods here
    @Override
    protected Object encode(ChannelHandlerContext chc, Channel chnl, Object o) throws Exception {
        Message m = (Message) o;
        /*TODO change to actual KEMP encode*/
        return m.toString();
    }
}