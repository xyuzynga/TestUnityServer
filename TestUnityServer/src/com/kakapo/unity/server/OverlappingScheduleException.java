package com.kakapo.unity.server;

public class OverlappingScheduleException extends InvalidMessageException
{
	private static final long serialVersionUID = 1L;
	private final ScheduledStatus existing;
	private final ScheduledStatus added;

	public OverlappingScheduleException(ScheduledStatus ss1, ScheduledStatus ss2)
	{
		this.existing = ss1;
		this.added = ss2;
	}

	public String getMessage()
	{
		return "Scheduled status " + this.added + " overlaps with existing " + this.existing;
	}
}