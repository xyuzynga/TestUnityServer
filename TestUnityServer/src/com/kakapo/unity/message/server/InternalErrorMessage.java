package com.kakapo.unity.message.server;

public class InternalErrorMessage extends ErrorMessage
{
  public InternalErrorMessage()
  {
    super(1, "INTERNAL_ERROR", false, false);
  }
}