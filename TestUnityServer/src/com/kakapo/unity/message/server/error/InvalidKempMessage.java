package com.kakapo.unity.message.server.error;

public class InvalidKempMessage extends ErrorMessage
{
  public InvalidKempMessage()
  {
    super(4, "INVALID_KEMP_FORMAT", true, true);
  }
}