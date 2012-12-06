package com.kakapo.unity.message.interserver;

import com.kakapo.unity.message.ManualStatus;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

/**
 * this class represents the messages sent from a foreign server to the unity
 * server when a unity client sets an ad-hoc status or a scheduled status
 * notification has to be send to the clients
 *
 * @author amith.bharathan
 */
public class ServerSetStatus extends InterServerMessage {

    /**
     * This filed represents type of the message which is ServerSetStatus
     */
    public final String _Command = "ServerSetStatus";
    private final String group;
    private final String extension;
    private final ManualStatus status;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");

    /**
     * Constructor to create an object of ServerSetStatus
     *
     * @param group - group to which presence details have to be broadcasted
     * @param extension - extension whose presence details have changed
     * @param status - new status of the unity client connected to the foreign
     * server
     * @param override
     */
    public ServerSetStatus(String group, String extension, String status, Date startDate, boolean override) {
        super("ServerSetStatus");
        this.group = group;
        this.extension = extension;
        this.status = new ManualStatus(status, startDate, override);
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public String getExtension() {
        return extension;
    }

    public String getGroup() {
        return group;
    }

    public ManualStatus getStatus() {
        return status;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: ServerSetStatus\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Group: " + this.group + "\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("ExtensionStatus: " + new StringBuilder().append(this.extension).append(" ").append(this.status.getName()).append(" ").append(this.dateFormat.format(this.status.getStart()).toString())+"\n").getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}
