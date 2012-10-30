package com.kakapo.unity.message.server;

import com.kakapo.unity.message.Message;
import java.util.Collection;

public class ContactListMessage extends Message
{
  public static final String COMMAND = "ContactList";
  private final Collection<ContactAction> _actions;

  public ContactListMessage(Collection<ContactAction> actions)
  {
    super("ContactList");
    this._actions = actions;
  }

  public ContactListMessage(ContactAction action)
  {
    this(new SingleItemSet<ContactAction>(action));
  }

  public Collection<ContactAction> getActions()
  {
    return this._actions;
  }
}