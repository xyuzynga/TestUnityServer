package com.kakapo.unity.message.server;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SingleItemIterator<T>
  implements Iterator<T>
{
  private T _item;
  private boolean _done;

  public SingleItemIterator(T item)
  {
    this._item = item;
  }

  public boolean hasNext()
  {
    return !this._done;
  }

  public T next()
  {
    if (this._done)
    {
      throw new NoSuchElementException();
    }

    this._done = true;
    return this._item;
  }

  public void remove()
  {
    throw new UnsupportedOperationException("Not implemented");
  }
}