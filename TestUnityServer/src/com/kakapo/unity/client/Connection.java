package com.kakapo.unity.client;

import com.kakapo.unity.message.Message;

public abstract interface Connection
{
  public abstract void receive(Message paramMessage);

  public abstract String getGroup();

  public abstract String getExtension();
}