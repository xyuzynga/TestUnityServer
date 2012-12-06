package com.kakapo.unity.message;

public class ContactAction {

    private Action _action;
    private String _extension;
    private String _product;

    public ContactAction(Action action, String contact) {
        this._action = action;
        this._extension = contact;
        this._product = null;
    }

    public ContactAction(Action action, String contact, String product) {
        this._action = action;
        this._extension = contact;
        this._product = product;
    }

    public Action getAction() {
        return this._action;
    }

    public String getExtension() {
        return this._extension;
    }

    public String getProduct() {
        return _product;
    }

    public enum Action {

        ADD, REMOVE;
    }

    @Override
    public String toString() {
        if ((this._product == null)||((this._product).contentEquals("none"))) {
            if (_action == Action.ADD) {
                return ("Add-Contact: " + _extension + "\n");
            } else {
                return ("Remove-Contact: " + _extension + "\n");
            }
        } else {
            if (_action == Action.ADD) {
                return ("Add-Contact: " + _extension + " " + _product + "\n");
            } else {
                return ("Remove-Contact: " + _extension + " " + _product + "\n");
            }
        }
    }
}