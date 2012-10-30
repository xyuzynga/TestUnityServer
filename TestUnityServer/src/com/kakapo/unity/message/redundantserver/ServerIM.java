/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.message.redundantserver;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author amith.bharathan
 */
public class ServerIM extends ServerMessage{
   /**
    * This filed represents type of the message which is ServerMessage
    */
    public static final String COMMAND="ServerMessage";
    private final CharSequence group;
    private final CharSequence id;
    private final CharSequence sender;
    private final boolean share;
    private Set<CharSequence> extensions;
    private final int  length;
    private final CharSequence message;
/**
 * Constructor to create an object of ServerIM
 * @param group - Group to which the IM message has to be send
 * @param id - unique id of the sender
 * @param sender - Extension of the sender
 * @param share - boolean value which tells if the message has to be send to the recipients or not
 * @param extensions - extensions to which the message has o be relayed by the server
 * @param length - length of the Message as received from ServerMessage
 * @param message - the message that has to be sent to the recipients
 */
    public ServerIM(CharSequence group, CharSequence id, CharSequence sender, boolean share, Set<CharSequence> extensions, int length, CharSequence message) {
        super(COMMAND);
        this.group = group;
        this.id = id;
        this.sender = sender;
        this.share = share;
        this.extensions = extensions;
        this.length = length;
        this.message = message;
    }

    /**
 * Constructor to create an object of ServerIM
 * @param group - Group to which the IM message has to be send
 * @param id - unique id of the sender
 * @param sender - Extension of the sender
 * @param share - boolean value which tells if the message has to be send to the recipients or not
 * @param extension - extension to which the message has o be relayed by the server
 * @param length - length of the Message as received from ServerMessage
 * @param message - the message that has to be sent to the recipients
 */
    public ServerIM(CharSequence group, CharSequence id, CharSequence sender, boolean share, int length, CharSequence message,CharSequence extension) {
        super(COMMAND);
        this.group = group;
        this.id = id;
        this.sender = sender;
        this.share = share;
        this.length = length;
        this.message = message;
        this.extensions=new HashSet<>();
        extensions.add(extension);
    }

    /**
     * function to get the group of the sender
     * @return group of the sender
     */
    public CharSequence getGroup() {
        return group;
    }
/**
 * function to retrieve the unique ID of the sender
 * @return unique ID of the sender
 */
    public CharSequence getId() {
        return id;
    }
/**
 * function to retrieve the extension of the sender
 * @return extension of the sender
 */
    public CharSequence getSender() {
        return sender;
    }
/**
 * function to get the boolean value as received from the Message  
 * @return boolean value obtained from the message
 */
    public boolean isShare() {
        return share;
    }
/**
 * function to get set of all the Extensions to whom the instant message was sent
 * @return  set of all the Extensions to whom the instant message was sent
 */
    public Set<CharSequence> getExtensions() {
        return extensions;
    }
/**
 * function to get the length of the message 
 * @return length of the message
 */
    public int getLength() {
        return length;
    }
/**
 * function to get the message
 * @return the message extracted from the ServerMessage
 */
    public CharSequence getMessage() {
        return message;
    }
    
    
    
    
}
