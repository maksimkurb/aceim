package ru.cubly.aceim.client.utils.history;

import java.util.List;

import aceim.api.dataentity.Buddy;
import aceim.api.dataentity.Message;
import ru.cubly.aceim.client.dataentity.Account;

public interface HistorySaver {

	public abstract void saveMessage(Buddy buddy, Message message);

	public abstract List<Message> getMessages(Buddy buddy);

	public abstract List<Message> getMessages(Buddy buddy, int startFrom, int maxMessagesToRead);
	
	public abstract boolean deleteHistory(Buddy buddy);

	public abstract void removeAccount(Account account);
}