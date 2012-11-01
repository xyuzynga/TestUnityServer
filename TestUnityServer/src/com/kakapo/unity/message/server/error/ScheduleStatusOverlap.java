package com.kakapo.unity.message.server.error;

public class ScheduleStatusOverlap extends ErrorMessage
{
  public ScheduleStatusOverlap()
  {
    super(3, "SCHEDULE_STATUS_OVERLAP", true, false);
  }
}