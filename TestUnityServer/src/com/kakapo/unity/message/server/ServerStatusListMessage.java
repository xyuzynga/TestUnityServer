package com.kakapo.unity.message.server;

import com.kakapo.unity.message.ExtensionStatus;
import com.kakapo.unity.message.Message;

public class ServerStatusListMessage extends Message
{
  public static final String COMMAND = "StatusList";
  private final ExtensionStatus _status;

  public ServerStatusListMessage(ExtensionStatus status)
  {
    super("StatusList");
    this._status = status;
  }

}