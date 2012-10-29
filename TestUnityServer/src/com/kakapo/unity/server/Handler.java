package com.kakapo.unity.server;

import com.kakapo.unity.message.KeepAliveMessage;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.client.ClientMessage;
import com.kakapo.unity.message.server.ServerMessage;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

public class Handler extends IdleStateAwareChannelHandler {

    private static Handler uniqueInstance;
// other useful instance variables here
    private static final int messagesReceived = 0;
    static DefaultChannelGroup group = new DefaultChannelGroup();
    static int i = 0;

    private Handler() {
    }

    public static Handler getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new Handler();
        }
        return uniqueInstance;
    }
// other useful methods here

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Message m = (Message) e.getMessage();

        if (m instanceof ClientMessage) {
            processClientMessage(m);
        } else if (m instanceof ServerMessage) {
            processServerMessage(m);
        }

        System.out.println(m);
        group.write(m);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        i++;
        if (i % 2 == 0) {
            ctx.getChannel().getPipeline().remove("messageEncoder");
            ctx.getChannel().getPipeline().addAfter("StringEncoder", "msgEncdr", new MsgEncdr());
        }
        group.add(e.getChannel());
    }

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        if (e.getState() == IdleState.WRITER_IDLE) {
            e.getChannel().write(new KeepAliveMessage());
        }
    }

    private void processClientMessage(Message clientCommandMessage) {
        String current = clientCommandMessage.getCommand();
        Main.ClientCommandMessage currentClientCommandMessage = Main.ClientCommandMessage.valueOf(current);

        switch (currentClientCommandMessage) {
            case Register:

                /*TODO-GK Process Register message */

                break;
            case KeepAlive:

                /*TODO-GK Process KeepAlive message */

                break;
            case Message:

                /*TODO-GK Process Message message*/

                break;
            case Custom:

                /*TODO-GK Process Custom message */

                break;
            case SetStatus:

                /*TODO-GK Process SetStatus message */

                break;
            case AddScheduledStatus:

                /*TODO-GK Process AddScheduledStatus message */

                break;
            case ClearStatus:

                /*TODO-GK Process ClearStatus message */

                break;
            case ListScheduledStatuses:

                /*TODO-GK Process ListScheduledStatuses message */

                break;
            case Register2:

                /*TODO-GK Process Register2 message */

                break;
            case Message2:

                /*TODO-GK Process Message2 message */

                break;
            default:
                throw new AssertionError();
        }
    }

    public void processServerMessage(Message severCommandMessage) {
        String current = severCommandMessage.getCommand();
        Main.ServerCommandMessage currentSeverCommandMessage = Main.ServerCommandMessage.valueOf(current);

        switch (currentSeverCommandMessage) {
            case KeepAlive:

                /*TODO-Amith Process KeepAlive message*/

                break;
            case ServerRegister:

                /*TODO-Amith Process ServerRegister message*/

                break;
            case ServerContactList:

                /*TODO-Amith Process ServerContactList message*/

                break;
            case ServerMessage:

                /*TODO-Amith Process ServerMessage message*/

                break;
            case ServerStatusList:

                /*TODO-Amith Process ServerStatusList message*/

                break;
            case ServerSetStatus:

                /*TODO-Amith Process ServerSetStatus message*/

                break;
            default:
                throw new AssertionError();
        }

    }
}