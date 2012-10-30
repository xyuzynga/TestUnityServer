package com.kakapo.unity.server;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ExclusionIterator<T>
  implements Iterator<T>
{
  private Iterator<T> _delegate;
  private final T _exclude;
  private T _next;

  public ExclusionIterator(Iterator<T> delegate, T exclude)
  {
    this._delegate = delegate;
    this._exclude = exclude;
  }

  public boolean hasNext()
  {
    if (this._next != null)
    {
      return true;
    }

    this._next = doNext();

    return this._next != null;
  }

  private T doNext()
  {
    if (this._delegate.hasNext())
    {
      T result = this._delegate.next();
      if (exclude(result))
      {
        return doNext();
      }

      return result;
    }

    return null;
  }

  public T next()
  {
    if (hasNext())
    {
      T result = this._next;
      this._next = null;
      return result;
    }

    throw new NoSuchElementException();
  }

  public void remove()
  {
    throw new UnsupportedOperationException("Not implemented");
  }

  protected boolean exclude(T item)
  {
    return this._exclude.equals(item);
  }
}