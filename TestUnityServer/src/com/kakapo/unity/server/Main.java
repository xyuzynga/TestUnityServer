package com.kakapo.unity.server;

/**
 *
 * @author felix.vincent
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO-GK Server startup & code associated with Netty
    }

    private void processScheduledStatuses() {
        
        /*TODO-Prijo CODE for processing scheduled statuses*/
        
    }
    
    private void processDisconnection() {
        
        /*TODO-Abin CODE for processing disconnection events (Server & Client)*/
        
    }

    /**
     * All the Server to Server messages
     */
    public static enum ServerCommandMessage {

        KeepAlive,
        //From client to server
        //From server to server
        //Used to keep the TCP connection active, should be ignored by the server

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

        ServerSetStatus,
        //From server to server
        //Used to inform other servers [which then inform connected clients] that a connected client has set their ad-hoc status.
    }

    /**
     * All the Client to Server messages
     */
    public static enum ClientCommandMessage {

        Register,
        //From client to server
        //Used to register a connection with user details such as user id and group name.

        KeepAlive,
        //From client to server
        //From server to server
        //Used to keep the TCP connection active, should be ignored by the server

        Message,
        //From client to server
        //From server to client
        //Send an instant message to one or more users

        Custom,
        //From client to server
        //From server to client
        //Send an instant message to one or more users

        SetStatus,
        //From client to server
        //Set the ad-hoc status for a specified user

        AddScheduledStatus,
        //From client to server
        //Add a scheduled status for a specified user

        ClearStatus,
        //From client to server
        //Clear an ad-hoc or scheduled status for a specified user

        ListScheduledStatuses,
        //From client to server
        //Requests all scheduled statuses for the specified user and date range

        Register2,
        //From client to server
        //Used by clients using the new command set to register on the server

        Message2;
        //From client to server
        //From server to client
        //Used by clients using the new command
        //set to send instant messages, which are
        //then relayed through all servers.
    }

    /**
     * All the Server to Client messages
     */
    public static enum ServerResponseMessage {

        ContactList,
        //From server to client
        //Returns the “online” status of one or more users

        StatusList,
        //From server to client
        //Returns the current presence status of one of more users.

        Message,
        //From client to server
        //From server to client
        //Send an instant message to one or more users

        ScheduledStatusList,
        //From server to client
        //Response to the ListScheduledStatuses command. Lists all scheduled statuses that meet the params given in the command.

        Error,
        //From server to client
        //Used to inform a client that an error has occurred.

        ContactList2,
        //From server to client
        //Used to inform connected clients using
        //the new command set when another user
        //registers or deregisters.

        Message2;
        //From client to server
        //From server to client
        //Used by clients using the new command
        //set to send instant messages, which are
        //then relayed through all servers.
    }
}
