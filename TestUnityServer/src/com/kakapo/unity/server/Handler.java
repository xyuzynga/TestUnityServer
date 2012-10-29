package com.kakapo.unity.server;

import com.kakapo.unity.message.KeepAliveMessage;
import com.kakapo.unity.message.Message;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

public class Handler extends IdleStateAwareChannelHandler {

    private static final int messagesReceived = 0;
    static DefaultChannelGroup group = new DefaultChannelGroup();
    static int i = 0;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Message m = (Message) e.getMessage();
        System.out.println(m);
        group.write(m);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        i++;
        if (i % 2 == 0) {
            ctx.getChannel().getPipeline().remove("messageencoder");
            ctx.getChannel().getPipeline().addAfter("Stringencoder", "mmencoder", new mencoder1());
        }
        group.add(e.getChannel());
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        if (e.getState() == IdleState.WRITER_IDLE) {
            e.getChannel().write(new KeepAliveMessage());
        }
    }
}
