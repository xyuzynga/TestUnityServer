package com.kakapo.unity.server;

import com.kakapo.unity.message.ContactAction;
import com.kakapo.unity.message.KeepAliveMessage;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.client.ClientMessage;
import com.kakapo.unity.message.client.ContactListMessage;
import com.kakapo.unity.message.client.StatusListMessage;
import com.kakapo.unity.message.server.ServerMessage;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

public class Handler extends IdleStateAwareChannelHandler {

    static DefaultChannelGroup group = new DefaultChannelGroup();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ServerMain.totalMessagesReceived.incrementAndGet();
        Message m = (Message) e.getMessage();

        if (m instanceof ClientMessage) {
            System.out.println("\nClient message --> \n " + m);
            processClientMessage(m, ctx);
        } else if (m instanceof ServerMessage) {
            System.out.println("\nServer message --> \n " + m);
            processServerMessage(m, ctx);
        }
        
        group.write(m);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
       ServerMain.totalConnections.incrementAndGet();
        if (ServerMain.totalConnections.intValue()%100 == 0) {
            System.out.println("Crossed another century connections" + ServerMain.totalConnections.intValue());
        }
        
//        if (i % 2 == 0) {
//            ctx.getChannel().getPipeline().remove("messageEncoder");
//            ctx.getChannel().getPipeline().addAfter("StringEncoder", "msgEncdr", new MsgEncdr());
//        }
        
        group.add(e.getChannel());
        
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelClosed(ctx, e);
        ServerMain.totalConnections.decrementAndGet();
    }
    

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
        if (e.getState() == IdleState.WRITER_IDLE) {
            e.getChannel().write(new KeepAliveMessage());
        }
    }

    private void processClientMessage(Message clientCommandMessage, ChannelHandlerContext ctx) {
        String current = clientCommandMessage.getCommand();
        ServerMain.ClientCommandMessage currentClientCommandMessage = ServerMain.ClientCommandMessage.valueOf(current);

        switch (currentClientCommandMessage) {
            case Register:

                /*TODO-GK Process Register message */

                //put the login id and channel id into the hashmap ServerMain.connectedChannel_channelIDMap

                //check if group exists in hashmap ServerMain.groups if not create a new channelGroup 
                //and add it to the hashmap with name of the group as key,then add the channel to the corresponding groups channelgroup.
                //channel.write(contactlist),channel.write(statuslist),channelgroup.write(contactlist),channelgroup.write(statuslist)
                
                ctx.getChannel().write(new ContactListMessage(new ContactAction(ContactAction.Action.ADD, "felix@drd.co.uk")));
                
                ctx.getChannel().write(new StatusListMessage());
                
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        super.exceptionCaught(ctx, e);
        System.out.println(e.getCause());
    }

    private void processServerMessage(Message severCommandMessage, ChannelHandlerContext ctx) {
        String current = severCommandMessage.getCommand();
        ServerMain.ServerCommandMessage currentSeverCommandMessage = ServerMain.ServerCommandMessage.valueOf(current);

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