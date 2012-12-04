package com.kakapo.unity.server;

import com.kakapo.unity.connection.ConnectedClient;
import com.kakapo.unity.message.server.error.OverrideMessage;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Group {

    private final String groupName;
    private Map<String, ConnectedClient> objConnectedClientHashMap = new ConcurrentHashMap<>();

    public Group(String groupName) {
        this.groupName = groupName;
    }

    public String getName() {
        return this.groupName;
    }

    public boolean containsConnectedClient(String loginId) {
        return this.objConnectedClientHashMap.containsKey(loginId);
    }

    public ConnectedClient removeConnectedClient(String loginId) {
        return this.objConnectedClientHashMap.remove(loginId);
    }

    public ConnectedClient getConnectedClient(String loginId) {
        return this.objConnectedClientHashMap.get(loginId);
    }

    public Collection<ConnectedClient> getAllConnectedClient() {
        return Collections.unmodifiableCollection(this.objConnectedClientHashMap.values());
    }

    public void addClient(String loginID, ConnectedClient client) {
        //TODO - GK <Done> Add Client

        ConnectedClient connectedClientWithSameLoginID = objConnectedClientHashMap.get(loginID);
         if (connectedClientWithSameLoginID != null) {
            try {
                connectedClientWithSameLoginID.receive(new OverrideMessage());
                objConnectedClientHashMap.remove(loginID);
                Logger.getLogger(ConnectedClient.class.getName()).log(Level.FINE, "\n\ndisconnect() called from addClient()");
            } catch (Exception e) {
                System.out.println("EXCEPTION WHILE OVERRIDING " + e);
            }
        }

        objConnectedClientHashMap.put(loginID, client);
    }

    public boolean isEmpty() {
        return this.objConnectedClientHashMap.isEmpty();
    }
}
