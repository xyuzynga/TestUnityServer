/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.server;

import com.kakapo.unity.connection.ConnectedClient;
import com.kakapo.unity.connection.ConnectedServer;
import com.kakapo.unity.connection.Connection;
import com.kakapo.unity.connection.ConnectionStub;
import com.kakapo.unity.database.DatabaseOperation;
import com.kakapo.unity.message.ContactAction;
import com.kakapo.unity.message.ExtensionStatus;
import com.kakapo.unity.message.KeepAliveMessage;
import com.kakapo.unity.message.ManualStatus;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.ScheduledStatus;
import com.kakapo.unity.message.client.AddScheduledStatusMessage;
import com.kakapo.unity.message.client.ClearStatusMessage;
import com.kakapo.unity.message.client.ClientMessage;
import com.kakapo.unity.message.client.ListScheduledStatusesMessage;
import com.kakapo.unity.message.client.RemoveScheduledStatusMessage;
import com.kakapo.unity.message.client.SetStatusMessage;
import com.kakapo.unity.message.interserver.InterServerMessage;
import com.kakapo.unity.message.interserver.ServerContactList;
import com.kakapo.unity.message.interserver.ServerIM;
import com.kakapo.unity.message.interserver.ServerRegisterMessage;
import com.kakapo.unity.message.interserver.ServerSetStatus;
import com.kakapo.unity.message.interserver.ServerStatusListMessage;
import com.kakapo.unity.message.kempcodec.legacy.SimpleMessageCodec;
import com.kakapo.unity.message.peer.PeerMessage;
import com.kakapo.unity.message.server.ContactList2Message;
import com.kakapo.unity.message.server.ContactListMessage;
import com.kakapo.unity.message.server.StatusListMessage;
import com.kakapo.unity.network.Listener;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p> This is the main class from where the UnityIM&P server starts. </p>
 *
 * @return true if server startup was successful or false if unsuccessful
 * @author gopikrishnan.v & amith
 */
public class UnityIMPServer implements Server {

    private String strServerName;
    static final Logger logger = Logger.getLogger(Server.class.getName());
    private Level logLevel = Level.SEVERE;
    private String strPorts[];
    private final int DEFAULT_BUFFER_SIZE = 512;
    private final SimpleMessageCodec objSimpleCodec = new SimpleMessageCodec();
    private final Map<String, Group> groups = new HashMap<>();
    private final ServerGroup INBOUND_SERVERS = new ServerGroup(ServerGroup.SERVER_TYPE.INBOUND_SERVER_ENUM);
    private final ServerGroup OUTBOUND_SERVERS = new ServerGroup(ServerGroup.SERVER_TYPE.OUTBOUND_SERVER_ENUM);
    private Timestamp lastMessageReceivedTime;
    private int intKeepAliveDuration;
    private Date lastKeepAliveSend;
    private Timestamp lastScheduleStatusProcessTime;

