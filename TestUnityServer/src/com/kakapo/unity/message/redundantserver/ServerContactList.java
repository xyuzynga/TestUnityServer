/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.message.redundantserver;

import com.kakapo.unity.message.server.ContactAction;
import java.util.Collection;
import java.util.HashSet;

/**
 * This class is to represent the objects of a ServerContactList message originating from foreign servers to notify the unity IM&P server when a new client registers or de registers from the system
 * @author amith.bharathan
 */

public class ServerContactList extends ServerMessage{
   /**
    * This field used to represent the type of the message which is ServerContactList
    */
     public static final String COMMAND = "ServerContactList";
     /**
      * This field is used to represent the group of the newly registered or unregistered client as received from the ServerMessage
      */
     private final CharSequence group;
     /**
      * This field represents the set of users who have registered or unregistered to the unity IM&P server.
      */
     private final Collection<ContactAction> extensions;

    public ServerContactList(CharSequence group, Collection<ContactAction> extensions) {
        super(COMMAND);
        this.group = group;
        this.extensions = extensions;
    }
/**
 * Constructor to create an object of type ServerMessage
 * @param group  - group of the users who have logged in to the system
 * @param extension - extension which has registered or de registered from the foreign IM&P Server
 */
    public ServerContactList(CharSequence group,  ContactAction extension) {
        super(COMMAND);
        this.group = group;
        extensions=new HashSet<>() ;
        extensions.add(extension);
    }
    
     
     
}
