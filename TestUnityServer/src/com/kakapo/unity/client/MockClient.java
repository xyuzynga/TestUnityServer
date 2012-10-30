package com.kakapo.unity.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.kakapo.unity.message.Message;
import com.kakapo.unity.message.client.ClientMessage;
import com.kakapo.unity.message.server.ContactAction;
import com.kakapo.unity.message.server.ContactListMessage;
import com.kakapo.unity.message.server.ExtensionStatus;
import com.kakapo.unity.message.server.ScheduledStatusListMessage;
import com.kakapo.unity.message.server.StatusListMessage;
import com.kakapo.unity.server.ScheduledStatus;
import com.kakapo.unity.server.Server;

public class MockClient implements Client
{
	final Server _server;
	private Set<String> _extensions = new HashSet<String>();
	private Map<String, ScheduledStatus> scheduled = new HashMap<String, ScheduledStatus>();
	private Map<String, String> statuses = new HashMap<String, String>();
	private String _group;
	private String _extension;
	private static final Logger _logger = Logger.getLogger(MockClient.class.getName());
	private final LinkedList<Message> _messages = new LinkedList<Message>();

	public MockClient(Server server, String group, String extension)
	{
		this._server = server;
		this._group = group;
		this._extension = extension;
	}

	public Message messageOfType(Class<? extends Message> clazz)
	{
		for (Message message : this._messages)
		{
			if (clazz.isAssignableFrom(message.getClass()))
			{
				return message;
			}
		}
		return null;
	}

	public void register(boolean override)
	{
		this._server.register(this, override);
	}

	public void unregister()
	{
		synchronized (getClass())
		{
			this._server.unregister(this);
		}
	}

	public synchronized void receive(Message message)
	{
		_logger.info("Client " + this + " Received message: " + message);

		if ((message instanceof ContactListMessage))
		{
			Collection<ContactAction> actions = ((ContactListMessage) message).getActions();
			for (ContactAction action : actions)
			{
				if (action.getAction().equals(ContactAction.Action.ADD))
				{
					this._extensions.add(action.getExtension());
				}
				else
				{
					this._extensions.remove(action.getExtension());
				}
			}
		}
		else if ((message instanceof StatusListMessage))
		{
			StatusListMessage slm = (StatusListMessage) message;
			for (ExtensionStatus es : slm.getStatuses())
			{
				this.statuses.put(es.getExtension(), es.getStatus());
			}
		}
		else if ((message instanceof ScheduledStatusListMessage))
		{
			ScheduledStatusListMessage sslm = (ScheduledStatusListMessage) message;
			for (ScheduledStatus es : sslm.getStatuses())
			{
				this.scheduled.put(es.getId(), es);
			}

		}

		this._messages.add(message);

		if (this._messages.size() > 10)
		{
			this._messages.poll();
		}
	}

	public void send(ClientMessage message)
	{
		this._server.send(this, message);
	}

	public Set<String> getExtensions()
	{
		return this._extensions;
	}

	public String getStatus(String extension)
	{
		return this.statuses.get(extension);
	}

	public Map<String, ScheduledStatus> getScheduled()
	{
		return this.scheduled;
	}

	public Server getServer()
	{
		return this._server;
	}

	public String getExtension()
	{
		return this._extension;
	}

	public String getGroup()
	{
		return this._group;
	}

	public LinkedList<Message> getMessages()
	{
		return this._messages;
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> T getMessageOfType(Class<T> type)
	{
		for (Message message : this._messages)
		{
			if (type.isAssignableFrom(message.getClass()))
			{
				return (T) message;
			}
		}
		return null;
	}

	public void close()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public String toString()
	{
		return getClass().getSimpleName() + " Group: " + this._group + " Extn: " + this._extension;
	}
}