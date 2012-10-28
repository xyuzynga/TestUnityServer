package com.kakapo.unity.message.client;

import com.kakapo.unity.server.ScheduledStatus;
import com.kakapo.unity.message.Message;
import java.util.Collection;

public class ScheduledStatusListMessage extends Message
{
  public static final String COMMAND = "ScheduledStatusList";
  private final Collection<ScheduledStatus> statuses;

  public ScheduledStatusListMessage(Collection<ScheduledStatus> statuses)
  {
    super("ScheduledStatusList");
    this.statuses = statuses;
  }

  public Collection<ScheduledStatus> getStatuses()
  {
    return this.statuses;
  }
}