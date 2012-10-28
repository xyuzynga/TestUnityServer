package com.kakapo.unity.client;

import com.kakapo.unity.util.Objects;
import java.util.Date;

public abstract class Status
{
  private final String name;
  private final Date start;

  public Status(String name, Date start)
  {
    if ((name == null) || (name.length() == 0))
    {
      throw new IllegalArgumentException("Status name must not be empty");
    }
    this.name = name;
    this.start = start;
  }

  public Date getStart()
  {
    return this.start;
  }

  public String getName()
  {
    return this.name;
  }

  public String toString()
  {
    return Objects.toString(this);
  }
}