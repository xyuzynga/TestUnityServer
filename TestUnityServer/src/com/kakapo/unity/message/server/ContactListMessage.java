package com.kakapo.unity.message.server;

import com.kakapo.unity.message.ContactAction;
import java.util.Collection;

public class ContactListMessage extends ServerMessage {

    public final String COMMAND = "ContactList";
    private final Collection<ContactAction> _actions;

    public ContactListMessage(Collection<ContactAction> actions) {
        super("ContactList");
        this._actions = actions;
    }

    public ContactListMessage(ContactAction action) {
        this(new SingleItemSet<>(action));
    }

    public Collection<ContactAction> getActions() {
        return this._actions;
    }
}