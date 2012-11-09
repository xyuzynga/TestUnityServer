package com.kakapo.unity.server;

import com.kakapo.unity.connection.ConnectedClient;
import com.kakapo.unity.connection.ConnectedServer;
import com.kakapo.unity.connection.Connection;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.interserver.InterServerMessage;

public abstract interface Server {

    public abstract boolean startup();

    public abstract void processClientRegisteration(ConnectedClient paramClient);

    public abstract void processServerRegisteration(ConnectedServer paramClient);

    public abstract void processClientMessage(Connection paramClient, Message paramClientMessage);

    public abstract void processInterServerMessage(Connection paramServer, InterServerMessage paramInterServerMessage);

    public abstract void processScheduledStatuses();

    public abstract void processDisconnection();

    public abstract void sendServerKeepAliveMessage();

    public abstract void unregister(Connection paramClientOrServer);

    public abstract void shutdown();
}