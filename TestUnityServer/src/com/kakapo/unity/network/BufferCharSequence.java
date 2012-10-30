package com.kakapo.unity.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class BufferCharSequence implements CharSequence
{
	private LinkedList<BufferHolder> _bufferHolders = new LinkedList<BufferHolder>();
	private static Charset _charset = Charset.forName("UTF-8");
	private static CharsetDecoder _decoder = _charset.newDecoder();
	private static CharsetEncoder _encoder = _charset.newEncoder();

	public BufferCharSequence()
	{
	}

	BufferCharSequence(BufferCharSequence base, int start, int end)
	{
		Iterator<BufferHolder> holders = base._bufferHolders.iterator();

		BufferHolder previous = null;
		while (holders.hasNext())
		{
			BufferHolder original = (BufferHolder) holders.next();
			if ((original.total + original.view.length() > start) && (original.total < end))
			{
				BufferHolder replacement = new BufferHolder();

				if ((original.total < start) || (original.total + original.view.length() > end))
				{
					replacement.view = ((CharBuffer) original.view.subSequence(
							Math.max(0, start - original.total),
							Math.min(end - original.total, original.view.length())));
				}
				else
				{
					replacement.view = original.view;
				}

				if (previous != null)
				{
					previous.total += previous.view.length();
				}
				previous = replacement;

				this._bufferHolders.add(replacement);
			}
		}
	}

	public void addBuffer(ByteBuffer buffer) throws IOException
	{
		BufferHolder holder = new BufferHolder();

		holder.view = _decoder.decode(buffer);

		if (!this._bufferHolders.isEmpty())
		{
			BufferHolder previous = this._bufferHolders.getLast();
			holder.total += previous.total + previous.view.length();
		}
		this._bufferHolders.add(holder);
	}

	public char charAt(int index)
	{
		for (BufferHolder holder : this._bufferHolders)
		{
			if (index < holder.total + holder.view.length())
			{
				return holder.view.charAt(index - holder.total);
			}
		}
		throw new IndexOutOfBoundsException();
	}

	public int length()
	{
		if (this._bufferHolders.isEmpty())
		{
			return 0;
		}

		BufferHolder last = this._bufferHolders.getLast();
		return last.total + last.view.length();
	}

	public CharSequence subSequence(int start, int end)
	{
		return new BufferCharSequence(this, start, end);
	}

	public List<ByteBuffer> getBuffers()
	{
		List<ByteBuffer> result = new ArrayList<ByteBuffer>();
		for (BufferHolder holder : this._bufferHolders)
		{
			try
			{
				result.add(_encoder.encode(holder.view));
				holder.view.rewind();
			}
			catch (CharacterCodingException e)
			{
				throw new RuntimeException("Problem while re-encoding CharBuffer to ByteBuffer", e);
			}
		}
		return result;
	}

	@Override
	public String toString()
	{
		return new StringBuffer(this).toString();
	}

	private class BufferHolder
	{
		CharBuffer view;
		int total;

		private BufferHolder()
		{
		}
	}
}