package com.kakapo.unity.message.client;

public class RemoveExtensionMessage extends ClientMessage
{
  public static final String COMMAND = "RemoveExtension";
  private final String extension;

  public RemoveExtensionMessage(String extension)
  {
    super("RemoveExtension");
    this.extension = extension;
  }

  public String getExtension()
  {
    return this.extension;
  }
}