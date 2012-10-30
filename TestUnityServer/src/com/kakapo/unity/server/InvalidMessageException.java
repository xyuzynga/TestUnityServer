package com.kakapo.unity.server;

public class InvalidMessageException extends Exception
{
	private static final long serialVersionUID = 1L;

	public InvalidMessageException()
	{
	}

	public InvalidMessageException(String message)
	{
		super(message);
	}
}