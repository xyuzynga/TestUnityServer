package com.kakapo.unity.message.server.error;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class OverrideMessage extends ErrorMessage {

    public OverrideMessage() {
        super(6, "OVERRIDE", true, true);
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: Error\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Number: 6\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Description: This registration is being terminated as it has been overridden by a new connection\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("AlertUser: " + super.isAlert() + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}