package ru.cubly.aceim.app.dataentity.listeners;

import ru.cubly.aceim.api.dataentity.Message;
import ru.cubly.aceim.api.dataentity.MessageAckState;

public interface IHasMessages {
	
	public void onMessageReceived(Message message);
	public void onMessageAckReceived(long messageId, MessageAckState ack);
	public boolean hasMessagesOfBuddy(byte serviceId, String buddyProtocolUid);
}
