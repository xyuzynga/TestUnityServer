package com.kakapo.unity.network;

public class OffsetCharSequence implements CharSequence {

    private final CharSequence _sequence;
    private final int _start;
    private final int _end;

    public OffsetCharSequence(CharSequence sequence, int start, int end) {
        this._sequence = sequence;
        this._start = start;
        this._end = end;
    }

    @Override
    public char charAt(int index) {
        if (index > this._sequence.length() + this._end) {
            throw new IndexOutOfBoundsException();
        }
        return this._sequence.charAt(index + this._start);
    }

    @Override
    public int length() {
        return this._end - this._start;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new OffsetCharSequence(this, start, end);
    }

    @Override
    public String toString() {
        return new StringBuffer(this).toString();
    }
}