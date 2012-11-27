package com.kakapo.unity.server;

import com.kakapo.unity.connection.ConnectedServer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGroup {

    private final SERVER_TYPE serverType;
    private Map<String, ConnectedServer> objConnectedServerHashMap = new ConcurrentHashMap<>();

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
        //TODO - AKB <Done> Add server
        objConnectedServerHashMap.put(strServerName, connectedServer);
    }

    public boolean isEmpty() {
        return this.objConnectedServerHashMap.isEmpty();
    }

    /**
     * All Server.Error messages
     */
    public enum SERVER_TYPE {

        INBOUND_SERVER_ENUM,
        OUTBOUND_SERVER_ENUM;
    }
}