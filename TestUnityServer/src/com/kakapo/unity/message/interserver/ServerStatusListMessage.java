package com.kakapo.unity.message.interserver;

import com.kakapo.unity.message.ContactAction;
import com.kakapo.unity.message.ExtensionStatus;
import com.kakapo.unity.message.Status;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class ServerStatusListMessage extends InterServerMessage {

    public final String _Command = "ServerStatusList";
    private final String group;
    private List<ExtensionStatus> statuses = new ArrayList<ExtensionStatus>();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
    private final String DEFAULT_STATUS = "none";

    public ServerStatusListMessage(String group) {
        super("ServerStatusList");
        this.group = group;
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void addStatus(String extension, Status status) {
        this.statuses.add(new ExtensionStatus(extension, status));
    }

    public void addStatus(String extension, String status, Date since) {
        this.statuses.add(new ExtensionStatus(extension, status, since));
    }

    public List<ExtensionStatus> getStatuses() {
        return this.statuses;
    }

    public void setStatuses(List<ExtensionStatus> statuses) {
        this.statuses = statuses;
    }

    public String getGroup() {
        return group;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: ServerStatusList\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage.writeBytes(("Group: " + this.group + "\n").getBytes(CharsetUtil.UTF_8));
        for (ExtensionStatus es : statuses) {
            encodedMessage.writeBytes(("ExtensionStatus: " + new StringBuilder().append(es.getExtension()).append(" ").append(es.getStatus() == null ? DEFAULT_STATUS : new StringBuilder().append(es.getStatus()).append(" ").append(this.dateFormat.format(es.getSince())).toString()).toString() + "\n").getBytes(CharsetUtil.UTF_8));
        }
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}