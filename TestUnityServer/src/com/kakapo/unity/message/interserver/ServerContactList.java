/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kakapo.unity.message.interserver;

import com.kakapo.unity.message.ContactAction;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

/**
 * This class is to represent the objects of a ServerContactList message
 * originating from foreign servers to notify the unity IM&P server when a new
 * client registers or de registers from the system
 *
 * @author amith.bharathan
 */
public class ServerContactList extends InterServerMessage {

    /**
     * This field used to represent the type of the message which is
     * ServerContactList
     */
    public final String _Command = "ServerContactList";
    /**
     * This field is used to represent the group of the newly registered or
     * unregistered client as received from the InterServerMessage
     */
    private final CharSequence group;

    public CharSequence getGroup() {
        return group;
    }
    /**
     * This field represents the set of users who have registered or
     * unregistered to the unity IM&P server.
     */
    private final ContactAction _actions;

    public ContactAction getActions() {
        return _actions;
    }

    /**
     * Constructor to create an object of type InterServerMessage
     *
     * @param group - group of the users who have logged in to the system
     * @param action - extension which has registered or de registered from the
     * foreign IM&P Server
     */
    public ServerContactList(CharSequence group, ContactAction action) {
        super("ServerContactList");
        this.group = group;
        this._actions = action;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: ServerContactList\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Group: " + this.group + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(_actions.toString().getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}
