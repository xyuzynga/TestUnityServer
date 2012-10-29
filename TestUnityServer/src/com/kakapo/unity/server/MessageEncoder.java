/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.server;

import com.kakapo.unity.message.Message;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 *
 * @author felix.vincent
 */
public class MessageEncoder extends OneToOneEncoder {

    private static MessageEncoder uniqueInstance;
    // other useful instance variables here
    
    private MessageEncoder() {
    }

    public static MessageEncoder getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new MessageEncoder();
        }
        return uniqueInstance;
    }
    
    // other useful methods here
    @Override
    protected Object encode(ChannelHandlerContext chc, Channel chnl, Object o) throws Exception {
        Message m = (Message) o;
        return m.toString();
    }
}