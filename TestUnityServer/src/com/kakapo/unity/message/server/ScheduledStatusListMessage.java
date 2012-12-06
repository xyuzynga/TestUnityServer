package com.kakapo.unity.message.server;

import com.kakapo.unity.message.ExtensionStatus;
import com.kakapo.unity.message.ScheduledStatus;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.TimeZone;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public class ScheduledStatusListMessage extends ServerMessage {

    public final String _Command = "ScheduledStatusList";
    private final Collection<ScheduledStatus> statuses;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");

    public ScheduledStatusListMessage(Collection<ScheduledStatus> statuses) {
        super("ScheduledStatusList");
        this.statuses = statuses;
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public Collection<ScheduledStatus> getStatuses() {
        return this.statuses;
    }

    @Override
    public ChannelBuffer getEncodedMessage() {
        ChannelBuffer encodedMessage = ChannelBuffers.dynamicBuffer();
        encodedMessage.writeBytes("Command: ScheduledStatusList\n".getBytes(CharsetUtil.UTF_8));
        for (ScheduledStatus ss : statuses) {
            encodedMessage.writeBytes(("ScheduledStatus: " + new StringBuilder().append(ss.getName()).append(" ").append(ss.getId()).append(" ").append(this.dateFormat.format(ss.getStart())).append(" ").append(this.dateFormat.format(ss.getEnd())).toString()+"\n").getBytes(CharsetUtil.UTF_8));
        }
        encodedMessage.writeBytes("\n".getBytes(CharsetUtil.UTF_8));
        encodedMessage = encodedMessage.slice(0, encodedMessage.writerIndex());
        return encodedMessage;
    }
}