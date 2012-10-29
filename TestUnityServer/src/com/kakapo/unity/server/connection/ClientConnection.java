package com.kakapo.unity.server.connection;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.server.ServerMain.ClientCommandMessage;

public class ClientConnection implements Connection{
    
     private final String loginId = "";
     private final String groupName = "";
     private final String productName = "";
     
    @Override
    public void receive(Message paramMessage) {
        
        /*TODO-GK CODE for the ClientConnection obj to receive an already created message either 
         in processMessage() or in processScheduledStatuses()*/
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void processMessage(Message clientCommandMessage) {
        String current = clientCommandMessage.getCommand();
        ClientCommandMessage currentClientCommandMessage = ClientCommandMessage.valueOf(current);

        switch (currentClientCommandMessage) {
            case Register:
                
                /*TODO-GK Process Register message */
                
                break;
            case KeepAlive:
                
                /*TODO-GK Process KeepAlive message */
                
                break;
            case Message:
                
                /*TODO-GK Process Message message*/
                
                break;
            case Custom:
                
                /*TODO-GK Process Custom message */
                
                break;
            case SetStatus:
                
                /*TODO-GK Process SetStatus message */
                
                break;
            case AddScheduledStatus:
                
                /*TODO-GK Process AddScheduledStatus message */
                
                break;
            case ClearStatus:
                
                /*TODO-GK Process ClearStatus message */
                
                break;
            case ListScheduledStatuses:
                
                /*TODO-GK Process ListScheduledStatuses message */
                
                break;
            case Register2:
                
                /*TODO-GK Process Register2 message */
                
                break;
            case Message2:
                
               /*TODO-GK Process Message2 message */
                
                break;
            default:
                throw new AssertionError();
        }
    }
}
