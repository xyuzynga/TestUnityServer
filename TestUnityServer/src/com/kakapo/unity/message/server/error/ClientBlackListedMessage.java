package com.kakapo.unity.message.server.error;

public class ClientBlackListedMessage extends ErrorMessage
{
  public ClientBlackListedMessage()
  {
    super(2, "CLIENT_IS_BLACKLISTED", true, true);
  }
}