package com.kakapo.unity.message.server.error;

public class OverrideMessage extends ErrorMessage
{
  public OverrideMessage()
  {
    super(7, "OVERRIDE", true, true);
  }
}