    public static void main(String[] args) {
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    @Override
    public boolean startup() {
        //TODO - GK & AKB <Done> startup() Server Startup
        Properties objProperties = new Properties();
        //read servername and loglevel from unity.properties
        try {
            try (FileInputStream objFileInputStreamProperties = new FileInputStream("unity.properties")) {
                objProperties.load(objFileInputStreamProperties);
            }
            strServerName = objProperties.getProperty("ServerName");

            if (strServerName == null) {
                logger.log(Level.SEVERE, "SERVER EXITING......"
                        + "the ServerName is not set or corrupt in unity.properties"
                        + "Please add it to unity.properties file and start the server again");
                return false;
            }
            logLevel = Level.parse(objProperties.getProperty("LogLevel"));
        } catch (IOException e) {

            logger.log(logLevel, this.getClass().toString() + "SERVER EXITING......unity.properties file is "
                    + "missing.Server"
                    + " will not be able to startup.Please add unity."
                    + "properties file and start the server again", e);
            return false;
        } catch (NullPointerException e) {
            logger.log(Level.SEVERE, "Invalid details in"
                    + "unity.properties settings file.Please check the settings file"
                    + "and restart the server " + this.getClass().toString(), e);
            return false;
        }

        objProperties.clear();
//read database connection details from dbconnection.properties,then connect to the database and retrieve data from the settings table
        try (FileInputStream fileInputStreamProperties = new FileInputStream("dbconnection.properties")) {
            objProperties.load(fileInputStreamProperties);
            DatabaseOperation objDatabaseOperation = null;
            objDatabaseOperation = DatabaseOperation.getInstance("jdbc:mysql://"
                    + objProperties.getProperty("Address") + ":" + objProperties.getProperty("Port")
                    + "/" + objProperties.getProperty("DBName") + "?user=" + objProperties.getProperty("User")
                    + "&password=" + objProperties.getProperty("Password") + "&autoReconnect=true");
            objProperties.clear();
            try (ResultSet rs = objDatabaseOperation.executeQuery("call sp_get_settings")) {
                if (rs.next()) {
                    strPorts = rs.getString(2).trim().split(",");
                    intKeepAliveDuration = Integer.parseInt(rs.getString(1));
                    rs.close();
                }
            }
//start listener so that the server socketchannels are initialized and ready to perform communication
            Listener listener = new Listener(strPorts, DEFAULT_BUFFER_SIZE, 1000, this);
            listener.start();
//retrieve details of other servers running in the system from the database and send server registration messages to all of them
            SocketChannel socketChannelToConnectedServer;
            ConnectedServer objConnectedServer;
            try (ResultSet rs = objDatabaseOperation.executeQuery("CALL sp_get_all_name_and_address_of_running_servers('" + strServerName + "')")) {
                String strConnectedServerName;
                while (rs.next()) {

                    socketChannelToConnectedServer = SocketChannel.open();
                    socketChannelToConnectedServer.configureBlocking(true);

                    strConnectedServerName = rs.getString(1);
                    try {
                        socketChannelToConnectedServer.connect(new InetSocketAddress(rs.getString(2), Integer.parseInt(strPorts[0])));
                    } catch (NumberFormatException numberFormatException) {
                        logger.log(Level.SEVERE, "Server ports Cannot parse into Integer " + strConnectedServerName, numberFormatException);
                    }
                    if (socketChannelToConnectedServer.finishConnect()) {
                        socketChannelToConnectedServer.configureBlocking(false);
                    }

                    objConnectedServer = new ConnectedServer(new ConnectionStub(socketChannelToConnectedServer, this, objSimpleCodec, listener));
                    OUTBOUND_SERVERS.addServer(strConnectedServerName, objConnectedServer);
                    socketChannelToConnectedServer.register(listener.getSelector(), SelectionKey.OP_READ);
                    
                    objConnectedServer.receive(new ServerRegisterMessage(strServerName));

                }
            }
            objDatabaseOperation.executeUpdate("call sp_check_and_update_server_record__ServName__ServAddress('" + strServerName + "','" + InetAddress.getLocalHost().getHostAddress() + "')");



        } catch (IOException e) {
            logger.log(logLevel, "SERVER EXITING......Network error "
                    + " will not be able to startup", e);
            return false;
        } catch (SQLException e) {
            logger.log(logLevel, "SERVER EXITING......exception while executing call sp_get_settings", e);
            return false;
        } catch (ClassNotFoundException e) {
            logger.log(logLevel, "SERVER EXITING......MYSQL Driver is not found", e);
            return false;
        } catch (InstantiationException e) {
            logger.log(logLevel, "SERVER EXITING......DatabaseOperation object could not be created", e);
            return false;
        } catch (IllegalAccessException e) {
            logger.log(logLevel, "SERVER EXITING......Database connection settings are incorrect", e);
            return false;
        }
        return true;

    }

    @Override
    public void processClientRegisteration(ConnectedClient paramClient) {
        //TODO - GK processClientRegisteration() [Memory server --> public void register(Connection client, boolean override)]

        DatabaseOperation objdaDatabaseOperation = DatabaseOperation.getInstance();
        ConnectionStub objConnectionStub = (ConnectionStub) paramClient.getObjConnectionStub();
        ArrayList<ContactAction> contactActions = new ArrayList<>();
        //send contactlist details to the client who is logging in to the server
        try (ResultSet rs = objdaDatabaseOperation.executeQuery("call `unitydb`.`sp_get_all_other_clients_in_group__LoginId__Group`('" + objConnectionStub.getLoginIdOrServerName() + "''" + objConnectionStub.getLoginIdOrServerName() + "')")) {
            if (paramClient.getProductName() == null) {
                ContactListMessage contactListMessage = null;
                while (rs.next()) {
                    contactActions.add(new ContactAction(ContactAction.Action.ADD, rs.getString(1)));

                }
                contactListMessage = new ContactListMessage(contactActions);
                paramClient.receive(contactListMessage);

            } else {
                ContactList2Message contactList2Message = null;
                while (rs.next()) {
                    contactActions.add(new ContactAction(ContactAction.Action.ADD, rs.getString(1), rs.getString(2)));
                }
                contactList2Message = new ContactList2Message(contactActions);
                paramClient.receive(contactList2Message);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "unable to retrieve client list from database{0}", e);
        }
        //send statuslist message of all other clients who are connected to the IM&P system.
        StatusListMessage objStatusListMessage = new StatusListMessage();
        try (ResultSet rs = objdaDatabaseOperation.executeQuery("call `unitydb`.`sp_get_all_other_presence_status_in_grp_online__LoginId__Group`('" + objConnectionStub.getLoginIdOrServerName() + "','" + objConnectionStub.getLoginIdOrServerName() + "')")) {
            while (rs.next()) {
                objStatusListMessage.addStatus(rs.getString(1), rs.getString(2), rs.getDate(3));
            }
            paramClient.receive(objStatusListMessage);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "unable to retrieve satatus list from database{0}", e);
        }
//broadcast servercontactlist and server status list message to all other servers that are up and running        
        ServerContactList serverContactList;
        serverContactList = new ServerContactList((CharSequence) objConnectionStub.getGroupOrServerType(), new ContactAction(ContactAction.Action.ADD, objConnectionStub.getLoginIdOrServerName().toString(), paramClient.getProductName().toString()));
        ServerStatusListMessage serverStatusListMessage = null;
        try (ResultSet rs = objdaDatabaseOperation.executeQuery("call sp_get_presencestatus('" + objConnectionStub.getLoginIdOrServerName() + "')")) {
            if (rs.next()) {
                objStatusListMessage.addStatus(objConnectionStub.getLoginIdOrServerName().toString(), rs.getString(1), rs.getDate(2));
            }
            sendToAllConnectedServer(serverContactList);
            if (serverStatusListMessage != null) {
                sendToAllConnectedServer(serverStatusListMessage);
            }
        } catch (SQLException e) {
            Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, e);
        }
//Registration table in Database update
        try {
            objdaDatabaseOperation.executeQuery("call sp_check_and_update_client_record__LgnId__Grp__SrvNm__PrdNm('" + objConnectionStub.getLoginIdOrServerName() + "','" + objConnectionStub.getGroupOrServerType() + "','" + strServerName + "',''" + paramClient.getProductName() + "')");
        } catch (SQLException ex) {
            Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
//create a new group if group does not exist and add the client to the group
        Group objGroup = groups.get(objConnectionStub.getGroupOrServerType());
        if (objGroup == null) {
            objGroup = groups.put(objConnectionStub.getGroupOrServerType().toString(), new Group(objConnectionStub.getGroupOrServerType().toString()));
        }
        objGroup.addClient(objConnectionStub.getLoginIdOrServerName().toString(), paramClient);


    }

