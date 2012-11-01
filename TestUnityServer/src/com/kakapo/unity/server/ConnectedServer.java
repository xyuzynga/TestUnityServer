package com.kakapo.unity.server;

import com.kakapo.unity.client.Connection;
import com.kakapo.unity.util.Objects;

public class ConnectedServer {

    private final String serverName;
    private transient Connection server;
    private boolean isDirectionIn;

    public ConnectedServer(String serverName, Connection connection, boolean isDirectionIn) {
        this.serverName = serverName;
        this.server = connection;
        this.isDirectionIn = isDirectionIn;
    }

    public String getServerName() {
        return serverName;
    }

    public Connection getConnection() {
        return server;
    }

    public boolean isIsDirectionIn() {
        return isDirectionIn;
    }

    public void setServer(Connection server) {
        this.server = server;
    }

    @Override
    public String toString() {
        return Objects.toString(this);
    }
}