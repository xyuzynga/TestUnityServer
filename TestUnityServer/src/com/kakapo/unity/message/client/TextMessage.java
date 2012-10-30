package com.kakapo.unity.message.client;

import com.kakapo.unity.message.server.SingleItemSet;
import java.util.Set;

public class TextMessage extends PeerMessage
{
  public static final String COMMAND = "Message";
  private final CharSequence _text;
  private final boolean _share;

  public TextMessage(CharSequence text, Set<String> extensions, CharSequence input, CharSequence id, CharSequence sender, boolean share)
  {
    super("Message", extensions, input, id, sender);
    this._text = text;
    this._share = share;
  }

  public TextMessage(CharSequence text, String extension, CharSequence input, CharSequence id, CharSequence sender, boolean share)
  {
    this(text, new SingleItemSet(extension), input, id, sender, share);
  }

  public CharSequence getText()
  {
    return this._text;
  }

  public String toString()
  {
    return super.toString() + " \"" + this._text + "\"";
  }

  public boolean isShare()
  {
    return this._share;
  }
}