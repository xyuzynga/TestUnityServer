package com.kakapo.unity.message.server;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.server.Status;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatusListMessage extends Message
{
  public static final String COMMAND = "StatusList";
  private List<ExtensionStatus> statuses = new ArrayList<ExtensionStatus>();

  public StatusListMessage()
  {
    super("StatusList");
  }

  public void addStatus(String extension, Status status)
  {
    this.statuses.add(new ExtensionStatus(extension, status));
  }

  public void addStatus(String extension, String status, Date since)
  {
    this.statuses.add(new ExtensionStatus(extension, status, since));
  }

  public List<ExtensionStatus> getStatuses()
  {
    return this.statuses;
  }
}