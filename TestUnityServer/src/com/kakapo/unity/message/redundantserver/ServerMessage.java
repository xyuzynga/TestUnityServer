/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.message.redundantserver;

import com.kakapo.unity.message.Message;

/**
 *this class represents the messages sent from a foreign server to the unity server 
 * @author amith.bharathan
 */
public class ServerMessage extends Message{
    
   
    /**
     * constructor for ServerMessage
     * @param command - command associated with the ServerMessage
     */
    public ServerMessage(String command)
    {
        super(command);
    }
}

   
