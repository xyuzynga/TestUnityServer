package com.kakapo.unity.message.server;

public class OverrideMessage extends ErrorMessage
{
  public OverrideMessage()
  {
    super(6, "OVERRIDE", true, true);
  }
}