package com.kakapo.unity.message.server.error;

public class ShutDownMessage extends ErrorMessage
{
  public ShutDownMessage()
  {
    super(8, "SHUTDOWN", true, true);
  }
}