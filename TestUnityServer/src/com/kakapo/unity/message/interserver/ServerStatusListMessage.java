package com.kakapo.unity.message.interserver;

import com.kakapo.unity.message.ExtensionStatus;
import java.util.Date;

public class ServerStatusListMessage extends ServerMessage
{
  public static final String COMMAND = "StatusList";
  private final String group;
  private final ExtensionStatus _status;

  public ServerStatusListMessage(String group, ExtensionStatus status)
  {
    super("StatusList");
    this.group = group;
    this._status = status;
  }

  public ServerStatusListMessage(String group, String extension, String status, Date since)
  {
    super("StatusList");
    this.group = group;
    this._status = new ExtensionStatus(extension, status, since);
  }
}