package com.kakapo.unity.message.kempcodec.oldcodec;

import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;

public class AppendableWriter extends Writer
{
  private final Appendable _wrapped;

  public AppendableWriter(Appendable wrapped)
  {
    this._wrapped = wrapped;
  }

  public void close()
    throws IOException
  {
  }

  public void flush()
    throws IOException
  {
  }

  public void write(char[] cbuf, int off, int len)
    throws IOException
  {
    this._wrapped.append(CharBuffer.wrap(cbuf), off, off + len);
  }
}