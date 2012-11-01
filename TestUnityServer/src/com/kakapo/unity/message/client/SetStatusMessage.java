package com.kakapo.unity.message.client;

import com.kakapo.unity.message.ManualStatus;

public class SetStatusMessage extends ClientMessage
{
  public static final String COMMAND = "SetStatus";
  private final ManualStatus status;
  private final String extension;

  public SetStatusMessage(String extension, String name, boolean override)
  {
    super("SetStatus");
    this.extension = extension;
    this.status = new ManualStatus(name, override);
  }

  public ManualStatus getStatus()
  {
    return this.status;
  }

  public String getExtension()
  {
    return this.extension;
  }
}