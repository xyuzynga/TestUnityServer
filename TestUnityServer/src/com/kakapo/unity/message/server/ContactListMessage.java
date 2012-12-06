package com.kakapo.unity.message.server;

import com.kakapo.unity.message.ContactAction;
import java.util.Collection;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class ContactListMessage extends ServerMessage {

    public final String _Command = "ContactList";
    private final Collection<ContactAction> actions;

    public ContactListMessage(Collection<ContactAction> actions) {
        super("ContactList");
        this.actions = actions;
    }

    public ContactListMessage(ContactAction action) {
        this(new SingleItemSet<>(action));
    }

    public Collection<ContactAction> getActions() {
        return this.actions;
    }

    @Override
    public String toString() {
        String toReturn = "Command: ContactList\n";
        for (ContactAction contactAction : actions) {
            toReturn += contactAction.toString();
        }
        return toReturn;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: ContactList\n".getBytes(CharsetUtil.UTF_8));
        for (ContactAction contactAction : actions) {
            encodedMessage.writeBytes(contactAction.toString().getBytes(CharsetUtil.UTF_8));
        }
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}