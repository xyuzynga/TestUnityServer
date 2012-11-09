package com.kakapo.unity.message.server;

import com.kakapo.unity.message.ContactAction;
import java.util.Collection;

public class ContactList2Message extends ServerMessage {

    public static final String COMMAND = "ContactList2";
    private final Collection<ContactAction> _actions;

    public ContactList2Message(Collection<ContactAction> actions) {
        super("ContactList");
        this._actions = actions;
    }

    public ContactList2Message(ContactAction action) {
        this(new SingleItemSet<ContactAction>(action));
    }

    public Collection<ContactAction> getActions() {
        return this._actions;
    }
}