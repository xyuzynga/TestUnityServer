package com.kakapo.unity.message.server;

import com.kakapo.unity.message.ContactAction;
import java.util.Collection;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class ContactList2Message extends ServerMessage {

    public final String _Command = "ContactList2";
    private final Collection<ContactAction> _actions;

    public ContactList2Message(Collection<ContactAction> actions) {
        super("ContactList2");
        this._actions = actions;
    }

    public ContactList2Message(ContactAction action) {
        this(new SingleItemSet<>(action));
    }

    public Collection<ContactAction> getActions() {
        return this._actions;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: ContactList2\n".getBytes(CharsetUtil.UTF_8));
        for (ContactAction contactAction : _actions) {
            encodedMessage.writeBytes(contactAction.toString().getBytes(CharsetUtil.UTF_8));
        }
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}