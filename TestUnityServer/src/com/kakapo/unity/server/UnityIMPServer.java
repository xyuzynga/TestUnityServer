package com.kakapo.unity.server;

import com.kakapo.unity.connection.ConnectedClient;
import com.kakapo.unity.connection.ConnectedServer;
import com.kakapo.unity.connection.Connection;
import com.kakapo.unity.connection.ConnectionStub;
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
import com.kakapo.unity.message.kempcodec.KempCodec;
import com.kakapo.unity.message.kempcodec.LegacyPatternCodec;
import com.kakapo.unity.message.peer.CustomMessage;
import com.kakapo.unity.message.peer.PeerMessage;
import com.kakapo.unity.message.peer.TextMessage;
import com.kakapo.unity.message.peer.TextMessage2;
import com.kakapo.unity.message.server.ContactList2Message;
import com.kakapo.unity.message.server.ContactListMessage;
import com.kakapo.unity.message.server.ScheduledStatusListMessage;
import com.kakapo.unity.message.server.StatusListMessage;
import com.kakapo.unity.message.server.error.InternalErrorMessage;
import com.kakapo.unity.network.Listener;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * <p> This is the main class from where the UnityIM&P server starts. </p>
 *
 * @return true if server startup was successful or false if unsuccessful
 * @author gopikrishnan.v & amith
 */
public class UnityIMPServer implements Server {

    private String strServerName;
    private final Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
    private String strPorts[];
    private final int DEFAULT_BUFFER_SIZE = 512;
    private final KempCodec objKempCodec = new KempCodec();
    private final Map<String, Group> groups = new ConcurrentHashMap<>();
    private final ServerGroup INBOUND_SERVERS = new ServerGroup(ServerGroup.SERVER_TYPE.INBOUND_SERVER_ENUM);
    private final ServerGroup OUTBOUND_SERVERS = new ServerGroup(ServerGroup.SERVER_TYPE.OUTBOUND_SERVER_ENUM);
    private int intKeepAliveDuration;
    private Date lastKeepAliveSend = new Date();
    private Timestamp lastScheduleStatusProcessTime = new Timestamp(new Date().getTime());
    private boolean isMaster = false;
    public java.sql.Connection objConnectionMYSQL;
    String mysqlString;

    public UnityIMPServer() {
    }

    public static void main(String[] args) {
        new UnityIMPServer().startup();
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    @Override
    public void startup() {
        logger.log(Level.INFO, "MessageCodec = " + objKempCodec.getMessageCodec().getClass().getName());
        logger.log(Level.INFO, "Server startup ->\n" + "Maximum heap memory available = " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "M");
        //TODO - GK & AKB <Done> startup() Server Startup
        Properties objProperties = new Properties();
        //read servername and loglevel from unity.properties
        try {
            try (FileInputStream objFileInputStreamProperties = new FileInputStream("props/unity.properties")) {
                objProperties.load(objFileInputStreamProperties);
            }
            strServerName = objProperties.getProperty("ServerName");
            logger.log(Level.INFO, "Server name :{0}...", strServerName);
            if (strServerName == null) {
                logger.log(Level.SEVERE, "......"
                        + "the ServerName is not set or corrupt in unity.properties"
                        + "Please add it to unity.properties file and start the server again");
            }

            //Display user.home directory, if desired.
            //(This is the directory where the log files are generated.)
            logger.log(Level.INFO, "user.home dir: {0}", System.getProperty("user.home"));
            try {
                try (FileInputStream configFile = new FileInputStream("props/log.properties")) {
                    LogManager.getLogManager().readConfiguration(configFile);
                }
            } catch (IOException ex) {
                logger.logp(Level.WARNING, this.getClass().toString(), "UnityIMPServer - Constructor", "Could not load log.properties file", ex);
            }

        } catch (IOException e) {

            logger.log(Level.SEVERE, this.getClass().toString() + "......unity.properties file is "
                    + "missing.Server"
                    + " will not be able to startup.Please add unity."
                    + "properties file and start the server again", e);
        } catch (NullPointerException e) {
            logger.log(Level.SEVERE, "Invalid details in"
                    + "unity.properties settings file.Please check the settings file"
                    + "and restart the server " + this.getClass().toString(), e);
        }
        logger.log(Level.INFO, "Looking up for previously connected servers from database");
        objProperties.clear();
//read database connection details from dbconnection.properties,then connect to the database and retrieve data from the settings table
        try (FileInputStream fileInputStreamProperties = new FileInputStream("props/dbconnection.properties")) {
            objProperties.load(fileInputStreamProperties);
            // DatabaseOperation objDatabaseOperation = null;
            mysqlString = "jdbc:mysql://"
                    + objProperties.getProperty("Address") + ":" + objProperties.getProperty("Port")
                    + "/" + objProperties.getProperty("DBName") + "?user=" + objProperties.getProperty("User")
                    + "&password=" + objProperties.getProperty("Password") + "&autoReconnect=true";
            InitiaizeDatabaseOperations(mysqlString);
            objProperties.clear();
            try (ResultSet rs = executeQuery("call sp_get_settings")) {
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
            try (ResultSet rs = executeQuery("CALL sp_get_all_name_and_address_of_running_servers__ServName('" + strServerName + "')")) {
                String strConnectedServerName;
                while (rs.next()) {

                    socketChannelToConnectedServer = SocketChannel.open();
                    socketChannelToConnectedServer.configureBlocking(true);

                    strConnectedServerName = rs.getString(1);
                    try {
                        logger.log(Level.INFO, "Attempting to connect to server: {0}", strConnectedServerName);
                        socketChannelToConnectedServer.connect(new InetSocketAddress(rs.getString(2), Integer.parseInt(strPorts[0])));
                    } catch (NumberFormatException numberFormatException) {
                        logger.log(Level.SEVERE, "Server ports Cannot parse into Integer " + strConnectedServerName, numberFormatException);
                        continue;
                    } catch (IOException e) {

                        /*
                         * 1. call stored procedure to update server Details()-->sp_server_disconnection_server_updation__serverName
                         * 2. Call Stored procedure to update and retrieve client details-->` sp_server_disconnection_client_updation__serverName`
                         */
                        executeUpdate("call sp_server_disconnection_server_updation__serverName('" + strConnectedServerName + "')");
                        executeUpdate("call sp_server_disconnection_client_updation_Master__serverName('" + strConnectedServerName + "')");

                        logger.log(Level.SEVERE, "Cannot connect to server: " + strConnectedServerName, e);
                        continue;
                    }
                    if (socketChannelToConnectedServer.finishConnect()) {
                        socketChannelToConnectedServer.configureBlocking(false);
                    }

                    objConnectedServer = new ConnectedServer(new ConnectionStub(socketChannelToConnectedServer, this, objKempCodec.getMessageCodec(), listener, true, strConnectedServerName));
                    ConnectionStub objConnectionStub = (ConnectionStub) objConnectedServer.getConnection();
                    objConnectionStub.setGroupOrServerType(OUTBOUND_SERVERS.getServerType());
                    OUTBOUND_SERVERS.addServer(strConnectedServerName, objConnectedServer);
                    socketChannelToConnectedServer.register(listener.getSelector(), SelectionKey.OP_READ);
                    logger.log(Level.INFO, "Sending Server register message");
                    objConnectedServer.receive(new ServerRegisterMessage(strServerName));
                    objConnectionStub = null;
                }
                socketChannelToConnectedServer = null;
                objConnectedServer = null;
            }
            if (OUTBOUND_SERVERS.isEmpty()) {
                isMaster = true;
            }
            executeUpdate("call sp_check_and_update_server_record__ServName__ServAddress('" + strServerName + "','" + InetAddress.getLocalHost().getHostAddress() + "')");
            logger.log(Level.INFO, "Updated the server table with details of this server");


        } catch (IOException e) {

            logger.log(Level.SEVERE, "......Network error " + " will not be able to startup", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "......exception while executing call sp_get_settings", e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "......MYSQL Driver is not found", e);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "......DatabaseOperation object could not be created", e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "......Database connection settings are incorrect", e);
        } finally {
            objProperties = null;
        }
    }

    @Override
    public void processClientRegisteration(ConnectedClient paramClient) {
        //TODO - GK  <Done> processClientRegisteration() [Memory server --> public void register(Connection client, boolean override)]
        logger.log(Level.INFO, "\n\nClient Registeration ->");

        ConnectionStub objConnectionStub = (ConnectionStub) paramClient.getObjConnectionStub();
        ArrayList<ContactAction> contactActions = new ArrayList<>();
        ConnectionStub paramConnection = (ConnectionStub) paramClient.getObjConnectionStub();


        String strProductNameOfClient = paramClient.getProductName().toString();
        String loginIdOfClient = paramConnection.getLoginIdOrServerName().toString();
        logger.log(Level.INFO, "Client Registration :{0}Group :{1}", new Object[]{objConnectionStub.getLoginIdOrServerName(), objConnectionStub.getGroupOrServerType()});
        //send contactlist details to the client who is logging in to the server
        try (ResultSet rs = executeQuery("call `sp_get_all_other_clients_in_group__LoginId__Group`('" + objConnectionStub.getLoginIdOrServerName() + "','" + objConnectionStub.getGroupOrServerType() + "')")) {
            if (paramClient.getProductName().equals("none")) {
                logger.log(Level.INFO, "Legacy client");
                ContactListMessage contactListMessage = null;
                while (rs.next()) {
                    contactActions.add(new ContactAction(ContactAction.Action.ADD, rs.getString(1)));

                }
                contactListMessage = new ContactListMessage(contactActions);
                paramClient.receive(contactListMessage);
                contactListMessage = null;

            } else {
                logger.log(Level.INFO, "New client");
                ContactList2Message contactList2Message = null;
                while (rs.next()) {
                    contactActions.add(new ContactAction(ContactAction.Action.ADD, rs.getString(1), rs.getString(2)));
                }
                contactList2Message = new ContactList2Message(contactActions);
                paramClient.receive(contactList2Message);
                contactList2Message = null;
            }
            logger.log(Level.INFO, "ContactList message send to the user");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "unable to retrieve client list from database{0}", e);
        }
        //send statuslist message of all other clients who are connected to the IM&P system.
        StatusListMessage objStatusListMessage = new StatusListMessage();
        try (ResultSet rs = executeQuery("call `sp_get_all_other_presence_status_in_grp_online__LoginId__Group`('" + objConnectionStub.getLoginIdOrServerName() + "','" + objConnectionStub.getGroupOrServerType() + "')")) {
            String statusString;
            Timestamp startTime;
            while (rs.next()) {
                statusString = rs.getString(2);
                startTime = rs.getTimestamp(3);
                if (startTime == null) {
                    startTime = new Timestamp(new Date().getTime());
                }
                if (statusString != null) {
                    objStatusListMessage.addStatus(rs.getString(1), statusString, new Date(startTime.getTime()));
                }
            }
            paramClient.receive(objStatusListMessage);
            logger.log(Level.INFO, "StatusList message send to the user");
            statusString = null;
            objStatusListMessage = null;
            startTime = null;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "unable to retrieve satatus list from database{0}", e);
        }
        //broadcast servercontactlist and server status list message to all other servers that are up and running        
        ServerContactList serverContactList = null;

