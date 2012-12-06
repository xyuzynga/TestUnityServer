package com.kakapo.unity.message.interserver;

import com.kakapo.unity.message.peer.CustomMessage;
import com.kakapo.unity.message.peer.PeerMessage;
import com.kakapo.unity.message.peer.TextMessage;
import com.kakapo.unity.message.peer.TextMessage2;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

/**
 *
 * @author amith.bharathan
 */
public class ServerIM extends InterServerMessage {

    /**
     * This filed represents type of the message which is InterServerMessage
     */
    public final String _Command = "ServerMessage";
    private final CharSequence _group;
    private final PeerMessage _peerMessage;

    /**
     * Constructor to create an object of ServerIM
     *
     * @param group - Group to which the IM message has to be send
     * @param peerMessage - the message that has to be sent to the recipients
     */
    public ServerIM(CharSequence group, PeerMessage peerMessage) {
        super("ServerMessage");
        this._group = group;
        this._peerMessage = peerMessage;
    }

    public CharSequence getGroup() {
        return _group;
    }

    public PeerMessage getPeerMessage() {
        return _peerMessage;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: ServerMessage\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Group: " + _group + "\n").getBytes(CharsetUtil.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (String extension : _peerMessage.getExtensions()) {
            builder.append(extension);
            builder.append(", ");
        }
        if (_peerMessage instanceof TextMessage) {
            TextMessage tm = (TextMessage) _peerMessage;
            encodedMessage.writeBytes("Command: Message\n".getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Id: " + _peerMessage.getId() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Sender: " + _peerMessage.getSender() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Share: " + tm.isShare() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Extensions: " + builder + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Length: " + tm.getLength() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes((tm.getText() + "\n").getBytes(CharsetUtil.UTF_8));
        } else if (_peerMessage instanceof TextMessage2) {
            TextMessage2 tm2 = (TextMessage2) _peerMessage;
            encodedMessage.writeBytes("Command: Message2\n".getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Id: " + _peerMessage.getId() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Sender: " + _peerMessage.getSender() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Share: " + tm2.getDateTime() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Extensions: " + builder + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Length: " + tm2.getLength() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes((tm2.getText() + "\n").getBytes(CharsetUtil.UTF_8));
        } else if (_peerMessage instanceof CustomMessage) {
            CustomMessage cm = (CustomMessage) _peerMessage;
            encodedMessage.writeBytes("Command: Custom\n".getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Id: " + _peerMessage.getId() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Sender: " + _peerMessage.getSender() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Share: " + cm.getDateTime() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Extensions: " + builder + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes(("Length: " + cm.getLength() + "\n").getBytes(CharsetUtil.UTF_8));
            encodedMessage.writeBytes((cm.getText() + "\n").getBytes(CharsetUtil.UTF_8));
        }
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;

    }
}
