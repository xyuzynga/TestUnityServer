package com.kakapo.unity.message.server.error;

public class MaximumAttemptsMessage extends ErrorMessage
{
  public MaximumAttemptsMessage()
  {
    super(6, "EXCEEDED_MAXIMUM_UNSUCCESSFUL_REGISTRATION_ATTEMPTS", true, true);
  }
}