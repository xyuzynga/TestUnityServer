package com.kakapo.unity.message;

import com.kakapo.unity.util.Objects;
import java.util.Date;

public class ExtensionStatus
{
  private final String extension;
  private final String status;
  private final Date since;

  public ExtensionStatus(String extension, String status, Date since)
  {
    this.extension = extension;
    this.status = status;
    this.since = since;
  }

  public ExtensionStatus(String extension, Status status)
  {
    this(extension, status == null ? null : status.getName(), status == null ? null : status.getStart());
  }

  public String getStatus()
  {
    return this.status;
  }

  public String getExtension()
  {
    return this.extension;
  }

  public Date getSince()
  {
    return this.since;
  }

    @Override
  public String toString()
  {
    return Objects.toString(this);
  }
}