    @Override
    public void processServerRegisteration(ConnectedServer paramServer) {
        //TODO - AKB <Done> processServerRegisteration() [Memory server --> public void register(Connection client, boolean override)] Done
        ConnectionStub objConnectionStub = (ConnectionStub) paramServer.getConnection();
        OUTBOUND_SERVERS.addServer((String) objConnectionStub.getLoginIdOrServerName(), paramServer);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void processClientMessage(Connection paramClient, Message paramClientOrPeerMessage) {
        //TODO - GK [Memory server --> public void send(Connection sender, Message message)] [Can be CLIENT or PEER]

        String current = paramClientOrPeerMessage.getCommand();

        if (paramClientOrPeerMessage instanceof ClientMessage) {
            UnityIMPServer.Client_Message current_Message = UnityIMPServer.Client_Message.valueOf(current);
            DatabaseOperation objDatabaseOperation = DatabaseOperation.getInstance();
            ResultSet result = null;
            String strExtensionOfClient;
            Date objStartDate;
            Date objEndDate;
            switch (current_Message) {

                case SetStatus:
                    SetStatusMessage objSetStatusMessage = (SetStatusMessage) paramClientOrPeerMessage;
                    ConnectionStub objConnectionStub = (ConnectionStub) paramClient;
                    String strGroupName = (String) objConnectionStub.getGroupOrServerType();
                    strExtensionOfClient = objSetStatusMessage.getExtension();
                    ManualStatus status = objSetStatusMessage.getStatus();
                    ConnectedClient sender = this.groups.get(strGroupName).getConnectedClient(strExtensionOfClient);
                    Collection<ConnectedClient> recepients = this.groups.get(strGroupName).getAllConnectedClient();
                    Date objFromDate = status.getStart();
                    StatusListMessage objStatusListMessage = new StatusListMessage();
                    objStatusListMessage.addStatus(strExtensionOfClient, status);
                    // Send to Connected Clients

                    String strStatus = status.getName();
                    try {
                        objDatabaseOperation.executeUpdate("Call sp_add_AddhocStatus('" + strStatus + "','" + strExtensionOfClient + "')");
                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "Cannot set the AdHocStatus of Client : " + strExtensionOfClient, ex);
                    }
                    // Send to Connected Clients
                    sendToAllRecepientsInGroup(sender, recepients, objStatusListMessage);
                    ServerSetStatus objServerSetStatus = new ServerSetStatus(strGroupName, strExtensionOfClient, strStatus, objFromDate, true);
                    // send to connetced Servers both inbound and outbound
                    sendToAllConnectedServer(objServerSetStatus);
                    break;
                case AddScheduledStatus:
                    AddScheduledStatusMessage objAddScheduledStatus = (AddScheduledStatusMessage) paramClientOrPeerMessage;
                    strExtensionOfClient = objAddScheduledStatus.getExtension();
                    ScheduledStatus objScheduledStatus = objAddScheduledStatus.getScheduledStatus();

                    objStartDate = objScheduledStatus.getStart();
                    objEndDate = objScheduledStatus.getEnd();
                    String strIdOfScheduledStatus = objScheduledStatus.getId();
                    try {
                        result = objDatabaseOperation.executeQuery("");
                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "could not added the addhoc status of client : " + strExtensionOfClient, ex);
                    }
                    //Insert to the dtatbase and check if already a schedule status is exsist in DB.

                    break;
                case ClearStatus:
                    ClearStatusMessage objClearStatusMessage = (ClearStatusMessage) paramClientOrPeerMessage;
                    strExtensionOfClient = objClearStatusMessage.getExtension();
                    try {
                        result = objDatabaseOperation.executeQuery("");
                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "could not clear the status of client : " + strExtensionOfClient, ex);
                    }
                    // Create statusList message
                    break;
                case RemoveScheduledStatus:
                    RemoveScheduledStatusMessage objRemoveScheduledStatusMessage = (RemoveScheduledStatusMessage) paramClientOrPeerMessage;
                    String dOfScheduledStatus = objRemoveScheduledStatusMessage.getId();
                    strExtensionOfClient = objRemoveScheduledStatusMessage.getExtension();
                    String strRegistrationID = "Select ID from Registration where LoginID ='" + strExtensionOfClient + "'";
                    try {
                        objDatabaseOperation.executeUpdate("DELETE FROM unitydb.ScheduledStatus where RegistrationID ='" + strRegistrationID + "' and ID ='" + dOfScheduledStatus + "'");
                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "could not remove ScheduledStatus of client : " + strExtensionOfClient, ex);
                    }
                    // Create statusList message
                    break;
                case ListScheduledStatuses:
                    ListScheduledStatusesMessage objListScheduledStatusesMessage = (ListScheduledStatusesMessage) paramClientOrPeerMessage;
                    strExtensionOfClient = objListScheduledStatusesMessage.getExtension();
                    objStartDate = objListScheduledStatusesMessage.getStart();
                    objEndDate = objListScheduledStatusesMessage.getEnd();
                    String RegistrationID = "Select ID from Registration where LoginID ='" + strExtensionOfClient + "'";
                    try {
                        result = objDatabaseOperation.executeQuery("SELECT * FROM unitydb.ScheduledStatus where (StartDateTime = '" + objStartDate + "' and EndDateTime  ='" + objEndDate + "') and RegistrationID =('" + RegistrationID + "')  ;");
                        while (result.next()) {
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "could not added the addhoc status of client : " + strExtensionOfClient, ex);
                    }
                    // Create statusList Message
                    break;
                default:
                    throw new AssertionError();
            }

        } else if (paramClientOrPeerMessage instanceof PeerMessage) {
            UnityIMPServer.Peer_Message current_Message = UnityIMPServer.Peer_Message.valueOf(current);
            switch (current_Message) {

                case Message:
                    break;
                case Message2:
                    break;
                case Custom:
                    break;
                default:
                    throw new AssertionError();
            }

        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void sendToGroup() {
    }

    /**
     * All message from another server will be processed and Corresponding
     * action of each message has to be taken in this method.
     *
     * @param paramServer Sender of the message.
     * @param paramInterServerMessage Message from another server.
     */
    @Override
    public void processInterServerMessage(Connection paramServer, InterServerMessage paramInterServerMessage) {
        //TODO - AKB <Done>  processInterServerMessage() - new

        String current = paramInterServerMessage.getCommand();
        UnityIMPServer.InterServer_Message current_Message = UnityIMPServer.InterServer_Message.valueOf(current);
        switch (current_Message) {
            case ServerContactList:
                ServerContactList objServerContactList = (ServerContactList) paramInterServerMessage;
                String strGroup = (String) objServerContactList.getGroup();
                Group objClientGroup = this.groups.get(strGroup);
                ContactAction objContactAction = objServerContactList.getActions();
                String strExtensionOfClient = objContactAction.getExtension();
                String strProductNameOfClient = objContactAction.getProduct();
                ContactListMessage message = new ContactListMessage(objContactAction);
                Collection<ConnectedClient> recepients = objClientGroup.getAllConnectedClient();
                if (objClientGroup.getConnectedClient(strExtensionOfClient) != null) {
                    objClientGroup.removeConnectedClient(strExtensionOfClient);

                }
                if (strProductNameOfClient == null) {
                    for (ConnectedClient recipient : recepients) {
                        if (recipient != null) {
                            recipient.receive(message);
                        }

                    }
                } else {
                    ContactListMessage objContactList = new ContactListMessage(new ContactAction(objContactAction.getAction(), objContactAction.getExtension()));
                    ContactListMessage objContactList2 = new ContactListMessage(objContactAction);
                    for (ConnectedClient recipient : recepients) {
                        if ((recipient != null) && (recipient.getProductName() == null)) {
                            recipient.receive(objContactList);
                        } else if (recipient != null) {
                            recipient.receive(objContactList2);
                        }
                    }
                }
                break;


            case ServerMessage:
                ServerIM objServerIM = (ServerIM) paramInterServerMessage;
                Group objClientGroupName = this.getGroups().get(objServerIM.getGroup());
                Collection<ConnectedClient> recipients = objClientGroupName.getAllConnectedClient();
                PeerMessage objPeerMessageFromSender = objServerIM.getPeerMessage();
                sendToAllConnectedClientInGroup(recipients, objPeerMessageFromSender);
                break;
            case ServerStatusList:
                ServerStatusListMessage objServerStatusListMessage = (ServerStatusListMessage) paramInterServerMessage;
                Group objClientGrp = this.getGroups().get(objServerStatusListMessage.getGroup());
                List<ExtensionStatus> statuses = objServerStatusListMessage.getStatuses();
                StatusListMessage objStatusListMessage = new StatusListMessage();
                objStatusListMessage.setStatuses(statuses);
                Collection<ConnectedClient> recipientsInSameGroup = objClientGrp.getAllConnectedClient();
                // Send statusList
                sendToAllConnectedClientInGroup(recipientsInSameGroup, objStatusListMessage);
                break;
            case ServerSetStatus:
                ServerSetStatus objServerSetStatus = (ServerSetStatus) paramInterServerMessage;
                Group objClientGr = this.getGroups().get(objServerSetStatus.getGroup());
                String extensionOfClient = objServerSetStatus.getExtension();
                ManualStatus status = objServerSetStatus.getStatus();
                StatusListMessage objAdHocStatusListMessage = new StatusListMessage();
                objAdHocStatusListMessage.addStatus(extensionOfClient, status.getName(), status.getStart());
                Collection<ConnectedClient> recipientsInSameGrp = objClientGr.getAllConnectedClient();
                // send ServerSetStatus
                sendToAllConnectedClientInGroup(recipientsInSameGrp, objAdHocStatusListMessage);
                break;
            default:
                throw new AssertionError();
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void sendToAllConnectedClientInGroup(Collection<ConnectedClient> recepients, Message message) {
        for (ConnectedClient recipient : recepients) {
            if (recipient != null) {
                recipient.receive(message);
            }

        }
    }

    private void sendToAllRecepientsInGroup(ConnectedClient sender, Collection<ConnectedClient> recepients, Message message) {
        recepients.remove(sender);
        for (ConnectedClient recipient : recepients) {
            if (recipient != null) {
                recipient.receive(message);
            }
        }
    }

    private void sendToAllConnectedServer(Message message) {
        Collection<ConnectedServer> objInBoundConnectedServers = INBOUND_SERVERS.getAllConnectedServers();
        Collection<ConnectedServer> objOutBoundConnectedServers = OUTBOUND_SERVERS.getAllConnectedServers();
        for (ConnectedServer inBoundServers : objInBoundConnectedServers) {
            inBoundServers.receive(message);
        }
        for (ConnectedServer outBoundServers : objOutBoundConnectedServers) {
            outBoundServers.receive(message);
        }
    }

    @Override
    public void processScheduledStatuses() {
        //TODO - PP <Done> processScheduledStatuses()[Memory server --> public void processScheduledStatuses()]
        DatabaseOperation datbase = DatabaseOperation.getInstance();
        ResultSet rs = null;
        if (OUTBOUND_SERVERS.isEmpty()) {//Master Server
            try {
                rs = datbase.executeQuery("call sp_get_all_loginids_exp_sch_statuses_MASTER__ServerName('" + strServerName + "')");
                this.recieveExpiredScheduledStatuses(rs);
                rs = datbase.executeQuery("call sp_get_all_active_sch_statuses_MASTER__ServerName__LstProTm('" + strServerName + "','" + lastScheduleStatusProcessTime + "')");
                lastScheduleStatusProcessTime = new Timestamp(new Date().getTime());
                this.recieveActivatedScheduledStatuses(rs);
            } catch (SQLException e) {
                logger.log(logLevel, "Exception while executing call sp_get_settings", e);
            }
        } else {//Normal Server
            try {
                rs = datbase.executeQuery("call sp_get_all_loginids_exp_sch_statuses_NORMAL__ServerName('" + strServerName + "')");
                this.recieveExpiredScheduledStatuses(rs);
                rs = datbase.executeQuery("call sp_get_all_active_sch_statuses_NORMAL__ServerName__LstProTm('" + strServerName + "','" + lastScheduleStatusProcessTime + "')");
                lastScheduleStatusProcessTime = new Timestamp(new Date().getTime());
                this.recieveActivatedScheduledStatuses(rs);
            } catch (SQLException e) {
                logger.log(logLevel, "Exception while executing call sp_get_settings", e);
            }
            //  throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private void serverDisconnection() {
        //method made by AJ
        ResultSet rs = null;
        if (OUTBOUND_SERVERS.isEmpty()) {
            /*
             * 1. call stored procedure to update server Details()-->sp_server_disconnection_server_updation__serverName
             * 2. Call Stored procedure to update and retrieve client details-->` sp_server_disconnection_client_updation__serverName`
             */
//            String   query="sp_server_disconnection_server_updation__serverName("+ServerName+")";
//            databaseOperation/*change name*/.executeUpdate(query);   
//            query="sp_server_disconnection_client_updation__serverName("+ServerName+")";
//           rs=databaseOperation/*change name*/.executeUpdate(query); 
        } else {
            /*
             * not master. process server disconnection+
             *  Call Stored procedure retrieve client details
             */
        }
        Map<String, ContactListMessage> contactListMessagesNew = new HashMap<>();
        Map<String, ContactListMessage> contactListMessagesOld = new HashMap<>();
        try {
            while (rs.next()) {
                ContactListMessage contacNew = contactListMessagesOld.get(rs.getString(2));
                ContactListMessage contacOld = contactListMessagesOld.get(rs.getString(2));
                if (contacNew == null && contacOld == null) {
                    ContactAction actionNew = new ContactAction(ContactAction.Action.REMOVE, rs.getString(1), rs.getString(3));
                    ContactAction actionOld = new ContactAction(ContactAction.Action.REMOVE, rs.getString(1));
                    contacNew = new ContactListMessage(actionNew);
                    contacOld = new ContactListMessage(actionOld);
                } else {
                    ContactAction actionNew = new ContactAction(ContactAction.Action.REMOVE, rs.getString(1), rs.getString(3));
                    ContactAction actionOld = new ContactAction(ContactAction.Action.REMOVE, rs.getString(1));
                    contacNew.getActions().add(actionNew);
                    contacOld.getActions().add(actionOld);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        Set<String> groupNames = contactListMessagesNew.keySet();
        for (String groupName : groupNames) {
            Group groupFor = groups.get(groupName);
            {
                if (groupFor != null) {
                    Collection<ConnectedClient> clients1 = groupFor.getAllConnectedClient();
                    for (ConnectedClient client2 : clients1) {
                        if (client2.getProductName() == null) {
                            client2.receive(contactListMessagesOld.get(groupName));
                        } else {
                            client2.receive(contactListMessagesNew.get(groupName));
                        }
                    }
                }
            }
        }
    }

    private void recieveExpiredScheduledStatuses(ResultSet rs) {
        Map<Group, StatusListMessage> statusListmessages = new HashMap<>();
        Map<String, ServerStatusListMessage> ServerStatusListmessages = new HashMap<>();
        try {
            if (rs != null) {
                while (rs.next()) {
                    String groupName = rs.getString("Group");
                    String extension = rs.getString("LoginID");
                    Group group = (Group) this.groups.get(groupName);
                    if (group != null) {
                        StatusListMessage statusListMessage = (StatusListMessage) statusListmessages.get(group);
                        if (statusListMessage == null) {
                            statusListMessage = new StatusListMessage();
                            statusListmessages.put(group, statusListMessage);
                        }
                        statusListMessage.addStatus(extension, null, null);
                    }
                    ServerStatusListMessage serverStatusListMessage = (ServerStatusListMessage) ServerStatusListmessages.get(groupName);
                    if (serverStatusListMessage == null) {
                        serverStatusListMessage = new ServerStatusListMessage(groupName);
                        ServerStatusListmessages.put(groupName, serverStatusListMessage);
                    }
                    serverStatusListMessage.addStatus(extension, null, null);
                }
                if (statusListmessages != null) {
                    Set<Map.Entry<Group, StatusListMessage>> groupMessages = statusListmessages.entrySet();
                    for (Map.Entry<Group, StatusListMessage> entry : groupMessages) {                        
                        this.sendToAllConnectedClientInGroup(entry.getKey().getAllConnectedClient(), entry.getValue());
                    }
                }                
                for (ServerStatusListMessage message : ServerStatusListmessages.values()) {
                    this.sendToAllConnectedServer(message);       
                }
            }
        } catch (SQLException e) {
        }
    }

    @Override
    public void unregister(Connection paramClientOrServer) {
        //TODO - AJ <Done> unregister() [Memory server --> public void unregister(Connection client)]
        ConnectionStub connectionStub = (ConnectionStub) paramClientOrServer;
        CharSequence groupOrServerType = connectionStub.getLoginIdOrServerName();
        if ("INBOUND_SERVER_ENUM".contentEquals(groupOrServerType)) {
            String serverName = connectionStub.getLoginIdOrServerName().toString();
            INBOUND_SERVERS.removeConnectedServer(serverName.toString());
            serverDisconnection();
        } else if ("OUTBOUND_SERVER_ENUM".contentEquals(groupOrServerType)) {
            CharSequence serverName = connectionStub.getLoginIdOrServerName();
            OUTBOUND_SERVERS.removeConnectedServer(serverName.toString());
            serverDisconnection();
        } else {
            Group group = groups.get(groupOrServerType);
            String clientName = connectionStub.getLoginIdOrServerName().toString();
            if (group == null) {
                logger.log(Level.SEVERE, "Could not find group for client {0}", connectionStub);
                return;
            }
            ConnectedClient extension = group.removeConnectedClient(clientName);
            String productName = extension.getProductName().toString();
            if (extension != null) {
                ContactListMessage contactListMessage;
                ServerContactList serverContactList;
                if (productName == null) {
                    ContactAction action = new ContactAction(ContactAction.Action.REMOVE, clientName);
                    contactListMessage = new ContactListMessage(action);
                    serverContactList = new ServerContactList(groupOrServerType, action);
                } else {
                    ContactAction action = new ContactAction(ContactAction.Action.REMOVE, clientName, productName);
                    contactListMessage = new ContactListMessage(action);
                    serverContactList = new ServerContactList(groupOrServerType, action);
                }
                Collection<ConnectedClient> connectedClients = group.getAllConnectedClient();
                for (ConnectedClient connectedClient : connectedClients) {
                    connectedClient.receive(contactListMessage);
                }
                Collection<ConnectedServer> inBoundServers = INBOUND_SERVERS.getAllConnectedServers();
                for (ConnectedServer connectedServer : inBoundServers) {
                    connectedServer.receive(serverContactList);
                }
                Collection<ConnectedServer> outBoundServers = INBOUND_SERVERS.getAllConnectedServers();
                for (ConnectedServer connectedServer : outBoundServers) {
                    connectedServer.receive(serverContactList);
                }
            } else {
                logger.severe("Unregister client that was not connected");
            }
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void sendStatusesToClients(Collection<ConnectedClient> extensions, Message message) {
        for (ConnectedClient extension : extensions) {
            extension.receive(message);
        }
    }

    protected void sendStatusesToServers(Collection<ConnectedServer> extensions, Message message) {
        for (ConnectedServer extension : extensions) {
            extension.receive(message);
        }
    }

    private void recieveActivatedScheduledStatuses(ResultSet rs) {
        Map<Group, StatusListMessage> statusListmessages = new HashMap<>();
        Map<String, ServerStatusListMessage> ServerStatusListmessages = new HashMap<>();
        try {
            if (rs != null) {
                while (rs.next()) {
                    String groupName = rs.getString("Group");
                    String extension = rs.getString("LoginID");
                    String status = rs.getString("StatusString");
                    Date start = new Date(rs.getTimestamp("StartDateTime").getTime());
                    Group group = (Group) this.groups.get(groupName);
                    if (group != null) {
                        StatusListMessage statusListMessage = (StatusListMessage) statusListmessages.get(group);
                        if (statusListMessage == null) {
                            statusListMessage = new StatusListMessage();
                            statusListmessages.put(group, statusListMessage);
                        }
                        statusListMessage.addStatus(extension, status, start);
                    }
                    ServerStatusListMessage serverStatusListMessage = (ServerStatusListMessage) ServerStatusListmessages.get(groupName);
                    if (serverStatusListMessage == null) {
                        serverStatusListMessage = new ServerStatusListMessage(groupName);
                        ServerStatusListmessages.put(groupName, serverStatusListMessage);
                    }
                    serverStatusListMessage.addStatus(extension, status, start);
                }
                if (statusListmessages != null) {
                    Set<Map.Entry<Group, StatusListMessage>> groupMessages = statusListmessages.entrySet();
                    for (Map.Entry<Group, StatusListMessage> entry : groupMessages) {
                        this.sendToAllConnectedClientInGroup(entry.getKey().getAllConnectedClient(), entry.getValue());
                    }
                }
                for (ServerStatusListMessage message : ServerStatusListmessages.values()) {
                    this.sendToAllConnectedServer(message);
                }
            }
        } catch (SQLException e) {
        }
    }

    @Override
    public void sendServerKeepAliveMessage() {

        //TODO - AJ <Done>  sendServerKeepAliveMessage() - new [Use intKeepAliveDuration][call Connected server.receive() to send a keep alive message to server]
        if (intKeepAliveDuration < ((new Date().getTime() - lastKeepAliveSend.getTime()) / 1000)) {
            KeepAliveMessage keepAliveMessage = new KeepAliveMessage();
            this.sendToAllConnectedServer(keepAliveMessage);
        }

        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void shutdown() {
        //TODO - Unallotted [Memory server --> public void shutdown()]
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void processDisconnection() {
        //TODO - PP & AJ process Disconnection()
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * All message types
     */
    public static enum Message_Types {

        KeepAlive_Message,
        Peer_Message,
        Client_Message,
        Server_Message,
        Error_Message,
        InterServer_Message;
    }

    /**
     * KeepAlive messages
     */
    public static enum KeepAlive_Message {

        KeepAlive;
        //From client to server
        //From server to server
        //Used to keep the TCP connection active, should be ignored by the server
    }

    /**
     * All the Server to Server messages
     */
    public static enum InterServer_Message {

        ServerRegister,
        //From server to server
        //Used to register one server with another server.

        ServerContactList,
        //From server to server
        //Used to inform other servers [which then inform connected clients] that a user has registered or deregistered on the server.

        ServerMessage,
        //From server to server
        //Used to relay instant messages between clients connected to separate servers

        ServerStatusList,
        //From server to server
        //Used to inform other servers [which then inform connected clients] that the scheduled status for a connected client has changed.

        ServerSetStatus;
        //From server to server
        //Used to inform other servers [which then inform connected clients] that a connected client has set their ad-hoc status.
    }

    /**
     * All the Peer messages
     */
    public static enum Peer_Message {

        Message,
        //From client to server
        //From server to client
        //Send an instant message to one or more users

        Message2,
        //From client to server
        //From server to client
        //Used by clients using the new command
        //set to send instant messages, which are
        //then relayed through all servers.

        Custom;
        //From client to server
        //From server to client
        //Send an instant message to one or more users
    }

    /**
     * All the Client to Server messages
     */
    public static enum Client_Message {

        Register,
        //From client to server
        //Used to register a connection with user details such as user id and group name.

        Register2,
        //From client to server
        //Used by clients using the new command set to register on the server

        SetStatus,
        //From client to server
        //Set the ad-hoc status for a specified user

        AddScheduledStatus,
        //From client to server
        //Add a scheduled status for a specified user

        ClearStatus,
        //From client to server
        //Clear an ad-hoc or scheduled status for a specified user

        RemoveScheduledStatus,
        //From client to server
        //Clear a scheduled status for a specified user [used only in existing client app]

        ListScheduledStatuses;
        //From client to server
        //Requests all scheduled statuses for the specified user and date range
    }

    /**
     * All the Server to Client messages
     */
    public static enum Server_Message {

        ContactList,
        //From server to client
        //Returns the “online” status of one or more users

        StatusList,
        //From server to client
        //Returns the current presence status of one of more users.

        ScheduledStatusList,
        //From server to client
        //Response to the ListScheduledStatuses command. Lists all scheduled statuses that meet the params given in the command.

        Error,
        //From server to client
        //Used to inform a client that an error has occurred.

        ContactList2;
        //From server to client
        //Used to inform connected clients using
        //the new command set when another user
        //registers or deregisters.
    }

    /**
     * All Server.Error messages
     */
    public static enum Error_Message {

        INTERNAL_ERROR,
        OVERRIDE;
    }
}
