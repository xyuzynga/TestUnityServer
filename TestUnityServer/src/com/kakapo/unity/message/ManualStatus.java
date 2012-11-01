package com.kakapo.unity.message;

import java.util.Date;

public class ManualStatus extends Status
{
  private final boolean override;

  public ManualStatus(String name, boolean override)
  {
    super(name, new Date());
    this.override = override;
  }

  public boolean isOverride()
  {
    return this.override;
  }
}