        serverContactList = new ServerContactList((CharSequence) objConnectionStub.getGroupOrServerType(), new ContactAction(ContactAction.Action.ADD, objConnectionStub.getLoginIdOrServerName().toString(), paramClient.getProductName().toString()));
        sendToAllConnectedServer(serverContactList);
        serverContactList = null;
        try {
            executeUpdate("call sp_check_and_update_client_record__LgnId__Grp__SrvNm__PrdNm('" + objConnectionStub.getLoginIdOrServerName() + "','" + objConnectionStub.getGroupOrServerType() + "','" + strServerName + "','" + paramClient.getProductName() + "')");
        } catch (SQLException ex) {
            Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        logger.log(Level.INFO, "Client details in Database updated");
        //create a new group if group does not exist and add the client to the group
        String group = objConnectionStub.getGroupOrServerType().toString();
        String loginID = objConnectionStub.getLoginIdOrServerName().toString();

        Group objGroup = groups.get(group);

        if (objGroup == null) {
            objGroup = new Group(group);
            groups.put(group, objGroup);
            logger.log(Level.INFO, "New group is added and put to the hashmap containing all groups");

        }

        objGroup.addClient(loginID, paramClient);
        group = null;
        loginID = null;
        logger.log(Level.INFO, "Client is added to the group");
        ContactListMessage objContactList = new ContactListMessage(new ContactAction(ContactAction.Action.ADD, loginIdOfClient));
        ContactList2Message objContactList2 = new ContactList2Message(new ContactAction(ContactAction.Action.ADD, loginIdOfClient, strProductNameOfClient));
        Collection<ConnectedClient> recipients = objGroup.getAllConnectedClient();
        if (strProductNameOfClient.equals("none")) {
            for (ConnectedClient recipient : recipients) {
                if (recipient != null) {
                    recipient.receive(objContactList);
                }

            }
        } else {

            for (ConnectedClient recipient : recipients) {
                if ((recipient != null) && (recipient.getProductName() == null)) {
                    recipient.receive(objContactList);
                } else if (recipient != null) {
                    recipient.receive(objContactList2);
                }
            }
        }
        objContactList = null;
        objContactList2 = null;
        logger.log(Level.INFO, "ContactList message of new user is send to all other clients in the group");
        try {
            //Todo GK offlyn messages
            ResultSet rs = executeQuery("call getOffMsgs('" + objConnectionStub.getLoginIdOrServerName() + "')");
            TextMessage textMessage;
            TextMessage2 textMessage2;
            if (paramClient.getProductName().equals("none")) {

                while (rs.next()) {
                    textMessage2 = (TextMessage2) objKempCodec.decode(rs.getString(1));
                    if (objKempCodec.getMessageCodec() instanceof LegacyPatternCodec) {
                        textMessage = new TextMessage(textMessage2.getText(), textMessage2.getExtensions(), null, textMessage2.getId(), textMessage2.getSender(), true, textMessage2.getLength());
                    } else {
                        textMessage = new TextMessage(textMessage2.getText(), textMessage2.getExtensions(), null, textMessage2.getId(), textMessage2.getSender(), true);
                    }
                    paramClient.receive(textMessage);
                }
                rs.close();

            } else {
                while (rs.next()) {
                    textMessage2 = (TextMessage2) objKempCodec.decode(rs.getString(1));
                    paramClient.receive(textMessage2);
                }
                rs.close();
            }
            textMessage = null;
            textMessage2 = null;
            objGroup = null;
            contactActions = null;
            executeUpdate("call removeIM('" + objConnectionStub.getLoginIdOrServerName() + "')");
            logger.log(Level.INFO, "Offlyn messages if any are retrieved and send to the user");
        } catch (Exception ex) {
            Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "Error while sending offline messages!", ex);
        } finally {
            objConnectionStub = null;
        }
    }

    @Override
    public void processClientMessage(Connection paramClient, Message paramClientOrPeerMessage) {
        //TODO - GK <Done> [Memory server --> public void send(Connection sender, Message message)] [Can be CLIENT or PEER]
        logger.log(Level.INFO, "\n\nClient Message ->");
        ConnectionStub clientconnection = (ConnectionStub) paramClient;
        String strGroupName = clientconnection.getGroupOrServerType().toString();
        Group objgroupOfClient = this.groups.get(strGroupName);
        Collection<ConnectedClient> objCollectionallMembersInGroup = objgroupOfClient.getAllConnectedClient();
        String current = paramClientOrPeerMessage.getCommand();
        ConnectedClient sender = this.groups.get(strGroupName).getConnectedClient(clientconnection.getLoginIdOrServerName().toString());
        if (paramClientOrPeerMessage instanceof ClientMessage) {
            UnityIMPServer.Client_Message current_Message = UnityIMPServer.Client_Message.valueOf(current);
            ResultSet result = null;
            String strExtensionOfClient;
            Date objStartDate;
            Date objEndDate;
            switch (current_Message) {

                case SetStatus:
                    logger.log(Level.INFO, "SetStatus Message");
                    SetStatusMessage objSetStatusMessage = (SetStatusMessage) paramClientOrPeerMessage;
                    // ConnectionStub objConnectionStub = (ConnectionStub) paramClient;
                    strExtensionOfClient = objSetStatusMessage.getExtension();
                    ManualStatus status = objSetStatusMessage.getStatus();
                    Collection<ConnectedClient> recepients = this.groups.get(strGroupName).getAllConnectedClient();
                    Date objFromDate = status.getStart();
                    StatusListMessage objStatusListMessage = new StatusListMessage();
                    objStatusListMessage.addStatus(strExtensionOfClient, status);

                    String strStatus = status.getName();
                    try {
                        executeUpdate("Call sp_add_AddhocStatus__StatusString__LoginId('" + strStatus + "','" + strExtensionOfClient + "')");
                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "Cannot set the AdHocStatus of Client : " + strExtensionOfClient, ex);
                    }
                    // Send to Connected Clients
                    sendToAllConnectedClientInGroup(recepients, objStatusListMessage);
                    ServerSetStatus objServerSetStatus = new ServerSetStatus(strGroupName, strExtensionOfClient, strStatus, objFromDate, true);
                    // send to connetced Servers both inbound and outbound
                    sendToAllConnectedServer(objServerSetStatus);
                    logger.log(Level.INFO, "statuslist message is send to all connected clients in the group and to all other connected servers");
                    objSetStatusMessage = null;
                    status = null;
                    recepients = null;
                    objFromDate = null;
                    objStatusListMessage = null;
                    strStatus = null;
                    objServerSetStatus = null;
                    break;
                case AddScheduledStatus:
                    logger.log(Level.INFO, "AddScheduledStatus message");
                    AddScheduledStatusMessage objAddScheduledStatus = (AddScheduledStatusMessage) paramClientOrPeerMessage;
                    strExtensionOfClient = objAddScheduledStatus.getExtension();
                    ScheduledStatus objScheduledStatus = objAddScheduledStatus.getScheduledStatus();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    objStartDate = objScheduledStatus.getStart();
                    objEndDate = objScheduledStatus.getEnd();
                    String strIdOfScheduledStatus = objScheduledStatus.getId();
                    // registrationID = "Select ID from Registration where LoginID ='" + strExtensionOfClient + "'";
                    if ((objStartDate.before(new Date()) || (objStartDate.after(objEndDate)))) {
                        logger.log(Level.SEVERE, "Invalid message time");
                        sender.receive(new InternalErrorMessage());
                        break;
                        //Todo send error message
                    }
                    try {
                        result = executeQuery("call getOverlappingStatuses('" + strExtensionOfClient + "','" + formatter.format(objStartDate) + "' , '" + formatter.format(objEndDate) + "')");
                        if (result.next()) {
                            logger.log(Level.SEVERE, "Overlapping status exists will not write the status to database");
                            sender.receive(new InternalErrorMessage());
                        } else {

                            try {

                                PreparedStatement ps = getPrepared("call sp_add_ScheduledStatus__SchStsId__Start__End__StsStr__LgnId(?,?,?,?,?)");
                                ps.setString(1, strIdOfScheduledStatus.toString());
                                ps.setTimestamp(2, new java.sql.Timestamp(objStartDate.getTime()));
                                ps.setTimestamp(3, new java.sql.Timestamp(objEndDate.getTime()));
                                ps.setString(4, objScheduledStatus.getName());
                                ps.setString(5, strExtensionOfClient);

                                ps.executeUpdate();
                                ps = null;
                                logger.log(Level.INFO, "Scheduled status is added to the database");
                            } catch (SQLException ex) {
                                sender.receive(new InternalErrorMessage());
                                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            objAddScheduledStatus = null;
                            objScheduledStatus = null;
                            formatter = null;
                            strIdOfScheduledStatus = null;
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "could not add ScheduledStatus of client : " + strExtensionOfClient, ex);
                    }
                    //Insert to the dtatbase and check if already a schedule status is exsist in DB.

                    break;
                case ClearStatus:
                    logger.log(Level.INFO, "ClearStatus Message");
                    String strAdHocStatus;
                    String strScheduledStatus;
                    String strscheduledStatusID;
                    Timestamp statusStartTime;
                    ClearStatusMessage objClearStatusMessage = (ClearStatusMessage) paramClientOrPeerMessage;
                    strExtensionOfClient = objClearStatusMessage.getExtension();
                    try {
                        result = executeQuery("call getCurrentStatus('" + strExtensionOfClient + "')");
                        if (result.next()) {
                            strAdHocStatus = result.getString(2);
                            strScheduledStatus = result.getString(3);
                            statusStartTime = result.getTimestamp(4);
                            if (!(statusStartTime == null)) {
                                strScheduledStatus = null;
                            }
                            if (strAdHocStatus == null) {
                                if (strScheduledStatus == null) {
                                    logger.log(Level.INFO, "No Status to be cleared for user {0}", strExtensionOfClient);
                                } else {
                                    strscheduledStatusID = result.getString(5);
                                    executeUpdate("call removeScheduledStatus('" + strscheduledStatusID + "')");
                                    StatusListMessage objRemovedStatusListMessage = new StatusListMessage();
                                    objRemovedStatusListMessage.addStatus(strExtensionOfClient, null);
                                    //Send to clients
                                    sendToAllConnectedClientInGroup(objCollectionallMembersInGroup, objRemovedStatusListMessage);
                                    objRemovedStatusListMessage = null;
                                    ServerStatusListMessage objServerStatusListMessage = new ServerStatusListMessage(strGroupName);
                                    objServerStatusListMessage.addStatus(strExtensionOfClient, null);
                                    // Send to all servers
                                    sendToAllConnectedServer(objServerStatusListMessage);
                                    objServerStatusListMessage = null;
                                }
                            } else {
                                executeUpdate("call removeAdhocStatus('" + strExtensionOfClient + "')");
                                StatusListMessage objRemovedStatusListMessage = new StatusListMessage();
                                ServerStatusListMessage objServerStatusListMessage = new ServerStatusListMessage(strGroupName);
                                if (strScheduledStatus == null) {
                                    objRemovedStatusListMessage.addStatus(strExtensionOfClient, null);
                                    objServerStatusListMessage.addStatus(strExtensionOfClient, null);
                                } else {
                                    objRemovedStatusListMessage.addStatus(strExtensionOfClient, strScheduledStatus, new Date());
                                    objServerStatusListMessage.addStatus(strExtensionOfClient, strScheduledStatus, new Date());
                                }
                                sendToAllConnectedClientInGroup(objCollectionallMembersInGroup, objRemovedStatusListMessage);
                                sendToAllConnectedServer(objServerStatusListMessage);
                                objRemovedStatusListMessage = null;
                                objServerStatusListMessage = null;

                            }
                            strAdHocStatus = null;
                            strScheduledStatus = null;
                            strscheduledStatusID = null;
                            statusStartTime = null;
                            objClearStatusMessage = null;

                        } else {
                            logger.log(Level.WARNING, "LoginID does not exist in the database :{0}", strExtensionOfClient);
                        }
                        logger.log(Level.INFO, "Presence status is cleared");

                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "could not clear the status of client : " + strExtensionOfClient, ex);
                    }
                    // Create statusList message
                    break;
                case RemoveScheduledStatus:
                    logger.log(Level.INFO, "RemoveScheduledStatus message");
                    RemoveScheduledStatusMessage objRemoveScheduledStatusMessage = (RemoveScheduledStatusMessage) paramClientOrPeerMessage;
                    String idOfScheduledStatus = objRemoveScheduledStatusMessage.getId();
                    strExtensionOfClient = objRemoveScheduledStatusMessage.getExtension();
                    // String strRegistrationID = "Select ID from Registration where LoginID ='" + strExtensionOfClient + "'";
                    try {
                        result = executeQuery("call getActiveScheduledStatus('" + strExtensionOfClient + "') ;");
                        if (result.next()) {
                            StatusListMessage objRemovedStatusListMessage = new StatusListMessage();
                            objRemovedStatusListMessage.addStatus(strExtensionOfClient, null, null);
                            //Send to clients
                            sendToAllConnectedClientInGroup(objCollectionallMembersInGroup, objRemovedStatusListMessage);
                            ServerStatusListMessage objServerStatusListMessage = new ServerStatusListMessage(strGroupName);
                            objServerStatusListMessage.addStatus(strExtensionOfClient, null, null);
                            // Send to all servers
                            sendToAllConnectedServer(objServerStatusListMessage);
                            objRemovedStatusListMessage = null;
                            objServerStatusListMessage = null;
                            logger.log(Level.INFO, "Statuslist is set to none and send to all connected clients in the group and to all connected servers");
                        }

                        executeUpdate("call removeScheduledStatus ('" + idOfScheduledStatus + "');");
                        logger.log(Level.WARNING, "Database updated");

                        ResultSet rs1 = executeQuery("call getCurrentStatus('" + clientconnection.getLoginIdOrServerName() + "')");
                        if (rs1.next()) {
                            if (rs1.getString(2) == null) {
                                StatusListMessage slm = new StatusListMessage();
                                slm.addStatus(clientconnection.getLoginIdOrServerName().toString(), null);
                                sender.receive(slm);
                                slm = null;
                            }
                        }
                        rs1.close();
                        rs1 = null;

                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "could not remove ScheduledStatus of client : " + strExtensionOfClient, ex);
                    }
                    // Create statusList message

                    objRemoveScheduledStatusMessage = null;
                    idOfScheduledStatus = null;
                    break;
                case ListScheduledStatuses:
                    logger.log(Level.INFO, "ListScheduledStatus message");
                    ListScheduledStatusesMessage objListScheduledStatusesMessage = (ListScheduledStatusesMessage) paramClientOrPeerMessage;
                    strExtensionOfClient = objListScheduledStatusesMessage.getExtension();
                    objStartDate = objListScheduledStatusesMessage.getStart();
                    objEndDate = objListScheduledStatusesMessage.getEnd();
                    //registrationID = "Select ID from Registration where LoginID ='" + strExtensionOfClient + "'";
                    ArrayList<ScheduledStatus> objstatusCollection = new ArrayList();
                    try {

                        PreparedStatement ps = getPrepared("call getScheduledStatusList (?,?,?)");
                        ps.setTimestamp(2, new java.sql.Timestamp(objStartDate.getTime()));
                        ps.setTimestamp(3, new java.sql.Timestamp(objEndDate.getTime()));
                        ps.setString(1, strExtensionOfClient);
                        result = ps.executeQuery();
                        while (result.next()) {
                            objstatusCollection.add(new ScheduledStatus(result.getString(4), result.getDate(2), result.getDate(3), result.getString(1)));
                        }
                        result.close();
                        result = null;
                        ps = null;
                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "could not ListScheduledStatuses : " + strExtensionOfClient, ex);
                    }
                    // Create statusList Message

                    //sendToSender(paramClient,objstatusCollection);
                    this.groups.get(clientconnection.getGroupOrServerType()).getConnectedClient(strExtensionOfClient).receive(new ScheduledStatusListMessage(objstatusCollection));
                    logger.log(Level.INFO, "Scheduled statuses are fetched from the database and ScheduledStatusList is send to the user");
                    objListScheduledStatusesMessage = null;
                    objstatusCollection = null;
                    break;
                default:
                    throw new AssertionError();
            }

        } else if (paramClientOrPeerMessage instanceof PeerMessage) {
            UnityIMPServer.Peer_Message current_Message = UnityIMPServer.Peer_Message.valueOf(current);
            switch (current_Message) {

                case Message:
                    logger.log(Level.INFO, "Instant Message");
                    TextMessage objTextMessage = (TextMessage) paramClientOrPeerMessage;
                    Group objClientGroup = this.groups.get(strGroupName);
                    Collection<String> recievers = objTextMessage.getExtensions();
                    //ConnectedClient objConnectedClientSender = objClientGroup.getConnectedClient(clientconnection.getLoginIdOrServerName().toString());
                    ConnectedClient recepientClient;
                    for (String recepient : recievers) {
                        recepientClient = objClientGroup.getConnectedClient(recepient);
                        if (recepientClient != null) {
                            recepientClient.receive(paramClientOrPeerMessage);
                        }
                        // check the client is online and call recieve method then broadcast to all connected servers.
                    }
                    //Send to connected Servers
                    ServerIM objServerIM = new ServerIM(strGroupName, objTextMessage);
                    sendToAllConnectedServer(objServerIM);
                    objTextMessage = null;
                    objClientGroup = null;
                    recievers = null;
                    recepientClient = null;
                    objServerIM = null;
                    logger.log(Level.INFO, "Instant Message is send to recepients connected to the server and broadcasted to all servers");
                    break;
                case Message2:
                    logger.log(Level.INFO, "Instant Message2");
                    // create Message and Message 2 check the product name of each recipient and if the client using legacy clientApp they should have to recieve only Message .
                    // If recepients is not online the message has to be stored in DB. 

                    TextMessage2 objTextMessage2 = (TextMessage2) paramClientOrPeerMessage;
                    TextMessage objTextMessageOld;
                    if (objKempCodec.getMessageCodec() instanceof LegacyPatternCodec) {
                        objTextMessageOld = new TextMessage(objTextMessage2.getText(), objTextMessage2.getExtensions(), objTextMessage2.getInput(), objTextMessage2.getId(), objTextMessage2.getSender(), true, objTextMessage2.getLength());
                    } else {
                        objTextMessageOld = new TextMessage(objTextMessage2.getText(), objTextMessage2.getExtensions(), objTextMessage2.getInput(), objTextMessage2.getId(), objTextMessage2.getSender(), true);
                    }
                    Collection<String> reciever = objTextMessage2.getExtensions();
                    //ConnectedClient objConnectedClientSender = objClientGroup.getConnectedClient(clientconnection.getLoginIdOrServerName().toString());
                    String senderID = objTextMessage2.getSender().toString();
                    ResultSet rs;

                    for (String recepient : reciever) {
                        recepientClient = objgroupOfClient.getConnectedClient(recepient);
                        if (recepientClient != null) {
                            if (recepientClient.getProductName().equals("none")) {
                                recepientClient.receive(objTextMessage2);
                            } else {
                                recepientClient.receive(objTextMessageOld);
                            }

                        } else {
                            try {
                                // go to db and check if client is online or offline
                                rs = executeQuery("call getRegID('" + senderID + "');");
                                if (rs.next()) {
                                    executeUpdate("call insertIM(null,'" + objTextMessage2.getInput() + "','" + rs.getInt(1) + "') ;");
                                }
                            } catch (SQLException ex) {
                                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        // check the client is online and call recieve method then broadcast to all connected servers.
                    }

                    ServerIM objServerIMNew = new ServerIM(strGroupName, objTextMessage2);
                    sendToAllConnectedServer(objServerIMNew);

                    objTextMessage2 = null;
                    objTextMessageOld = null;
                    reciever = null;
                    senderID = null;
                    recepientClient = null;
                    objServerIMNew = null;
                    logger.log(Level.INFO, "Instant Message is send to recepients connected to the server and broadcasted to all servers if any recepient is offline then it is added to the IM table");
                    break;
                case Custom:
                    logger.log(Level.INFO, "Custom Message");
                    CustomMessage objCustomMessage = (CustomMessage) paramClientOrPeerMessage;

                    Collection<String> customReciever = objCustomMessage.getExtensions();
                    for (String recepient : customReciever) {
                        recepientClient = objgroupOfClient.getConnectedClient(recepient);
                        if (recepientClient != null) {
                            if (!recepientClient.getProductName().equals("none")) {
                                recepientClient.receive(objCustomMessage);
                            }


                        }
                        logger.log(Level.INFO, "Custom Message is send to recepients connected to the server and broadcasted to all servers");
                        // check the client is online and call recieve method then broadcast to all connected servers.
                    }
                    ServerIM objServerIMCustom = new ServerIM(strGroupName, objCustomMessage);
                    sendToAllConnectedServer(objServerIMCustom);
                    objCustomMessage = null;
                    customReciever = null;
                    recepientClient = null;
                    objServerIMCustom = null;
                    break;
                default:
                    throw new AssertionError();
            }

            current_Message = null;
        }
        clientconnection = null;
        strGroupName = null;
        objgroupOfClient = null;
        objCollectionallMembersInGroup = null;
        current = null;
        sender = null;

        //  throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void processServerRegisteration(ConnectedServer paramServer) {
        //TODO - AKB <Done> processServerRegisteration() [Memory server --> public void register(Connection client, boolean override)] Done
        logger.log(Level.INFO, "\n\nServer Registeration ->");
        ConnectionStub objConnectionStub = (ConnectionStub) paramServer.getConnection();
        INBOUND_SERVERS.addServer(objConnectionStub.getLoginIdOrServerName().toString(), paramServer);
        logger.log(Level.INFO, "{0} is added to the INBOUND_SERVERS group", objConnectionStub.getLoginIdOrServerName().toString());
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * All message between each server will be processed in this method and its
     * Corresponding action of each message has to be taken.
     *
     * @
     * @param paramServer Sender of the message.
     * @param paramInterServerMessage Message from another server. </br>Server
     * Messages are follows </br> <P><b>ServerContactList : </b> Server receives
     * this message from another server when a new client is online or offline.
     * if the particular client is online in this server, server will remove the
     * existing client and send an override message to existing client,will
     * close the existing connection immediately. and also send the contactList
     * message to the connected client having the same group </p>
     * <P><b>ServerMessage :</b> This is a ServerInstant message received from
     * another server, this server will create IM and send to the recipients in
     * the same group. If server receives message in legacy format server sill
     * simply relay to all recipients in the same group. otherwise the server
     * will create message in legacy as well as in new format and send to the
     * connected client according to their unity client application.</p>
     * <P><b>ServerStatusList : </b> Server receives this message when scheduled
     * status of client in a remote server is activated, the receiving server
     * create the statuslist message from this message and send to the all
     * connected client in the same group </p> <P><b>ServerSetStatus : </b>
     * Server receives this message when a client of remote server set an AdHoc
     * status. the receiving server will create the statuslist message and send
     * to the all connected client in the same group </p>
     */
    @Override
    public void processInterServerMessage(Connection paramServer, InterServerMessage paramInterServerMessage) {
        //TODO - AKB <Done> processInterServerMessage() - new
        logger.log(Level.INFO, "\n\nInter-Server Message ->");
        UnityIMPServer.InterServer_Message current_Message = null;

        String current = paramInterServerMessage.getCommand();
        current_Message = UnityIMPServer.InterServer_Message.valueOf(current);


        switch (current_Message) {
            case ServerContactList:
                ServerContactList objServerContactList = (ServerContactList) paramInterServerMessage;
                String strGroup = objServerContactList.getGroup().toString();
                try {


                    Group objClientGroup = this.groups.get(strGroup);
                    if (objClientGroup != null) {
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
                            objContactList = null;
                            objContactList2 = null;
                        }
                        objContactAction = null;
                        strExtensionOfClient = null;
                        message = null;
                        recepients = null;
                        strProductNameOfClient = null;
                    } else {

                        logger.log(Level.INFO, "Group is not registered in the server");
                    }

                } catch (Exception E) {
                    logger.log(Level.SEVERE, "Error occured during the proccess of ServerContactlist message in server", E);

                } finally {
                    objServerContactList = null;
                    strGroup = null;
                }

                break;


            case ServerMessage:
                logger.log(Level.INFO, "Inter Server message");
                ServerIM objServerIM = (ServerIM) paramInterServerMessage;
                Group objClientGroupName = this.getGroups().get(objServerIM.getGroup().toString());
                try {


                    if (objClientGroupName != null) {

                        PeerMessage objPeerMessageFromSender = objServerIM.getPeerMessage();
                        if (objPeerMessageFromSender instanceof TextMessage) {
                            TextMessage objTextMessage = (TextMessage) objPeerMessageFromSender;
                            Set<String> extensions = objTextMessage.getExtensions();
                            ConnectedClient recipientClient;
                            for (String recipient : extensions) {
                                recipientClient = objClientGroupName.getConnectedClient(recipient);
                                if (recipientClient != null) {
                                    recipientClient.receive(objTextMessage);
                                } else {
                                    logger.log(Level.INFO, " Recipient : {0} is not in this server ", recipient);
                                }

                            }
                            logger.log(Level.INFO, "Message sent to all recipients connected to this server");
                            objTextMessage = null;
                            extensions = null;
                            recipientClient = null;
                        } else if (objPeerMessageFromSender instanceof TextMessage2) {
                            TextMessage2 objTextMessage2 = (TextMessage2) objPeerMessageFromSender;
                            Set<String> extensions = objTextMessage2.getExtensions();
                            TextMessage objTextMessageToOld;
                            if (objKempCodec.getMessageCodec() instanceof LegacyPatternCodec) {
                                objTextMessageToOld = new TextMessage(objTextMessage2.getText(), extensions, null, objTextMessage2.getId(), objTextMessage2.getSender(), true, objTextMessage2.getLength());
                            } else {
                                objTextMessageToOld = new TextMessage(objTextMessage2.getText(), extensions, null, objTextMessage2.getId(), objTextMessage2.getSender(), true);
                            }
                            ConnectedClient objRecipient;
                            for (String recipient : extensions) {
                                objRecipient = objClientGroupName.getConnectedClient(recipient);
                                if ((objRecipient != null) && (objRecipient.getProductName() == null)) {
                                    objRecipient.receive(objTextMessageToOld);
                                } else if (objRecipient != null) {
                                    objRecipient.receive(objTextMessage2);
                                } else {
                                    logger.log(Level.INFO, " Recipient : {0} is not in this server ", recipient);
                                }
                            }
                            logger.log(Level.INFO, "Message sent to all recipients connected to this server");
                            objTextMessage2 = null;
                            extensions = null;
                            objTextMessageToOld = null;
                            objRecipient = null;
                        }
                        objPeerMessageFromSender = null;


                    } else {
                        logger.log(Level.INFO, "Group is not registered in the server");
                    }

                } catch (Exception E) {
                    logger.log(Level.SEVERE, "Error occured during the proccess of ServerMessage message in server", E);
                } finally {
                    objServerIM = null;
                    objClientGroupName = null;
                }

                break;
            case ServerStatusList:
                ServerStatusListMessage objServerStatusListMessage = (ServerStatusListMessage) paramInterServerMessage;
                Group objClientGrp = this.getGroups().get(objServerStatusListMessage.getGroup());
                try {
                    logger.log(Level.INFO, "ServerStatusList");

                    if (objClientGrp != null) {
                        List<ExtensionStatus> statuses = objServerStatusListMessage.getStatuses();
                        StatusListMessage objStatusListMessage = new StatusListMessage();
                        objStatusListMessage.setStatuses(statuses);
                        Collection<ConnectedClient> recipientsInSameGroup = objClientGrp.getAllConnectedClient();
                        // Send statusList
                        sendToAllConnectedClientInGroup(recipientsInSameGroup, objStatusListMessage);
                        logger.log(Level.INFO, "Status sent to all recipients connected to this server and in the same group");
                        statuses = null;
                        objStatusListMessage = null;
                        recipientsInSameGroup = null;
                    } else {
                        logger.log(Level.INFO, "Group is not registered in the server");
                    }

                } catch (Exception E) {
                    logger.log(Level.SEVERE, "Error occured during the proccess of ServerStatusList message in server", "" + E);

                } finally {
                    objServerStatusListMessage = null;
                    objClientGrp = null;
                }

                break;
            case ServerSetStatus:
                logger.log(Level.INFO, "ServerSetStatus message");
                ServerSetStatus objServerSetStatus = (ServerSetStatus) paramInterServerMessage;
                Group objClientGr = this.getGroups().get(objServerSetStatus.getGroup());
                try {
                    if (objClientGr != null) {
                        String extensionOfClient = objServerSetStatus.getExtension();
                        ManualStatus status = objServerSetStatus.getStatus();
                        StatusListMessage objAdHocStatusListMessage = new StatusListMessage();
                        objAdHocStatusListMessage.addStatus(extensionOfClient, status.getName(), status.getStart());
                        Collection<ConnectedClient> recipientsInSameGrp = objClientGr.getAllConnectedClient();
                        // send ServerSetStatus
                        sendToAllConnectedClientInGroup(recipientsInSameGrp, objAdHocStatusListMessage);
                        logger.log(Level.INFO, "Status message is send to all connected clints in the same group");

                        extensionOfClient = null;
                        status = null;
                        objAdHocStatusListMessage = null;
                        recipientsInSameGrp = null;
                    } else {
                        logger.log(Level.INFO, "Group is not registered in the server");
                    }
                } catch (Exception E) {
                    logger.log(Level.SEVERE, "Error occured during the proccess of ServerSetStatus message in server", "" + E);

                } finally {
                    objServerSetStatus = null;
                    objClientGr = null;
                }

                break;
            default:
                throw new AssertionError();
        }
        current = null;
        current_Message = null;
    }

    private void sendToAllConnectedClientInGroup(Collection<ConnectedClient> recepients, Message message) {
        for (ConnectedClient recipient : recepients) {
            if (recipient != null) {
                recipient.receive(message);
            }

        }
        recepients = null;
    }

    private void sendToAllRecepientsInGroup(String loginIDOfSender, Collection<ConnectedClient> recepients, Message message) {
        //recepients.remove(sender);
        ConnectionStub stub;
        for (ConnectedClient recipient : recepients) {
            stub = (ConnectionStub) recipient.getObjConnectionStub();
            if (recipient != null && (!stub.getLoginIdOrServerName().toString().equals(loginIDOfSender))) {
                recipient.receive(message);
            }
            recipient = null;
        }
        recepients = null;
        stub = null;
    }

    private void sendToAllConnectedServer(Message message) {
        Collection<ConnectedServer> objInBoundConnectedServers = INBOUND_SERVERS.getAllConnectedServers();
        Collection<ConnectedServer> objOutBoundConnectedServers = OUTBOUND_SERVERS.getAllConnectedServers();

        for (ConnectedServer inBoundServer : objInBoundConnectedServers) {
            inBoundServer.receive(message);
        }
        for (ConnectedServer outBoundServer : objOutBoundConnectedServers) {
            outBoundServer.receive(message);
        }
        objInBoundConnectedServers = null;
        objOutBoundConnectedServers = null;
    }

    @Override
    public void processScheduledStatuses() {
        if (((new Date().getTime() - lastScheduleStatusProcessTime.getTime()) / 1000) > 59) {
            try {
                objConnectionMYSQL.close();
            } catch (SQLException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            objConnectionMYSQL = null;
            //TODO - PP  <Done> processScheduledStatuses() [Memory server --> public void processScheduledStatuses()] 
            logger.log(Level.FINEST, "\n\nProcess Scheduled Statuses ->");
            ResultSet rs = null;
            if (isMaster) {//Master Server
                try {
                    rs = executeQuery("call sp_get_all_loginids_exp_sch_statuses_MASTER__ServerName('" + strServerName + "')");
                } catch (SQLException e) {
                    Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "Problem during executing stored procedure sp_get_all_loginids_exp_sch_statuses_MASTER__ServerName", e);
                }
                this.recieveExpiredScheduledStatuses(rs);
                try {
                    rs = executeQuery("call sp_get_all_active_sch_statuses_MASTER__ServerName__LstProTm('" + strServerName + "','" + lastScheduleStatusProcessTime + "')");
                } catch (SQLException ex) {
                    Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "Problem during executing stored procedure sp_get_all_active_sch_statuses_MASTER__ServerName__LstProTm", ex);
                }
                lastScheduleStatusProcessTime = new Timestamp(new Date().getTime());
                this.recieveActivatedScheduledStatuses(rs);

            } else { //Normal Server     
                try {
                    rs = executeQuery("call sp_get_all_loginids_exp_sch_statuses_NORMAL__ServerName('" + strServerName + "')");
                } catch (SQLException ex) {
                    Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "Problem during executing stored procedure sp_get_all_loginids_exp_sch_statuses_NORMAL__ServerName", ex);
                }
                this.recieveExpiredScheduledStatuses(rs);

                try {
                    rs = executeQuery("call sp_get_all_active_sch_statuses_NORMAL__ServerName__LstProTm('" + strServerName + "','" + lastScheduleStatusProcessTime + "')");
                } catch (SQLException e) {
                    Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "Problem during executing sp_get_all_active_sch_statuses_NORMAL__ServerName__LstProTm", e);
                }
                lastScheduleStatusProcessTime = new Timestamp(new Date().getTime());
                this.recieveActivatedScheduledStatuses(rs);
            }
            rs = null;
        }
    }

    private void serverDisconnection(String serverName) {
        //method made by AJ
        ResultSet rs = null;
        if (isMaster) {//master
            try {
                /*
                 * 1. call stored procedure to update server Details()-->sp_server_disconnection_server_updation__serverName
                 * 2. Call Stored procedure to update and retrieve client details-->` sp_server_disconnection_client_updation__serverName`
                 */
                executeQuery("call sp_server_disconnection_server_updation__serverName('" + serverName + "')");
                rs = executeQuery("call sp_server_disconnection_client_updation_Master__serverName('" + serverName + "')");
            } catch (SQLException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            /*
             * not master. process server disconnection+
             *  Call Stored procedure retrieve client details
             */
            try {
                rs = executeQuery("call sp_server_disconnection_Normal__serverName('" + serverName + "')");
            } catch (SQLException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Map<String, Collection<ContactAction>> groupContactsMapNew = new HashMap<>();
        Map<String, Collection<ContactAction>> groupContactsMapOld = new HashMap<>();
        Collection<ContactAction> newContactAction;
        Collection<ContactAction> oldContactAction;
        ContactAction actionNew;
        ContactAction actionOld;
        try {
            if (rs != null) {
                while (rs.next()) {
                    String groupName = rs.getString(2);
                    newContactAction = groupContactsMapNew.get(groupName);
                    oldContactAction = groupContactsMapOld.get(groupName);
                    if (newContactAction == null && oldContactAction == null) {
                        actionNew = new ContactAction(ContactAction.Action.REMOVE, rs.getString(1), rs.getString(3));
                        actionOld = new ContactAction(ContactAction.Action.REMOVE, rs.getString(1));
                        newContactAction = new ArrayList<>();
                        oldContactAction = new ArrayList<>();
                        groupContactsMapOld.put(groupName, oldContactAction);
                        groupContactsMapNew.put(groupName, newContactAction);
                    } else {
                        actionNew = new ContactAction(ContactAction.Action.REMOVE, rs.getString(1), rs.getString(3));
                        actionOld = new ContactAction(ContactAction.Action.REMOVE, rs.getString(1));
                    }
                    newContactAction.add(actionNew);
                    oldContactAction.add(actionOld);
                    newContactAction.add(actionNew);
                    oldContactAction.add(actionOld);
                    groupName = null;
                }
                Set<String> groupNames = groupContactsMapNew.keySet();
                for (String groupName : groupNames) {
                    Group groupFor = groups.get(groupName);
                    ContactListMessage clm = new ContactListMessage(groupContactsMapOld.get(groupName));
                    ContactList2Message clm2 = new ContactList2Message(groupContactsMapNew.get(groupName));
                    if (groupFor != null) {
                        Collection<ConnectedClient> clients1 = groupFor.getAllConnectedClient();
                        for (ConnectedClient client2 : clients1) {
                            if (client2.getProductName().toString().contentEquals("none")) {
                                client2.receive(clm);
                            } else {
                                client2.receive(clm2);
                            }
                            client2 = null;
                        }
                        clients1 = null;
                    }
                    groupName = null;
                    groupFor = null;
                    clm = null;
                    clm2 = null;
                }
                groupNames = null;
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        rs = null;
        groupContactsMapNew = null;
        groupContactsMapOld = null;
        newContactAction = null;
        oldContactAction = null;
        actionNew = null;
        actionOld = null;

    }

    private void recieveExpiredScheduledStatuses(ResultSet rs) {
        try {
            if (rs.next()) {
                rs.beforeFirst();
                Map<String, StatusListMessage> statusListmessages = new HashMap<>();
                Map<String, ServerStatusListMessage> ServerStatusListmessages = new HashMap<>();
                String groupName;
                String extension;
                StatusListMessage statusListMessage;
                ServerStatusListMessage serverStatusListMessage;
                Set<Map.Entry<String, StatusListMessage>> groupMessages;
                while (rs.next()) {
                    groupName = rs.getString("Group");
                    extension = rs.getString("LoginID");
                    if (groups.containsKey(groupName)) {
                        statusListMessage = (StatusListMessage) statusListmessages.get(groupName);
                        if (statusListMessage == null) {
                            statusListMessage = new StatusListMessage();
                            statusListmessages.put(groupName, statusListMessage);
                        }
                        statusListMessage.addStatus(extension, null, null);
                    }
                    serverStatusListMessage = (ServerStatusListMessage) ServerStatusListmessages.get(groupName);
                    if (serverStatusListMessage == null) {
                        serverStatusListMessage = new ServerStatusListMessage(groupName);
                        ServerStatusListmessages.put(groupName, serverStatusListMessage);
                    }
                    serverStatusListMessage.addStatus(extension, null, null);
                }
                if (statusListmessages != null) {
                    groupMessages = statusListmessages.entrySet();
                    for (Map.Entry<String, StatusListMessage> entry : groupMessages) {
                        this.sendToAllConnectedClientInGroup(groups.get(entry.getKey()).getAllConnectedClient(), entry.getValue());
                    }
                }
                for (ServerStatusListMessage message : ServerStatusListmessages.values()) {
                    this.sendToAllConnectedServer(message);
                }

                rs.close();
                for (String st : statusListmessages.keySet()) {
                    statusListmessages.remove(st);
                    st = null;
                }
                statusListmessages = null;
                for (String st : ServerStatusListmessages.keySet()) {
                    ServerStatusListmessages.remove(st);
                    st = null;
                }
                ServerStatusListmessages = null;

                groupMessages = null;
                serverStatusListMessage = null;
                statusListMessage = null;
                extension = null;
                groupName = null;
            } else {
                rs.close();
            }
        } catch (SQLException e) {
            Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "SQL exception with expired scheduled statuses ", e);
        }
        rs = null;
    }

    @Override
    public void unregister(Connection paramClientOrServer) {
        //TODO - AJ <Done> unregister() [Memory server --> public void unregister(Connection client)]
        logger.log(Level.INFO, "\n\nUnregister -> " + paramClientOrServer.toString());
        ConnectionStub connectionStub = (ConnectionStub) paramClientOrServer;

        CharSequence groupOrServerType = connectionStub.getGroupOrServerType();

        if (groupOrServerType != null) {
            if (INBOUND_SERVERS.containsConnectedServer(connectionStub.getLoginIdOrServerName().toString())) {
                String serverName = connectionStub.getLoginIdOrServerName().toString();
                INBOUND_SERVERS.removeConnectedServer(serverName);
                serverDisconnection(serverName);
                serverName = null;
            } else if (OUTBOUND_SERVERS.containsConnectedServer(connectionStub.getLoginIdOrServerName().toString())) {
                String serverName = connectionStub.getLoginIdOrServerName().toString();
                OUTBOUND_SERVERS.removeConnectedServer(serverName);
                this.isMaster = OUTBOUND_SERVERS.isEmpty();
                serverDisconnection(serverName);
                serverName = null;
            } else {
                Group group = groups.get(groupOrServerType);
                String clientName = connectionStub.getLoginIdOrServerName().toString();
                if (group == null) {
                    logger.log(Level.WARNING, "Could not find group for client {0}", connectionStub);
                    return;
                }
                ConnectedClient extension = group.removeConnectedClient(clientName);

                if (extension != null) {
                    String productName = extension.getProductName().toString();
                    try {
                        executeQuery("call sp_client_disconnection_client_updation__LoginId('" + clientName + "')");
                    } catch (SQLException ex) {
                        Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "SQL exception while unregistering", ex);
                    }
                    ContactListMessage contactListMessage;
                    ServerContactList serverContactList;
                    if (productName == null) {
                        ContactAction action = new ContactAction(ContactAction.Action.REMOVE, clientName);
                        contactListMessage = new ContactListMessage(action);
                        serverContactList = new ServerContactList(groupOrServerType, action);
                        action = null;
                    } else {
                        ContactAction action = new ContactAction(ContactAction.Action.REMOVE, clientName, productName);
                        contactListMessage = new ContactListMessage(action);
                        serverContactList = new ServerContactList(groupOrServerType, action);
                        action = null;
                    }
                    sendToAllConnectedServer(serverContactList);
                    sendToAllConnectedClientInGroup(group.getAllConnectedClient(), contactListMessage);
                    productName = null;
                    contactListMessage = null;
                    serverContactList = null;
                } else {
                    logger.log(Level.WARNING, "Unregister client that was not connected");
                }
                group = null;
                clientName = null;
                extension = null;
            }
        } else {

            logger.log(Level.WARNING, "groupOrServerType of ConnectionStub not set and is null");
        }
        //  throw new UnsupportedOperationException("Not supported yet.");
        connectionStub = null;
        groupOrServerType = null;
    }

    private void recieveActivatedScheduledStatuses(ResultSet rs) {
        try {
            if (rs.next()) {
                rs.beforeFirst();
                Map<String, StatusListMessage> statusListmessages = new HashMap<>();
                Map<String, ServerStatusListMessage> ServerStatusListmessages = new HashMap<>();
                String groupName;
                String extension;
                String status;
                Date start;
                StatusListMessage statusListMessage;
                ServerStatusListMessage serverStatusListMessage;
                Set<Map.Entry<String, StatusListMessage>> groupMessages;
                while (rs.next()) {
                    groupName = rs.getString("Group");
                    extension = rs.getString("LoginID");
                    status = rs.getString("StatusString");
                    start = new Date(rs.getTimestamp("StartDateTime").getTime());
                    if (groups.containsKey(groupName)) {
                        statusListMessage = (StatusListMessage) statusListmessages.get(groupName);
                        if (statusListMessage == null) {
                            statusListMessage = new StatusListMessage();
                            statusListmessages.put(groupName, statusListMessage);
                        }
                        statusListMessage.addStatus(extension, status, start);
                    }
                    serverStatusListMessage = (ServerStatusListMessage) ServerStatusListmessages.get(groupName);
                    if (serverStatusListMessage == null) {
                        serverStatusListMessage = new ServerStatusListMessage(groupName);
                        ServerStatusListmessages.put(groupName, serverStatusListMessage);
                    }
                    serverStatusListMessage.addStatus(extension, status, start);
                }
                if (statusListmessages != null) {
                    groupMessages = statusListmessages.entrySet();
                    for (Map.Entry<String, StatusListMessage> entry : groupMessages) {
                        this.sendToAllConnectedClientInGroup(groups.get(entry.getKey()).getAllConnectedClient(), entry.getValue());
                    }
                }
                for (ServerStatusListMessage message : ServerStatusListmessages.values()) {
                    this.sendToAllConnectedServer(message);
                }
                rs.close();
                statusListmessages = null;
                ServerStatusListmessages = null;
                groupMessages = null;
                serverStatusListMessage = null;
                statusListMessage = null;
                start = null;
                status = null;
                extension = null;
                groupName = null;
            } else {
                rs.close();
            }
        } catch (SQLException e) {
            Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, "SQL exception with processing activated Scheduled statuses", e);
        }
        rs = null;
    }

    @Override
    public void sendServerKeepAliveMessage() {
        //TODO - AJ <Done>  sendServerKeepAliveMessage() - new [Use intKeepAliveDuration][call Connected server.receive() to send a keep alive message to server]
        logger.log(Level.FINEST, "\n\nSend Server KeepAlive Message ->");
        if (intKeepAliveDuration < ((new Date().getTime() - lastKeepAliveSend.getTime()) / 1000)) {
            KeepAliveMessage keepAliveMessage = new KeepAliveMessage();
            this.sendToAllConnectedServer(keepAliveMessage);
            lastKeepAliveSend = new Date();
            keepAliveMessage = null;
        }
        //throw new UnsupportedOperationException("Not supported yet.");        
    }

    @Override
    public void shutdown() {
        //TODO - Unallotted [Memory server --> public void shutdown()]
        logger.log(Level.INFO, "\n\nServer shutdown ->");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void processDisconnection() {
        // TODO - PP & AJ  <Done> Process Disconnection both for Server & Client
        logger.log(Level.FINEST, "\n\nProcess Disconnection ->");

        // Client disconnection processing      
        if (!groups.isEmpty()) {
            for (Group groupPresent : groups.values()) {
                if (!groupPresent.isEmpty()) {
                    for (ConnectedClient client : groupPresent.getAllConnectedClient()) {
                        if (intKeepAliveDuration * 2 < (((new Date().getTime()) - ((ConnectionStub) client.getObjConnectionStub()).getLastMessageTime().getTime()) / 1000)) {
                            unregister(client.getObjConnectionStub());
                        }
                        client = null;
                    }
                }
                groupPresent = null;
            }
        }

//        //server disconnection processing
//        Collection<ConnectedServer> servers;
//        if (!INBOUND_SERVERS.isEmpty()) {
//            servers = INBOUND_SERVERS.getAllConnectedServers();
//            for (ConnectedServer server : servers) {
//                if (intKeepAliveDuration * 2 < ((new Date().getTime()) - ((ConnectionStub) server.getConnection()).getLastMessageTime().getTime())) {
//                    unregister(server.getConnection());
//                }
//            }
//        }
//        if (!OUTBOUND_SERVERS.isEmpty()) {
//            servers = OUTBOUND_SERVERS.getAllConnectedServers();
//            for (ConnectedServer server : servers) {
//                if (intKeepAliveDuration * 2 < ((new Date().getTime()) - ((ConnectionStub) server.getConnection()).getLastMessageTime().getTime())) {
//                    unregister(server.getConnection());
//                }
//            }
//        }
        //   throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getDEFAULT_BUFFER_SIZE() {
        return DEFAULT_BUFFER_SIZE;
    }

    public KempCodec getObjKempCodec() {
        return objKempCodec;
    }

    /**
     * All message types
     */
    public enum Message_Types {

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
    public enum KeepAlive_Message {

        KeepAlive;
        //From client to server
        //From server to server
        //Used to keep the TCP connection active, should be ignored by the server
    }

    /**
     * All the Server to Server messages
     */
    public enum InterServer_Message {

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
    public enum Peer_Message {

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
        //Send a custom message to one or more users
    }

    /**
     * All the Client to Server messages
     */
    public enum Client_Message {

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
    public enum Server_Message {

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
    public enum Error_Message {

        INTERNAL_ERROR,
        OVERRIDE;
    }

    public ServerGroup getINBOUND_SERVERS() {
        return INBOUND_SERVERS;
    }

    public ServerGroup getOUTBOUND_SERVERS() {
        return OUTBOUND_SERVERS;
    }

    private void InitiaizeDatabaseOperations(String strConnectionURL) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        objConnectionMYSQL = DriverManager.getConnection(strConnectionURL);
        strConnectionURL = null;
    }

    /**
     * executeUpdate() method executes the update statements in the database.
     *
     * @param strSqlStatement - SQL update query that which is to be executed.
     * @throws SQLException
     */
    public void executeUpdate(String strSqlStatement) throws SQLException {
        if (objConnectionMYSQL == null) {
            try {
                InitiaizeDatabaseOperations(mysqlString);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try (PreparedStatement ps = objConnectionMYSQL.prepareStatement(strSqlStatement)) {
            ps.executeUpdate();
        }
    }

    /**
     * executeQuery() method executes an sql query passed to it and returns a
     * ResultSet that contains the result to the query.
     *
     * @param stringSqlStatement
     * @return ResultSet object which is the result of the sql query(argument to
     * the method)
     * @throws SQLException
     */
    public ResultSet executeQuery(String stringSqlStatement) throws SQLException {
        if (objConnectionMYSQL == null) {
            try {
                InitiaizeDatabaseOperations(mysqlString);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return objConnectionMYSQL.prepareStatement(stringSqlStatement).executeQuery();
    }

    public PreparedStatement getPrepared(String strSQL) throws SQLException {
        if (objConnectionMYSQL == null) {
            try {
                InitiaizeDatabaseOperations(mysqlString);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(UnityIMPServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return objConnectionMYSQL.prepareStatement(strSQL);
    }
}
