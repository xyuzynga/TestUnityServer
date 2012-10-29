package com.kakapo.unity.message.server;

import com.kakapo.unity.message.ExtensionStatus;

public class ServerStatusListMessage extends ServerMessage
{
  public static final String COMMAND = "StatusList";
  private final ExtensionStatus _status;

  public ServerStatusListMessage(ExtensionStatus status)
  {
    super("StatusList");
    this._status = status;
  }

}