package com.kakapo.unity.server;

import com.kakapo.unity.client.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Group
{
  private final String name;
  private Map<String, ConnectedClient> extensions = new HashMap<String, ConnectedClient>();

  public Group(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return this.name;
  }

  public boolean containsExtension(String extension)
  {
    return this.extensions.containsKey(extension);
  }

  public ConnectedClient removeExtension(String extension)
  {
    return this.extensions.remove(extension);
  }

  public ConnectedClient getExtension(String extension)
  {
    return this.extensions.get(extension);
  }

  public Collection<ConnectedClient> getExtensions()
  {
    return Collections.unmodifiableCollection(this.extensions.values());
  }

  public ConnectedClient addClient(String key, Connection client)
  {
    ConnectedClient extension = this.extensions.get(key);
    if (extension == null)
    {
      extension = new ConnectedClient(key, client);
      this.extensions.put(key, extension);
    }
    else
    {
      extension.setClient(client);
    }
    return extension;
  }

  public boolean isEmpty()
  {
    return this.extensions.isEmpty();
  }
}