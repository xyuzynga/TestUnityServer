package com.kakapo.unity.message.client;

import com.kakapo.unity.message.ExtensionStatus;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.Status;
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
  
   @Override
    public String toString() {
        return "Command: StatusList\n\n";
    }
}