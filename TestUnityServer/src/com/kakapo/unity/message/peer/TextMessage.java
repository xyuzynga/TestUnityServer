package com.kakapo.unity.message.peer;

import com.kakapo.unity.message.server.SingleItemSet;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class TextMessage extends PeerMessage {

    public final String _Command = "Message";
    private final CharSequence _text;
    private final boolean _share;
    private int _length;

    public TextMessage(CharSequence text, Set<String> extensions, CharSequence input, CharSequence id, CharSequence sender, boolean share) {
        super("Message", id, sender, extensions, input);
        this._share = share;
        this._text = text;
//        this._length = text.length();
    }

    public TextMessage(CharSequence text, String extension, CharSequence input, CharSequence id, CharSequence sender, boolean share) {
        this(text, new SingleItemSet(extension), input, id, sender, share);
//        this._length = _text.length();
    }

    public TextMessage(CharSequence text, Set<String> extensions, CharSequence input, CharSequence id, CharSequence sender, boolean share, int length) {
        super("Message", id, sender, extensions, input);
        this._share = share;
        this._text = text;
        this._length = length;
    }

    public TextMessage(CharSequence text, String extension, CharSequence input, CharSequence id, CharSequence sender, boolean share, int length) {
        this(text, new SingleItemSet(extension), input, id, sender, share, length);
    }

    public CharSequence getText() {
        return this._text;
    }

    public boolean isShare() {
        return this._share;
    }

    public int getLength() {
        return _length;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: Message\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Id: " + super.getId() + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Sender: " + super.getSender() + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Share: " + _share + "\n").getBytes(CharsetUtil.UTF_8));
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