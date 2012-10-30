package com.kakapo.unity.server;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

public class ExclusionSet<T> extends AbstractSet<T>
{
  private Collection<T> _delegate;
  private final T _exclude;

  public ExclusionSet(Collection<T> delegate, T exclude)
  {
    this._delegate = delegate;
    this._exclude = exclude;
  }

  public Iterator<T> iterator()
  {
    return new ExclusionIterator<T>(this._delegate.iterator(), this._exclude);
  }

  public int size()
  {
    return this._delegate.size() - 1;
  }
}