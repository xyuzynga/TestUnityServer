package com.kakapo.unity.server;

import com.kakapo.unity.client.Client;
import com.kakapo.unity.message.client.ClientMessage;

public abstract interface Server
{
  public abstract void register(Client paramClient, boolean paramBoolean);

  public abstract void unregister(Client paramClient);

  public abstract void send(Client paramClient, ClientMessage paramClientMessage);
}