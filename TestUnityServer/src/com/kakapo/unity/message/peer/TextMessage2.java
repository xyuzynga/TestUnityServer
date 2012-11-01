package com.kakapo.unity.message.peer;

import java.util.Set;

public class TextMessage2 extends PeerMessage
{
  public static final String COMMAND = "Message2";
  private final CharSequence _text;
  private final CharSequence _dateTime;

  public TextMessage2(CharSequence text, Set<String> extensions, CharSequence input, CharSequence id, CharSequence sender, CharSequence share) {
        super("Message2", id , sender, extensions, input);
        this._dateTime = share;
        this._text = text;
    }

    public static String getCOMMAND() {
        return COMMAND;
    }

    public CharSequence getText() {
        return _text;
    }

    public CharSequence getDateTime() {
        return _dateTime;
    }

 
}