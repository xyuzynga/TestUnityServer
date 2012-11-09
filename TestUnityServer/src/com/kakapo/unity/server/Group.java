package com.kakapo.unity.server;

import com.kakapo.unity.connection.ConnectedClient;
import com.kakapo.unity.message.server.error.OverrideMessage;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Group {

    private final String groupName;
    private Map<String, ConnectedClient> objConnectedClientHashMap = new HashMap<>();

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
        //<<--Done-->>TODO - GK Add Client
        ConnectedClient connectedClientWithSameLoginID=objConnectedClientHashMap.put(loginID, client);
        if(connectedClientWithSameLoginID!=null)
        {
            connectedClientWithSameLoginID.receive(new OverrideMessage());
            connectedClientWithSameLoginID.getObjConnectionStub().disconnect();
        }              
    }

   
    public boolean isEmpty() {
        return this.objConnectedClientHashMap.isEmpty();
    }
}
