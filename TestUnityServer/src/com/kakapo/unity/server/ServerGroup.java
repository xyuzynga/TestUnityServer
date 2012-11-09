package com.kakapo.unity.server;

import com.kakapo.unity.connection.ConnectedServer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServerGroup {

    private final SERVER_TYPE serverType;
    private Map<String, ConnectedServer> objConnectedServerHashMap = new HashMap<>();

    public ServerGroup(SERVER_TYPE serverType) {
        this.serverType = serverType;
    }

    public String getServerType() {
        return this.serverType.toString();
    }

    public boolean containsConnectedServer(String serverName) {
        return this.objConnectedServerHashMap.containsKey(serverName);
    }

    public ConnectedServer removeConnectedServer(String serverName) {
        return this.objConnectedServerHashMap.remove(serverName);
    }

    public ConnectedServer getConnectedServer(String serverName) {
        return this.objConnectedServerHashMap.get(serverName);
    }

    public Collection<ConnectedServer> getAllConnectedServers() {
        return Collections.unmodifiableCollection(this.objConnectedServerHashMap.values());
    }

    public void addServer(String strServerName, ConnectedServer connectedServer) {
        //TODO - AKB Add server
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean isEmpty() {
        return this.objConnectedServerHashMap.isEmpty();
    }

    /**
     * All Server.Error messages
     */
    public static enum SERVER_TYPE {

        INBOUND_SERVER_ENUM,
        OUTBOUND_SERVER_ENUM;
    }
}