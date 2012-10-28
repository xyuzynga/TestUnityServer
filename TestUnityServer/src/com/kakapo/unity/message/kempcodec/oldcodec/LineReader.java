package com.kakapo.unity.message.kempcodec.oldcodec;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LineReader
        implements Iterator<CharSequence> {

    private static final Pattern pattern = Pattern.compile("(.*)[\\r]?[\\n]?");
    private CharSequence _source;
    private CharSequence _next;
    private Matcher _matcher;
    private int _position;

    public LineReader(CharSequence source) {
        this._source = source;
        this._matcher = pattern.matcher(source);
    }

    public boolean hasNext() {
        if (this._next == null) {
            this._next = doNext();
        }

        return this._next != null;
    }

    public CharSequence next() {
        if (hasNext()) {
            CharSequence result = this._next;
            this._next = null;
            return result;
        }

        throw new NoSuchElementException();
    }

    private CharSequence doNext() {
        if (this._position == this._source.length()) {
            return null;
        }

        if (this._matcher.find(this._position)) {
            CharSequence result = new OffsetCharSequence(this._source, this._matcher.start(1), this._matcher.end(1));
            this._position = this._matcher.end();

            return result;
        }

        return null;
    }

    public void remove() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void position(int position) {
        this._position = position;
    }

    public int position() {
        return this._position;
    }

    public class OffsetCharSequence implements CharSequence {

        private final CharSequence _sequence;
        private final int _start;
        private final int _end;

        public OffsetCharSequence(CharSequence sequence, int start, int end) {
            this._sequence = sequence;
            this._start = start;
            this._end = end;
        }

        public char charAt(int index) {
            if (index > this._sequence.length() + this._end) {
                throw new IndexOutOfBoundsException();
            }
            return this._sequence.charAt(index + this._start);
        }

        public int length() {
            return this._end - this._start;
        }

        public CharSequence subSequence(int start, int end) {
            return new OffsetCharSequence(this, start, end);
        }

        public String toString() {
            return new StringBuffer(this).toString();
        }
    }
}