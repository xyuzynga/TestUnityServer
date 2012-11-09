/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.message.interserver;

import com.kakapo.unity.message.peer.PeerMessage;

/**
 *
 * @author amith.bharathan
 */
public class ServerIM extends InterServerMessage {

    /**
     * This filed represents type of the message which is InterServerMessage
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

    public CharSequence getGroup() {
        return _group;
    }

    public PeerMessage getPeerMessage() {
        return _peerMessage;
    }
}
