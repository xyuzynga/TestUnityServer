package com.kakapo.unity.server;

import com.kakapo.unity.client.Client;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Group
{
  private final String name;
  private Map<String, Extension> extensions = new HashMap<String, Extension>();

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

  public Extension removeExtension(String extension)
  {
    return this.extensions.remove(extension);
  }

  public Extension getExtension(String extension)
  {
    return this.extensions.get(extension);
  }

  public Collection<Extension> getExtensions()
  {
    return Collections.unmodifiableCollection(this.extensions.values());
  }

  public Extension addClient(String key, Client client)
  {
    Extension extension = this.extensions.get(key);
    if (extension == null)
    {
      extension = new Extension(key, client);
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