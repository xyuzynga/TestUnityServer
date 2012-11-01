package com.kakapo.unity.server;

import com.kakapo.unity.client.Connection;
import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.client.AddScheduledStatusMessage;
import com.kakapo.unity.message.client.ClearStatusMessage;
import com.kakapo.unity.message.client.ClientMessage;
import com.kakapo.unity.message.client.ListScheduledStatusesMessage;
import com.kakapo.unity.message.client.PeerMessage;
import com.kakapo.unity.message.client.RemoveExtensionMessage;
import com.kakapo.unity.message.client.RemoveScheduledStatusMessage;
import com.kakapo.unity.message.client.SetStatusMessage;
import com.kakapo.unity.message.server.ContactAction;
import com.kakapo.unity.message.server.ContactListMessage;
import com.kakapo.unity.message.server.ExtensionInUseMessage;
import com.kakapo.unity.message.server.InternalErrorMessage;
import com.kakapo.unity.message.server.OverrideMessage;
import com.kakapo.unity.message.server.ScheduledStatusListMessage;
import com.kakapo.unity.message.server.ShutDownMessage;
import com.kakapo.unity.message.server.SingleItemSet;
import com.kakapo.unity.message.server.StatusListMessage;
import com.kakapo.unity.util.Objects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MemoryServer
        implements Server {

    private static Logger logger;
    private int connections;
    private String redirect;
    private boolean shutdown;
    private final Map<String, Group> groups;
    private final Map<String, ConnectedServer> servers;
    private final Queue<ExtensionScheduledStatus> scheduledStatusUpdates;

    public MemoryServer() {

        this.groups = new HashMap<String, Group>();
        this.servers = new HashMap<String,ConnectedServer>();
        this.scheduledStatusUpdates = new PriorityQueue<ExtensionScheduledStatus>(500);
    }

    public void processScheduledStatuses() {
        Date now = new Date();

        Map<Group, StatusListMessage> messages = null;

        while (!this.scheduledStatusUpdates.isEmpty()) {
            ExtensionScheduledStatus head = (ExtensionScheduledStatus) this.scheduledStatusUpdates.peek();
            Date when = head.start ? head.scheduledStatus.getStart() : head.scheduledStatus.getEnd();
            if (when.after(now)) {
                break;
            }

            this.scheduledStatusUpdates.poll();

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Processing scheduled status " + head);
            }

            if (head.scheduledStatus.isValid()) {
                if (messages == null) {
                    messages = new HashMap<Group, StatusListMessage>();
                }

                Group group = (Group) this.groups.get(head.group);
                ConnectedClient extension = group.getExtension(head.extension);

                Status current = extension.getCurrentStatus();

                if (head.start) {
                    if (extension.getCurrentScheduledStatus() != null) {
                        throw new IllegalStateException("Attempt to set new status " + head.scheduledStatus + " but one already exists " + extension.getCurrentScheduledStatus() + "  for extension " + extension.getKey());
                    }
                    logger.fine("Setting current scheduled status for extension " + extension.getKey() + " to " + head.scheduledStatus);
                    extension.setCurrentScheduledStatus(head.scheduledStatus);
                } else {
                    if (head.scheduledStatus != extension.getCurrentScheduledStatus()) {
                        throw new IllegalStateException("Must end the same status instance");
                    }
                    extension.setCurrentScheduledStatus(null);
                    logger.fine("Removed current scheduled status for extension " + extension.getKey());

                    if (extension.removeScheduledStatus(head.scheduledStatus.getId()) == null) {
                        throw new IllegalStateException("Did not find scheduled status to remove");
                    }

                }

                if (extension.getCurrentStatus() != current) {
                    current = extension.getCurrentStatus();
                    addScheduledStatusMessageItem(messages, group, extension, current);
                }
            }

        }

        if (messages != null) {
            Set<Entry<Group, StatusListMessage>> groupMessages = messages.entrySet();
            for (Entry<Group, StatusListMessage> entry : groupMessages) {
                send(entry.getKey().getExtensions(), entry.getValue());
            }
        }
    }

    private void addScheduledStatusMessageItem(Map<Group, StatusListMessage> messages, Group group, ConnectedClient extension, Status status) {
        StatusListMessage message = (StatusListMessage) messages.get(group);
        if (message == null) {
            message = new StatusListMessage();
            messages.put(group, message);
        }
        message.addStatus(extension.getKey(), status == null ? null : status.getName(), status == null ? null : status.getStart());
    }

    @Override
    public void register(Connection client, boolean override) {
        try {
            logger.log(Level.INFO, "Client: {0} Override: {1}", new Object[]{client, override});



            if (this.shutdown) {
                logger.info("Client register during shutdown");
                client.receive(new ShutDownMessage(this.redirect));
                return;
            }

            Group group = (Group) this.groups.get(client.getGroup());
            if (group == null) {
                logger.log(Level.INFO, "Creating new group: {0}", client.getGroup());
                group = new Group(client.getGroup());
                Group existing = (Group) this.groups.put(client.getGroup(), group);
                assert (existing == null);
            }

            ConnectedClient extension = group.getExtension(client.getExtension());
            if (extension != null) {
                if (extension.getClient() != null) {
                    if (override) {
                        logger.log(Level.INFO, "Replacing client {0}", client);

                        extension.getClient().receive(new OverrideMessage());

                        extension.setClient(client);
                    } else {
                        logger.log(Level.INFO, "Extension already connected for {0}", client);
                        client.receive(new ExtensionInUseMessage());
                        return;
                    }
                } else {
                    extension.setClient(client);
                    this.connections += 1;
                }

            } else {
                extension = group.addClient(client.getExtension(), client);
                this.connections += 1;
            }

            sendContactList(extension, group);
            sendStatusList(extension, group);
            sendNewContact(extension, group);

            logger.log(Level.INFO, "Connections: {0}", this.connections);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Unexpected error during registration", t);

            client.receive(new InternalErrorMessage());
        }
    }

    private void sendStatusList(ConnectedClient extension, Group group) {
        Collection<ConnectedClient> extensions = group.getExtensions();

        StatusListMessage message = new StatusListMessage();
        for (ConnectedClient member : extensions) {
            if (member.getCurrentStatus() != null) {
                message.addStatus(member.getKey(), member.getCurrentStatus());
            }
        }
        send(extension, message);
    }

    private void sendNewContact(ConnectedClient extension, Group group) {
        Collection<ConnectedClient> extensions = group.getExtensions();
        ContactAction action = new ContactAction(ContactAction.Action.ADD, extension.getKey());
        ContactListMessage message = new ContactListMessage(action);

        Collection<ConnectedClient> excluded = new ExclusionSet<ConnectedClient>(extensions, extension);

        send(excluded, message);
    }

    private void sendContactList(ConnectedClient extension, Group group) {
        Collection<ConnectedClient> extensions = group.getExtensions();

        Collection<ContactAction> actions = new ArrayList<ContactAction>(extensions.size());
        for (ConnectedClient recipient : extensions) {
            Connection other = recipient.getClient();

            if ((other != null) && (recipient != extension)) {
                actions.add(new ContactAction(ContactAction.Action.ADD, other.getExtension()));
            }
        }
        send(extension, new ContactListMessage(actions));
    }

    @Override
    public void send(Connection sender, ClientMessage message) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Recieved message " + message + " from " + sender);
        }

        try {
            if ((message instanceof PeerMessage)) {
                PeerMessage pm = (PeerMessage) message;
                Group group = (Group) this.groups.get(sender.getGroup());
                Set<String> extensions = pm.getExtensions();

                Collection<ConnectedClient> recipients = new ArrayList<ConnectedClient>(extensions.size());
                for (String destination : extensions) {
                    ConnectedClient extension = group.getExtension(destination);
                    if (extension != null) {
                        recipients.add(extension);
                    } else {
                        logger.warning("Client not found with extn " + destination + " in group " + sender.getGroup());
                    }

                }

                send(recipients, message);
            } else {
                Group group = (Group) this.groups.get(sender.getGroup());
                ConnectedClient extension = group.getExtension(sender.getExtension());

                if ((message instanceof SetStatusMessage)) {
                    SetStatusMessage usm = (SetStatusMessage) message;
                    ConnectedClient subject = group.getExtension(usm.getExtension());
                    Status current = subject.getCurrentStatus();

                    if (usm.getStatus().getName().contains(" ")) {
                        throw new IllegalArgumentException("Statuses cannot contain spaces");
                    }

                    subject.setManualStatus(usm.getStatus());

                    logger.fine("Updated current status of extension " + usm.getExtension() + " from " + current + " to " + usm.getStatus());

                    StatusListMessage slm = new StatusListMessage();
                    slm.addStatus(usm.getExtension(), usm.getStatus());
                    send(group.getExtensions(), slm);
                } else if ((message instanceof ClearStatusMessage)) {
                    ClearStatusMessage csm = (ClearStatusMessage) message;
                    ConnectedClient subject = group.getExtension(csm.getExtension());
                    Status current = subject.getCurrentStatus();
                    subject.setManualStatus(null);

                    if (current != subject.getCurrentStatus()) {
                        current = subject.getCurrentStatus();
                        StatusListMessage slm = new StatusListMessage();
                        slm.addStatus(csm.getExtension(), current);
                        send(group.getExtensions(), slm);
                    }
                } else if ((message instanceof AddScheduledStatusMessage)) {
                    AddScheduledStatusMessage assm = (AddScheduledStatusMessage) message;
                    ConnectedClient subject = group.getExtension(assm.getExtension());

                    if (assm.getScheduledStatus().getStart().before(new Date())) {
                        throw new InvalidMessageException("Start date for status " + assm.getScheduledStatus() + " before current server time");
                    }

                    subject.addScheduledStatus(assm.getScheduledStatus());

                    this.scheduledStatusUpdates.add(new ExtensionScheduledStatus(assm.getExtension(), sender.getGroup(), assm.getScheduledStatus(), true));

                    this.scheduledStatusUpdates.add(new ExtensionScheduledStatus(assm.getExtension(), sender.getGroup(), assm.getScheduledStatus(), false));
                } else if ((message instanceof RemoveScheduledStatusMessage)) {
                    RemoveScheduledStatusMessage rssm = (RemoveScheduledStatusMessage) message;
                    ConnectedClient subject = group.getExtension(rssm.getExtension());
                    ScheduledStatus removed = subject.removeScheduledStatus(rssm.getId());

                    if (removed == subject.getCurrentScheduledStatus()) {
                        Status current = subject.getCurrentStatus();

                        subject.setCurrentScheduledStatus(null);

                        if (subject.getCurrentStatus() != current) {
                            StatusListMessage slm = new StatusListMessage();
                            slm.addStatus(rssm.getExtension(), subject.getCurrentStatus());
                            send(group.getExtensions(), slm);
                        }
                    }

                    if (removed != null) {
                        removed.invalidate();
                    }
                } else if ((message instanceof RemoveExtensionMessage)) {
                    RemoveExtensionMessage rem = (RemoveExtensionMessage) message;
                    for (ScheduledStatus scheduledStatus : extension.getScheduledStatuses()) {
                        scheduledStatus.invalidate();
                    }
                    group.removeExtension(rem.getExtension());
                } else if ((message instanceof ListScheduledStatusesMessage)) {
                    ListScheduledStatusesMessage lssm = (ListScheduledStatusesMessage) message;
                    ConnectedClient subject = group.getExtension(lssm.getExtension());

                    Collection<ScheduledStatus> scheduled = subject.getScheduledStatuses();
                    List<ScheduledStatus> filtered = new ArrayList<ScheduledStatus>(scheduled.size());
                    for (ScheduledStatus ss : scheduled) {
                        if ((ss.getEnd().after(lssm.getStart())) && (ss.getStart().before(lssm.getEnd()))) {
                            filtered.add(ss);
                        }
                    }
                    sender.receive(new ScheduledStatusListMessage(filtered));
                } else {
                    throw new IllegalArgumentException("Unknown client message: " + message);
                }
            }
        } catch (InvalidMessageException e) {
            logger.log(Level.WARNING, e.getMessage() + " from " + sender);
            sender.receive(new InternalErrorMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during send from " + sender, e);
            sender.receive(new InternalErrorMessage());
        }
    }

    protected void send(Collection<ConnectedClient> extensions, Message message) {
        for (ConnectedClient extension : extensions) {
            if (extension.getClient() != null) {
                extension.getClient().receive(message);
            }
        }
    }

    private void send(ConnectedClient recipient, Message message) {
        send(new SingleItemSet<ConnectedClient>(recipient), message);
    }

    @Override
    public void unregister(Connection client) {
        try {
            Group group = (Group) this.groups.get(client.getGroup());
            if (group == null) {
                logger.severe("Could not find group for client " + client);
                return;
            }

            ConnectedClient extension = group.getExtension(client.getExtension());

            if (extension != null) {
                if (extension.getClient() != null) {
                    extension.setClient(null);

                    this.connections -= 1;
                    assert (this.connections >= 0);

                    logger.info("Unregistering client " + client);
                    logger.info("Connections: " + this.connections);

                    ContactAction action = new ContactAction(ContactAction.Action.REMOVE, client.getExtension());
                    ContactListMessage message = new ContactListMessage(action);
                    send(group.getExtensions(), message);
                } else {
                    logger.severe("Unregister client that was not connected");
                }
            } else {
                logger.warning("Attempt to unregister unknown client " + client);
            }
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Unexpected error during unregistration", t);
        }
    }

    public void shutdown() {
        this.shutdown = true;

        Message message = new ShutDownMessage(this.redirect);

        Collection<ConnectedClient> clients = new ArrayList<ConnectedClient>(this.connections);
        for (Group group : this.groups.values()) {
            clients.addAll(group.getExtensions());
        }

        send(clients, message);
    }

    public String getRedirect() {
        return this.redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public int getConnections() {
        return this.connections;
    }

    static {
        logger = Logger.getLogger(MemoryServer.class.getName());
    }

    public ConnectedServer addServer(String key, Connection server)
  {
    ConnectedServer objServer = this.servers.get(key);
    if (objServer == null)
    {
      objServer = new ConnectedServer(key, server,true);
      this.servers.put(key, objServer);
    }
    else
    {
      objServer.setServer(server);
    }
    return objServer;
  }
    
    private static class ExtensionScheduledStatus
            implements Comparable<ExtensionScheduledStatus> {

        String extension;
        String group;
        ScheduledStatus scheduledStatus;
        boolean start;

        public ExtensionScheduledStatus(String extension, String group, ScheduledStatus scheduledStatus, boolean start) {
            this.extension = extension;
            this.group = group;
            this.scheduledStatus = scheduledStatus;
            this.start = start;
        }

        public int compareTo(ExtensionScheduledStatus o) {
            Date mine = this.start ? this.scheduledStatus.getStart() : this.scheduledStatus.getEnd();
            Date yours = o.start ? o.scheduledStatus.getStart() : o.scheduledStatus.getEnd();

            if (mine.equals(yours)) {
                return this.start ? 1 : -1;
            }

            return mine.compareTo(yours);
        }

        public String toString() {
            return Objects.toString(this);
        }
    }
}