package com.kakapo.unity.server.connection;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.server.ServerMain;

public class ServerConnection implements com.kakapo.unity.server.connection.Connection{

    @Override
    public void receive(Message paramMessage) {
        
        /*TODO-Amith CODE for the ServerConnection obj to receive an already created message either 
         in processMessage() or in processScheduledStatuses()*/
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void processMessage(Message severCommandMessage) {
        String current = severCommandMessage.getCommand();
        ServerMain.ServerCommandMessage currentSeverCommandMessage = ServerMain.ServerCommandMessage.valueOf(current);

        switch (currentSeverCommandMessage) {
            case KeepAlive:
                
                /*TODO-Amith Process KeepAlive message*/
                
                break;
            case ServerRegister:
                
                /*TODO-Amith Process ServerRegister message*/
                
                break;
            case ServerContactList:
                
                /*TODO-Amith Process ServerContactList message*/
                
                break;
            case ServerMessage:
                
                /*TODO-Amith Process ServerMessage message*/
                
                break;
            case ServerStatusList:
                
                /*TODO-Amith Process ServerStatusList message*/
                
                break;
            case ServerSetStatus:
                
                /*TODO-Amith Process ServerSetStatus message*/
                
                break;
            default:
                throw new AssertionError();
        }

    }

}
