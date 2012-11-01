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
public class EncoderServerMessage extends OneToOneEncoder {

    private static EncoderServerMessage uniqueInstance;
    // other useful instance variables here
    
    private EncoderServerMessage() {
    }

    public static EncoderServerMessage getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new EncoderServerMessage();
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