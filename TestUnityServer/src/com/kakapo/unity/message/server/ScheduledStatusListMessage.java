package com.kakapo.unity.message.server;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.client.ScheduledStatus;
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