package com.kakapo.unity.message.server.error;

import com.kakapo.unity.message.server.error.ErrorMessage;

public class InternalErrorMessage extends ErrorMessage
{
  public InternalErrorMessage()
  {
    super(1, "INTERNAL_ERROR", false, false);
  }
}