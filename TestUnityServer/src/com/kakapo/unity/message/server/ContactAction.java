package com.kakapo.unity.message.server;

import com.kakapo.unity.util.Objects;

public class ContactAction {

    private Action _action;
    private String _extension;

    public ContactAction(Action action, String contact) {
        this._action = action;
        this._extension = contact;
    }

    public Action getAction() {
        return this._action;
    }

    public String getExtension() {
        return this._extension;
    }

    public String toString() {
        return Objects.toString(this);
    }

    public static enum Action {

        ADD, REMOVE;
    }
}