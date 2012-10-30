package com.kakapo.unity.message.server;

public class ExtensionInUseMessage extends ErrorMessage
{
  public ExtensionInUseMessage()
  {
    super(5, "EXTENSION_IN_USE", false, true);
  }
}