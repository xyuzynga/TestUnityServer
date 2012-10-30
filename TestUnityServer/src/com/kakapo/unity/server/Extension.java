package com.kakapo.unity.server;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.kakapo.unity.client.Client;
import com.kakapo.unity.util.Objects;

public class Extension
{
  private final String extension;
  private transient Client client;
  private ManualStatus manual;
  private ScheduledStatus scheduled;
  private Map<String, ScheduledStatus> idToScheduledStatus = new HashMap<String, ScheduledStatus>();
  private TreeSet<ScheduledStatus> orderedStatuses = new TreeSet<ScheduledStatus>(new Comparator<ScheduledStatus>()
  {
    public int compare(ScheduledStatus o1, ScheduledStatus o2)
    {
      return o1.getStart().compareTo(o2.getStart());
    }
  });

  public Extension(String extension, Client client)
  {
    this.extension = extension;
    this.client = client;
  }

  public String getKey()
  {
    return this.extension;
  }

  public Client getClient()
  {
    return this.client;
  }

  public ManualStatus getManualStatus()
  {
    return this.manual;
  }

  public Collection<ScheduledStatus> getScheduledStatuses()
  {
    return this.idToScheduledStatus.values();
  }

  public void setClient(Client client)
  {
    this.client = client;
  }

  public void setManualStatus(ManualStatus status)
  {
    this.manual = status;
  }

  public void addScheduledStatus(ScheduledStatus scheduledStatus)
    throws InvalidMessageException
  {
    ScheduledStatus lower = this.orderedStatuses.floor(scheduledStatus);
    ScheduledStatus higher = this.orderedStatuses.ceiling(scheduledStatus);
    if ((lower != null) && (!lower.getEnd().before(scheduledStatus.getStart())))
    {
      throw new OverlappingScheduleException(lower, scheduledStatus);
    }
    if ((higher != null) && (!higher.getStart().after(scheduledStatus.getEnd())))
    {
      throw new OverlappingScheduleException(higher, scheduledStatus);
    }

    if (!scheduledStatus.getEnd().after(scheduledStatus.getStart()))
    {
      throw new InvalidMessageException("Start must be before End");
    }

    this.idToScheduledStatus.put(scheduledStatus.getId(), scheduledStatus);
    this.orderedStatuses.add(scheduledStatus);
  }

  public ScheduledStatus removeScheduledStatus(String id)
  {
    ScheduledStatus remove = this.idToScheduledStatus.remove(id);
    if (remove != null)
    {
      this.orderedStatuses.remove(remove);
    }
    return remove;
  }

  public void setCurrentScheduledStatus(ScheduledStatus scheduled)
  {
    this.scheduled = scheduled;
  }

  public ScheduledStatus getCurrentScheduledStatus()
  {
    return this.scheduled;
  }

  public Status getCurrentStatus()
  {
    if (this.manual != null)
    {
      if ((this.manual.isOverride()) || (this.scheduled == null) || (this.manual.getStart().after(this.scheduled.getStart())))
      {
        return this.manual;
      }
    }
    return this.scheduled;
  }

  public String toString()
  {
    return Objects.toString(this);
  }
}