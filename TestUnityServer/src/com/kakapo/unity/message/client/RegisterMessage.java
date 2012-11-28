package com.kakapo.unity.message.client;

public class RegisterMessage extends ClientMessage {

    public final String COMMAND = "Register";
    private final CharSequence _extension;
    private final CharSequence _group;
    private final boolean _override;

    public RegisterMessage(CharSequence group, CharSequence extension, boolean override) {
        super("Register");
        this._group = group;
        this._extension = extension;
        this._override = override;
    }

    public CharSequence getExtension() {
        return this._extension;
    }

    public CharSequence getGroup() {
        return this._group;
    }

    public boolean isOverride() {
        return this._override;
    }

    @Override
    public String toString() {
        return "RegisterMessage{" + "COMMAND=" + COMMAND + ", _extension=" + _extension + ", _group=" + _group + ", _override=" + _override + '}';
    }
}