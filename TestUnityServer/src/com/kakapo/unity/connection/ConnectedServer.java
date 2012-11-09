package com.kakapo.unity.connection;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.interserver.InterServerMessage;
import com.kakapo.unity.util.Objects;

public class ConnectedServer {

    private transient Connection objConnection;

    public ConnectedServer(Connection connection) {

        this.objConnection = connection;
    }

    public void receive(Message message) {
        //TODO - FRV method for the Connected server to receive any of the messages of type CLIENT or KEEPALIVE



        ConnectionStub objConnectionStub = (ConnectionStub) this.objConnection;
        objConnectionStub.receive(message);
        objConnectionStub = null;

        throw new UnsupportedOperationException("Not yet implemented receive(InterServerMessage message)");
    }

    public void send(InterServerMessage message) {

        //TODO - FRV call processInterServerMessage() in  UnityIMPServer
        throw new UnsupportedOperationException("Not yet implemented send(InterServerMessage registerMessage)");
    }

    @Override
    public String toString() {
        return Objects.toString(this);
    }

    public Connection getConnection() {
        return objConnection;
    }
}