package com.kakapo.unity.message.client;

public class RegisterMessage extends ClientMessage {

    public final String _Command = "Register";
    private final CharSequence _Group;
    private final CharSequence _Extension;
    private final boolean _Override;

    public RegisterMessage(CharSequence group, CharSequence extension, boolean override) {
        super("Register");
        this._Group = group;
        this._Extension = extension;
        this._Override = override;
    }

    public CharSequence getExtension() {
        return this._Extension;
    }

    public CharSequence getGroup() {
        return this._Group;
    }

    public boolean isOverride() {
        return this._Override;
    }
}