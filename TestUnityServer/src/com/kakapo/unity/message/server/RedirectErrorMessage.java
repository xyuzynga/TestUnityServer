package com.kakapo.unity.message.server;

public class RedirectErrorMessage extends ErrorMessage
{
  private final String _redirect;

  public RedirectErrorMessage(int number, String key, boolean alert, boolean disconnect, String redirect)
  {
    super(number, key, alert, disconnect);
    this._redirect = redirect;
  }

  public String getRedirect()
  {
    return this._redirect;
  }
}