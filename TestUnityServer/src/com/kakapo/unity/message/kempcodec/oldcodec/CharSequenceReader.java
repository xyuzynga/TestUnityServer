package com.kakapo.unity.message.kempcodec.oldcodec;

import java.io.IOException;
import java.io.Reader;

public class CharSequenceReader extends Reader
{
  private final CharSequence _input;
  private int _position;
  private boolean _done;
  private int _length;

  public CharSequenceReader(CharSequence input)
  {
    this._input = input;
    this._length = input.length();
  }

  public void close()
    throws IOException
  {
  }

  public void done()
  {
    this._done = true;
  }

  public int read()
    throws IOException
  {
    throw new UnsupportedOperationException("Not implemented");
  }

  public int read(char[] cbuf, int off, int len)
    throws IOException
  {
    int read = 0;
    while ((read < len) && (read + this._position < this._length))
    {
      cbuf[(off + read)] = this._input.charAt(off + read);
      read++;
    }
    this._position += read;

    return read;
  }
}