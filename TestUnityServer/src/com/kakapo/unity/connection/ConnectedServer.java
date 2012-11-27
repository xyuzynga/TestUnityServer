package com.kakapo.unity.connection;

import com.kakapo.unity.message.KeepAliveMessage;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.interserver.InterServerMessage;

public class ConnectedServer {

    private transient Connection objConnection;

    public ConnectedServer(Connection connection) {

        this.objConnection = connection;
    }

    public void receive(Message message) {
        //TODO - FRV <Done> method for the Connected server to receive any of the messages of type CLIENT or KEEPALIVE
        ConnectionStub objConnectionStub = (ConnectionStub) this.objConnection;
        if (message instanceof InterServerMessage || message instanceof KeepAliveMessage) {
            objConnectionStub.receive(message);
        } else {
            throw new IllegalArgumentException("Server can only receive messages of type INTERSERVER or KEEPALIVE");
        }
        objConnectionStub = null;

//        throw new UnsupportedOperationException("Not yet implemented receive(InterServerMessage message)");
    }

    public void send(InterServerMessage message) {

        //TODO - FRV <Done> call processInterServerMessage() in  UnityIMPServer
        ConnectionStub objConnectionStub = (ConnectionStub) this.objConnection;
        if (message instanceof InterServerMessage /*KeepAliveMessage is already processed in connectionStub */) {
            objConnectionStub.getServer().processInterServerMessage(objConnection, message);
        } else {
            throw new IllegalArgumentException("Server can only send messages of type INTERSERVER or KEEPALIVE");
        }
        objConnectionStub = null;

//        throw new UnsupportedOperationException("Not yet implemented send(InterServerMessage registerMessage)");
    }

    public Connection getConnection() {
        return objConnection;
    }
}