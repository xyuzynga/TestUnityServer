/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.message.server;

import com.kakapo.unity.message.client.PeerMessage;

/**
 *
 * @author amith.bharathan
 */
public class ServerIM extends ServerMessage {

    /**
     * This filed represents type of the message which is ServerMessage
     */
    public static final String COMMAND = "ServerMessage";
    private final CharSequence _group;
    private final PeerMessage _peerMessage;

    /**
     * Constructor to create an object of ServerIM
     *
     * @param group - Group to which the IM message has to be send
     * @param peerMessage - the message that has to be sent to the recipients
     */
    public ServerIM(CharSequence group, PeerMessage peerMessage) {
        super(COMMAND);
        this._group = group;
        this._peerMessage = peerMessage;
    }

}
