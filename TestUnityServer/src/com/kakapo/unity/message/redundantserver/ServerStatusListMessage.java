package com.kakapo.unity.message.redundantserver;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.server.*;
import com.kakapo.unity.client.Status;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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