package com.kakapo.unity.message.client;

public class RegisterMessage2 extends ClientMessage {

    public final String COMMAND = "Register2";
    private final CharSequence _loginID;
    private final CharSequence _checkSum;
    private final CharSequence _group;
    private final CharSequence _productName;

    public RegisterMessage2(CharSequence group, CharSequence loginID, CharSequence checkSum, CharSequence productName) {
        super("Register2");
        this._group = group;
        this._loginID = loginID;
        this._checkSum = checkSum;
        this._productName = productName;
    }

    public String getCOMMAND() {
        return COMMAND;
    }

    public CharSequence getLoginID() {
        return _loginID;
    }

    public CharSequence getCheckSum() {
        return _checkSum;
    }

    public CharSequence getGroup() {
        return _group;
    }

    public CharSequence getProductName() {
        return _productName;
    }

    @Override
    public String toString() {
        return "RegisterMessage2{" + "COMMAND=" + COMMAND + ", _loginID=" + _loginID + ", _checkSum=" + _checkSum + ", _group=" + _group + ", _productName=" + _productName + '}';
    }
}