package com.kakapo.unity.message.peer;

import java.util.Set;

public class TextMessage2 extends PeerMessage {

    public final String COMMAND = "Message2";
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

    public String getCOMMAND() {
        return COMMAND;
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
}