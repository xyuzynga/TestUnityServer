package com.kakapo.unity.server;

import com.kakapo.unity.message.kempcodec.WrapperKempCodec;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

public class ServerMain {

    public static ConcurrentHashMap<String, Integer> connectedChannel_channelIDMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, DefaultChannelGroup> groups = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        // TODO-GK Server startup & code associated with Netty

        final Timer timer = new HashedWheelTimer();
        final IdleStateHandler idleHandler = new IdleStateHandler(timer, 60, 60, 60);
        final DelimiterBasedFrameDecoder frameDecoder = new DelimiterBasedFrameDecoder(1024, ChannelBuffers.copiedBuffer("\n\n".getBytes()));
        final StringDecoder strDecoder = new StringDecoder(CharsetUtil.UTF_8);
        final StringEncoder strEncoder = new StringEncoder(CharsetUtil.UTF_8);
        final ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        ServerBootstrap bootstrap = new ServerBootstrap(factory);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();


                pipeline.addLast("idleHandler", idleHandler);

                //<editor-fold defaultstate="collapsed" desc="Ref - Order of adding handlers to pipiline">
                /*
                 * +----------------------------------------+---------------+
                 * |                  ChannelPipeline       |               |
                 * |                                       \|/              |
                 * |  +----------------------+  +-----------+------------+  |
                 * |  | Upstream Handler  N  |  | Downstream Handler  1  |  |
                 * |  +----------+-----------+  +-----------+------------+  |
                 * |            /|\                         |               |
                 * |             |                         \|/              |
                 * |  +----------+-----------+  +-----------+------------+  |
                 * |  | Upstream Handler N-1 |  | Downstream Handler  2  |  |
                 * |  +----------+-----------+  +-----------+------------+  |
                 * |            /|\                         .               |
                 * |             .                          .               |
                 * |     [ sendUpstream() ]        [ sendDownstream() ]     |
                 * |     [ + INBOUND data ]        [ + OUTBOUND data  ]     |
                 * |             .                          .               |
                 * |             .                         \|/              |
                 * |  +----------+-----------+  +-----------+------------+  |
                 * |  | Upstream Handler  2  |  | Downstream Handler M-1 |  |
                 * |  +----------+-----------+  +-----------+------------+  |
                 * |            /|\                         |               |
                 * |             |                         \|/              |
                 * |  +----------+-----------+  +-----------+------------+  |
                 * |  | Upstream Handler  1  |  | Downstream Handler  M  |  |
                 * |  +----------+-----------+  +-----------+------------+  |
                 * |            /|\                         |               |
                 * +-------------+--------------------------+---------------+
                 * |                         \|/
                 * +-------------+--------------------------+---------------+
                 * |             |                          |               |
                 * |     [ Socket.read() ]          [ Socket.write() ]      |
                 * |                                                        |
                 * |  Netty Internal I/O Threads (Transport Implementation) |
                 * +--------------------------------------------------------+
                 */
                //</editor-fold>

                pipeline.addLast("frameDecoder", frameDecoder);
                pipeline.addLast("stringDecoder", strDecoder);
                pipeline.addLast("messageDecoder", MessageDecoder.getInstance());

                pipeline.addLast("StringEncoder", strEncoder);
                pipeline.addLast("messageEncoder", MessageEncoder.getInstance());
                pipeline.addLast("myHandler", new Handler());
                return pipeline;
            }
        });

        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                processScheduledStatuses();
            }
        }, 1000, TimeUnit.MILLISECONDS);

        bootstrap.bind(new InetSocketAddress(8080));
    }

    private static void processScheduledStatuses() {
        /*TODO-Prijo CODE for processing scheduled statuses*/
    }

    private static void processDisconnection() {
        /*TODO-Abin CODE for processing disconnection events (Server & Client)*/
    }

    /**
     * All the Server to Server messages
     */
    public static enum ServerCommandMessage {

        KeepAlive,
        //From client to server
        //From server to server
        //Used to keep the TCP connection active, should be ignored by the server

        ServerRegister,
        //From server to server
        //Used to register one server with another server.

        ServerContactList,
        //From server to server
        //Used to inform other servers [which then inform connected clients] that a user has registered or deregistered on the server.

        ServerMessage,
        //From server to server
        //Used to relay instant messages between clients connected to separate servers

        ServerStatusList,
        //From server to server
        //Used to inform other servers [which then inform connected clients] that the scheduled status for a connected client has changed.

        ServerSetStatus,
        //From server to server
        //Used to inform other servers [which then inform connected clients] that a connected client has set their ad-hoc status.
    }

    /**
     * All the Client to Server messages
     */
    public static enum ClientCommandMessage {

        Register,
        //From client to server
        //Used to register a connection with user details such as user id and group name.

        KeepAlive,
        //From client to server
        //From server to server
        //Used to keep the TCP connection active, should be ignored by the server

        Message,
        //From client to server
        //From server to client
        //Send an instant message to one or more users

        Custom,
        //From client to server
        //From server to client
        //Send an instant message to one or more users

        SetStatus,
        //From client to server
        //Set the ad-hoc status for a specified user

        AddScheduledStatus,
        //From client to server
        //Add a scheduled status for a specified user

        ClearStatus,
        //From client to server
        //Clear an ad-hoc or scheduled status for a specified user

        ListScheduledStatuses,
        //From client to server
        //Requests all scheduled statuses for the specified user and date range

        Register2,
        //From client to server
        //Used by clients using the new command set to register on the server

        Message2;
        //From client to server
        //From server to client
        //Used by clients using the new command
        //set to send instant messages, which are
        //then relayed through all servers.
    }

    /**
     * All the Server to Client messages
     */
    public static enum ServerResponseMessage {

        ContactList,
        //From server to client
        //Returns the “online” status of one or more users

        StatusList,
        //From server to client
        //Returns the current presence status of one of more users.

        Message,
        //From client to server
        //From server to client
        //Send an instant message to one or more users

        ScheduledStatusList,
        //From server to client
        //Response to the ListScheduledStatuses command. Lists all scheduled statuses that meet the params given in the command.

        Error,
        //From server to client
        //Used to inform a client that an error has occurred.

        ContactList2,
        //From server to client
        //Used to inform connected clients using
        //the new command set when another user
        //registers or deregisters.

        Message2;
        //From client to server
        //From server to client
        //Used by clients using the new command
        //set to send instant messages, which are
        //then relayed through all servers.
    }
}
