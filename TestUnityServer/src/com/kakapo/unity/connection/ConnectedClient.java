package com.kakapo.unity.connection;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.client.ClientMessage;
import com.kakapo.unity.message.peer.PeerMessage;
import com.kakapo.unity.message.server.ServerMessage;
import com.kakapo.unity.message.server.error.ErrorMessage;
import com.kakapo.unity.server.UnityIMPServer;
import com.kakapo.unity.server.cryptography.DESEncryptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectedClient {

    private CharSequence productName;
    private transient Connection objConnectionStub;
    private int msgcounter = 0;
    private Date baseMessageReceivedTime;

    public ConnectedClient(CharSequence productName, Connection client) {

        this.objConnectionStub = client;
        this.productName = productName;
        this.baseMessageReceivedTime = new Date();
    }

    public void receive(Message message) {
        //TODO - FRV <Done> method for the Connected server to receive any of the messages of type SERVER,PEER or SERVER.ERROR

        ConnectionStub objConnectionStubRef = (ConnectionStub) this.objConnectionStub;
        if (message instanceof ServerMessage || message instanceof PeerMessage || message instanceof ErrorMessage) {
            objConnectionStubRef.receive(message);
        } else {
            throw new IllegalArgumentException("Client can only receive messages of type SERVER,PEER or SERVER.ERROR");
        }

        objConnectionStubRef = null;

//        throw new UnsupportedOperationException("Not yet implemented receive(ClientMessage message)");
    }

    public void send(Message message) {
        //TODO - FRV <Done> call processClientMessage() in  UnityIMPServer [messages of type CLIENT or PEER]

        ConnectionStub objConnectionStubRef = (ConnectionStub) this.objConnectionStub;
        if (message instanceof ClientMessage || message instanceof PeerMessage) {
            objConnectionStubRef.getServer().processClientMessage(objConnectionStubRef, message);
        } else {
            throw new IllegalArgumentException("Client can only send messages of type CLIENT or PEER");
        }

        objConnectionStubRef = null;

//        throw new UnsupportedOperationException("Not yet implemented send(ClientMessage message)");
    }

    public boolean checkBlackListMessageFrequency(String userId) {
        try {
            UnityIMPServer server;
            ConnectionStub connectionStub = (ConnectionStub) objConnectionStub;
            server = (UnityIMPServer) connectionStub.getServer();
            ResultSet rs = server.executeQuery("call sp_select_if_blacklisted__LoginId('" + userId + "')");
            if (rs.next()) {
                //BLACKLISTED OLD CLIENT ATTEMPT TO CONNECT
                Logger.getLogger(ConnectedClient.class.getName()).log(Level.INFO, ("Client is blacklisted"));
                return true;
            }
            rs.close();
            rs = null;
            server = null;
            connectionStub = null;
        } catch (SQLException e) {
            Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, e);
            return true;
        }
        return false;
    }

    public void getBlackListedRegistration(String checkSum) {
        //TODO - AJ  <Done> getBlackListedRegistration()
        UnityIMPServer server;
        ConnectionStub connectionStub = (ConnectionStub) objConnectionStub;
        server = (UnityIMPServer) connectionStub.getServer();
        String userId = ((ConnectionStub) (this.objConnectionStub)).getLoginIdOrServerName().toString();
        String serverCheckSum = new DESEncryptor().DesEncryption(userId.getBytes());
        ResultSet rs = null;
        if (serverCheckSum.equals(checkSum)) {
            try {
                //Check sum correct
                rs = server.executeQuery("call sp_check_loginid_and_blacklist_if_checksum_valid__loginid('" + userId + "')");

                rs.next();
                if (rs.getInt(1) == 0) {
                    Logger.getLogger(ConnectedClient.class.getName()).log(Level.FINE, "\n\ndisconnect() called from getBlackListedRegistration() - Check sum correct but is already blacklisted");
                    rs.close();
                    this.objConnectionStub.disconnect();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {       //Check sum NOT correct   

            try {
                server.executeUpdate("call sp_check_loginid_and_blacklist_if_checksum_invalid__loginid('" + userId + "')");

                //disconnect
                Logger.getLogger(ConnectedClient.class.getName()).log(Level.FINE, "\n\ndisconnect" + userId + "method called from getBlackListedRegistration() - Check sum INCORRECT");
                this.objConnectionStub.disconnect();

            } catch (SQLException ex) {
                Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        checkSum = null;
        serverCheckSum = null;
        server = null;
        connectionStub = null;
        rs = null;
        userId = null;
//        throw new UnsupportedOperationException("Not yet implemented getBlackListed()");
    }

    public void getBlackListedMessageFrequency() {
        //TODO - AJ <Done> getBlackListedMessageFrequency()
        UnityIMPServer server;
        ConnectionStub connectionStub = (ConnectionStub) objConnectionStub;
        server = (UnityIMPServer) connectionStub.getServer();
        msgcounter++;
        //ConnectionStub cs = (ConnectionStub) objConnectionStub;

        if (msgcounter > 30) //greater than 30
        {
            Date currentDate = new Date();
            long timeDifference = (currentDate.getTime() - baseMessageReceivedTime.getTime()) / 1000;
            if (timeDifference > 60) //normal case greater than 61
            {
                baseMessageReceivedTime = currentDate;
                msgcounter = 0;
            } else {    //Disconnection procedure & insert userId into blackList DB
                String loginId = ((ConnectionStub) (this.objConnectionStub)).getLoginIdOrServerName().toString();

                try {
                    server.executeQuery("call sp_insert_into_blacklist__LoginId('" + loginId + "')");
                } catch (SQLException ex) {
                    Logger.getLogger(ConnectedClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                Logger.getLogger(ConnectedClient.class.getName()).log(Level.FINE, "\n\ndisconnect() called from getBlackListedMessageFrequency()");
                loginId = null;
                this.objConnectionStub.disconnect();
            }
            currentDate = null;
        }
        connectionStub = null;
        server = null;
//throw new UnsupportedOperationException("Not yet implemented checkIfBlackListed()");
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
        if (productName == null) {
            return "none";
        } else {
            return productName;
        }
    }

    public Connection getObjConnectionStub() {
        return objConnectionStub;
    }
}