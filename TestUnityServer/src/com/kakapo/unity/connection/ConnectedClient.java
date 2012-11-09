package com.kakapo.unity.connection;

import com.kakapo.unity.message.Message;

public class ConnectedClient {

    private CharSequence productName;
    private transient Connection objConnectionStub;

    public ConnectedClient(CharSequence productName, Connection client) {

        this.objConnectionStub = client;

        this.productName = productName;
    }

    public void receive(Message message) {
        //TODO - FRV method for the Connected server to receive any of the messages of type SERVER,PEER or SERVER.ERROR

        ConnectionStub cs = (ConnectionStub) this.objConnectionStub;
        cs.receive(message);
        throw new UnsupportedOperationException("Not yet implemented receive(ClientMessage message)");
    }

    public void send(Message message) {

        //TODO - FRV call processClientMessage() in  UnityIMPServer [messages of type CLIENT or PEER]

        throw new UnsupportedOperationException("Not yet implemented send(ClientMessage message)");
    }

    public void getBlackListed() {
        //TODO - AJ getBlackListed()
        throw new UnsupportedOperationException("Not yet implemented getBlackListed()");
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void setConnectedClient(Connection objConnectedClient) {
        this.objConnectionStub = objConnectedClient;
    }

    public void setProductName(CharSequence productName) {
        this.productName = productName;
    }

    public CharSequence getProductName() {
        return productName;
    }

    public Connection getObjConnectionStub() {
        return objConnectionStub;
    }
    
}