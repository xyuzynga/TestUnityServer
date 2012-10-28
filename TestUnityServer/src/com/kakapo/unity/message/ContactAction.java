package com.kakapo.unity.message;

import com.kakapo.unity.util.Objects;

public class ContactAction
{
  private Action _action;
  private String _extension;
  private String _product;

  public ContactAction(Action action, String contact)
  {
    this._action = action;
    this._extension = contact;
  }
  
  public ContactAction(Action action, String contact, String product)
  {
    this._action = action;
    this._extension = contact;
    this._product = product;
  }

  public Action getAction()
  {
    return this._action;
  }

  public String getExtension()
  {
    return this._extension;
  }

    public String getProduct() {
        return _product;
    }

    @Override
  public String toString()
  {
    return Objects.toString(this);
  }

  public static enum Action
  {
    ADD, REMOVE;
  }
}