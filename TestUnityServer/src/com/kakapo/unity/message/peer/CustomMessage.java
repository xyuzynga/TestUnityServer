package com.kakapo.unity.message.peer;

import java.util.Set;

public class CustomMessage extends PeerMessage
{
  public static final String COMMAND = "Custom";
  private CharSequence _content;

  public CustomMessage(CharSequence content, Set<String> extensions, CharSequence input, CharSequence id, CharSequence sender)
  {
    super("Custom", id , sender, extensions, input);
    this._content = content;
  }

  public CharSequence getContent()
  {
    return this._content;
  }
}