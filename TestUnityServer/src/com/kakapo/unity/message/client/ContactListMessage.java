package com.kakapo.unity.message.client;

import com.kakapo.unity.message.SingleItemSet;
import com.kakapo.unity.message.ContactAction;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.server.*;
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

    @Override
    public String toString() {
        return "Command: ContactList\nAdd-Contact: 442082881251@drd.co.uk\n\n";    }
  
  
}