package com.kakapo.unity.message.client;

import com.kakapo.unity.client.ScheduledStatus;
import java.util.Date;

public class AddScheduledStatusMessage extends ClientMessage
{
  public static final String COMMAND = "AddScheduledStatus";
  private final ScheduledStatus scheduledStatus;
  private final String extension;

  public AddScheduledStatusMessage(String extension, String status, Date start, Date end, String id)
  {
    this(extension, new ScheduledStatus(status, start, end, id));
  }

  public AddScheduledStatusMessage(String extension, ScheduledStatus scheduledStatus)
  {
    super("AddScheduledStatus");
    this.extension = extension;
    this.scheduledStatus = scheduledStatus;
  }

  public ScheduledStatus getScheduledStatus()
  {
    return this.scheduledStatus;
  }

  public String getExtension()
  {
    return this.extension;
  }
}