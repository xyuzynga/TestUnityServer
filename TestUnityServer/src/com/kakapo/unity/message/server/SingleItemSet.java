package com.kakapo.unity.message.server;

import java.util.AbstractSet;
import java.util.Iterator;

public class SingleItemSet<T> extends AbstractSet<T> {

    private T _item;

    public SingleItemSet(T item) {
        this._item = item;
    }

    @Override
    public Iterator<T> iterator() {
        return new SingleItemIterator<T>(this._item);
    }

    @Override
    public int size() {
        return 1;
    }
}