package com.kakapo.unity.message.server.error.Legacy;

import com.kakapo.unity.message.server.error.ErrorMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class ExtensionInUseMessage extends ErrorMessage {

    public ExtensionInUseMessage() {
        super(5, "EXTENSION_IN_USE", false, true);
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: Error\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Number: 5\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Description: The register request failed as the extn is already in use\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}