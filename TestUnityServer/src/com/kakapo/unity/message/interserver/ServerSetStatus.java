package com.kakapo.unity.message.interserver;

import com.kakapo.unity.message.ManualStatus;
import java.util.Date;

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
    public final String COMMAND = "ServerSetStatus";
    private final String group;
    private final String extension;
    private final ManualStatus status;

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
}
