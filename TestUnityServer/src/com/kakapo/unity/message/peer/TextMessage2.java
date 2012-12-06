package com.kakapo.unity.message.peer;

import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class TextMessage2 extends PeerMessage {

    public final String _Command = "Message2";
    private final CharSequence _text;
    private final CharSequence _dateTime;
    private int _length;

    public TextMessage2(CharSequence text, Set<String> extensions, CharSequence input, CharSequence id, CharSequence sender, CharSequence share) {
        super("Message2", id, sender, extensions, input);
        this._dateTime = share;
        this._text = text;
//        this._length = text.length();
    }

    public TextMessage2(CharSequence text, Set<String> extensions, CharSequence input, CharSequence id, CharSequence sender, CharSequence share, int length) {
        super("Message2", id, sender, extensions, input);
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
        encodedMessage.writeBytes("Command: Message2\n".getBytes(CharsetUtil.UTF_8));
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