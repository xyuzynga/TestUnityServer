package com.kakapo.unity.message.server.error;

public class MaximumFrequencyMessage extends ErrorMessage
{
  public MaximumFrequencyMessage()
  {
    super(5, "EXCEEDED_MAXIMUM_MESSAGE_FREQUENCY", true, true);
  }
}