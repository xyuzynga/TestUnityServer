/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.message.interserver;

/**
 * this class represents the messages sent from a foreign server to the unity
 * server to register itself
 *
 * @author amith.bharathan
 */
public class ServerRegisterMessage extends InterServerMessage {

    /**
     * This filed represents type of the message which is ServerRegister
     */
    public static final String COMMAND = "ServerRegister";
    private final CharSequence serverName;

    /**
     * constructor to create an object of ServerRegisterMessage
     *
     * @param serverName - name of the foreign server
     */
    public ServerRegisterMessage(CharSequence serverName) {
        super(COMMAND);
        this.serverName = serverName;
    }

    /**
     * getter for server name
     *
     * @return server name
     */
    public CharSequence getServerName() {
        return serverName;
    }
}
