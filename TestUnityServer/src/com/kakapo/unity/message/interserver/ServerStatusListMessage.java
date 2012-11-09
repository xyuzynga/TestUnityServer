package com.kakapo.unity.message.interserver;

import com.kakapo.unity.message.ExtensionStatus;
import com.kakapo.unity.message.Status;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerStatusListMessage extends InterServerMessage {

    public final String COMMAND = "ServerStatusList";
    private final String group;
    private List<ExtensionStatus> statuses = new ArrayList<ExtensionStatus>();

    public ServerStatusListMessage(String group) {
        super("ServerStatusList");
        this.group = group;
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
}