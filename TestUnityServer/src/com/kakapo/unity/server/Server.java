package com.kakapo.unity.server;

import com.kakapo.unity.client.Connection;
import com.kakapo.unity.message.client.ClientMessage;

public abstract interface Server
{
  public abstract void register(Connection paramClient, boolean paramBoolean);

  public abstract void unregister(Connection paramClient);

  public abstract void send(Connection paramClient, ClientMessage paramClientMessage);
}