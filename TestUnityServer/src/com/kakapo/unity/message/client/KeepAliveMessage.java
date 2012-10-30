package com.kakapo.unity.message.client;

public class KeepAliveMessage extends ClientMessage
{
  public static final String COMMAND = "KeepAlive";

  public KeepAliveMessage()
  {
    super("KeepAlive");
  }
}