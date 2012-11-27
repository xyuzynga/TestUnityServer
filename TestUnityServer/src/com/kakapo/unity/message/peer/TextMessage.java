package com.kakapo.unity.message.peer;

import com.kakapo.unity.message.server.SingleItemSet;
import java.util.Set;

public class TextMessage extends PeerMessage {

    public final String COMMAND = "Message";
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

    @Override
    public String toString() {
        return super.toString() + " \"" + this._text + "\"";
    }

    public boolean isShare() {
        return this._share;
    }

    public int getLength() {
        return _length;
    }
}