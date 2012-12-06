package com.kakapo.unity.message.server.error.Legacy;

import com.kakapo.unity.message.server.error.ErrorMessage;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class RedirectErrorMessage extends ErrorMessage {

    private final String _redirect;

    public RedirectErrorMessage(int number, String key, boolean alert, boolean disconnect, String redirect) {
        super(number, key, alert, disconnect);
        this._redirect = redirect;
    }

    public String getRedirect() {
        return this._redirect;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: Error\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Number: "+super.getNumber()+"\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Description: The Unity Server has reached its maximum connection count.  Redirect to the included host\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("AlertUser: "+super.isAlert()+"\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("RedirectHostName: "+_redirect+"\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}