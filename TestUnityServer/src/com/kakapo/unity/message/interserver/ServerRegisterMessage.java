package com.kakapo.unity.message.interserver;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

/**
 * this class represents the messages sent from a foreign server to the unity
 * server to register itself
 *
 * @author amith.bharathan
 */
public class ServerRegisterMessage extends InterServerMessage {

    /**
     * This filed represents type of the message which is ServerRegister
     */
    public final String _Command = "ServerRegister";
    private final CharSequence serverName;

    /**
     * constructor to create an object of ServerRegisterMessage
     *
     * @param serverName - name of the foreign server
     */
    public ServerRegisterMessage(CharSequence serverName) {
        super("ServerRegister");
        this.serverName = serverName;
    }

    /**
     * getter for server name
     *
     * @return server name
     */
    public CharSequence getServerName() {
        return serverName;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: ServerRegister\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("ServerName: " + this.serverName + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}
