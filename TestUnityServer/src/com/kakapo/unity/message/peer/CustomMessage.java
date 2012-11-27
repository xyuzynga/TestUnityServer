package com.kakapo.unity.message.peer;

import java.util.Set;

public class CustomMessage extends PeerMessage {

    public final String COMMAND = "Custom";
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