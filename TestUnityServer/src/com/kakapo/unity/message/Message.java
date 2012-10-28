package com.kakapo.unity.message;

import com.kakapo.unity.util.Objects;

public abstract class Message
{
  private String _command;
  private final boolean _disconnect;

  public Message(String command)
  {
    this(command, false);
  }

  public Message(String command, boolean disconnect)
  {
    this._command = command;
    this._disconnect = disconnect;
  }

  public String getCommand()
  {
    return this._command;
  }

    @Override
  public String toString()
  {
    return Objects.toString(this);
  }

  public boolean isDisconnect()
  {
    return this._disconnect;
  }
}