package com.kakapo.unity.message.server.error;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class InternalErrorMessage extends ErrorMessage {

    public InternalErrorMessage() {
        super(1, "INTERNAL_ERROR", false, false);
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: Error\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Number: 1\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Description: The Unity server has experienced an internal error and the requested operation could not be performed\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("AlertUser: " + super.isAlert() + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}