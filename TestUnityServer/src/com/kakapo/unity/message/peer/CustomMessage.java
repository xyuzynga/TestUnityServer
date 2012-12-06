package com.kakapo.unity.message.peer;

import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class CustomMessage extends PeerMessage {

    public final String _Command = "Custom";
    private final CharSequence _text;
    private final CharSequence _dateTime;
    private int _length;

    public CustomMessage(CharSequence text, Set<String> extensions, CharSequence input, CharSequence id, CharSequence sender, CharSequence share) {
        super("Custom", id, sender, extensions, input);
        this._dateTime = share;
        this._text = text;
//        this._length = text.length();
    }

    public CustomMessage(CharSequence text, Set<String> extensions, CharSequence input, CharSequence id, CharSequence sender, CharSequence share, int length) {
        super("Custom", id, sender, extensions, input);
        this._dateTime = share;
        this._text = text;
        this._length = length;
    }

    public String get_Command() {
        return _Command;
    }

    public CharSequence getText() {
        return _text;
    }

    public CharSequence getDateTime() {
        return _dateTime;
    }

    public int getLength() {
        return _length;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: Custom\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Id: " + super.getId() + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Sender: " + super.getSender() + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("DateTime: " + _dateTime + "\n").getBytes(CharsetUtil.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (String extension : super.getExtensions()) {
            builder.append(extension);
            builder.append(", ");
        }
        encodedMessage.writeBytes(("Extensions: " + builder + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Length: " + _length + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes((_text + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}