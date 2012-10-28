/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.message.server;

/**
 *this class represents the messages sent from a foreign server to the unity server when a unity client sets an ad-hoc status or a scheduled status notification has to be send to the clients
 * @author amith.bharathan
 */
public class ServerSetStatus extends ServerMessage{
 /**
  * This filed represents type of the message which is ServerSetStatus
  */
   public static final String COMMAND="ServerSetStatus";
   private final CharSequence group;
   private final CharSequence extension;
   private final CharSequence status;
   private final boolean override;
/**
 * Constructor to create an object of ServerSetStatus
 * @param group - group to which presence details have to be broadcasted
 * @param extension - extension whose presence details have changed
 * @param status - new status of the unity client connected to the foreign server
 * @param override 
 */
    public ServerSetStatus(CharSequence group, CharSequence extension, CharSequence status, boolean override) {
        super(COMMAND);
        this.group = group;
        this.extension = extension;
        this.status = status;
        this.override = override;
    }
/**
 * function to get type of the message
 * @return  type of the message
 */
    public static String getCOMMAND() {
        return COMMAND;
    }
/**
 * getter for group
 * @return group to which status update has to be notified 
 */
    public CharSequence getGroup() {
        return group;
    }
/**
 * getter for extension
 * @return extension whose presence status was updated
 */
    public CharSequence getExtension() {
        return extension;
    }
/**
 * function to get the status
 * @return status
 */
    public CharSequence getStatus() {
        return status;
    }

   
}
