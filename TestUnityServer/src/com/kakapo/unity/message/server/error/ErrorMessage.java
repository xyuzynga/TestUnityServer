package com.kakapo.unity.message.server.error;

import com.kakapo.unity.message.server.ServerMessage;

public abstract class ErrorMessage extends ServerMessage {

    public final String COMMAND = "Error";
    private int _number;
    private boolean _alert;
    private String _key;

    public ErrorMessage(int number, String key, boolean alert) {
        this(number, key, alert, false);
    }

    public ErrorMessage(int number, String key, boolean alert, boolean disconnect) {
        super("Error", disconnect);
        this._number = number;
        this._alert = alert;
        this._key = key;
    }

    public int getNumber() {
        return this._number;
    }

    public boolean isAlert() {
        return this._alert;
    }

    public String getKey() {
        return this._key;
    }
}