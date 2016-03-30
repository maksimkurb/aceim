package ru.cubly.aceim.api.service;

import ru.cubly.aceim.api.dataentity.ConnectionState;
import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.BuddyGroup;
import ru.cubly.aceim.api.dataentity.OnlineInfo;
import ru.cubly.aceim.api.dataentity.PersonalInfo;
import ru.cubly.aceim.api.dataentity.ItemAction;
import ru.cubly.aceim.api.dataentity.Message;
import ru.cubly.aceim.api.dataentity.MessageAckState;
import ru.cubly.aceim.api.dataentity.ProtocolOption;
import ru.cubly.aceim.api.dataentity.ProtocolServiceFeature;
import ru.cubly.aceim.api.service.ICoreProtocolCallback;

interface IProtocolService {

	void registerCallback(ICoreProtocolCallback callback);
	void addAccount(byte serviceId, String protocolUid);
	void removeAccount(byte serviceId);
	void shutdown();
	void logToFile(boolean enable);
	
	void requestFullInfo(byte serviceId, String uid, boolean shortInfo);
	void buddyAction(in ItemAction action, in Buddy buddy);
	void buddyGroupAction(in ItemAction action, in BuddyGroup group);
	void setFeature(String featureId, in OnlineInfo info);
	void disconnect(byte serviceId);
	void connect(in OnlineInfo info);
	long sendMessage(in ru.cubly.aceim.api.dataentity.Message message);
	void requestIcon(byte serviceId, String ownerUid);
	void messageResponse(in ru.cubly.aceim.api.dataentity.Message message, boolean accept);
	void cancelFileFransfer(byte serviceId, long messageId);
	void sendTypingNotification(byte serviceId, String ownerUid);
	void joinChatRoom(byte serviceId, String chatId, boolean loadOccupantsIcons);
	void leaveChatRoom(byte serviceId, String chatId);
	void uploadAccountPhoto(byte serviceId, String filePath);
	void removeAccountPhoto(byte serviceId);
	
	ProtocolServiceFeature[] getProtocolFeatures();
	ProtocolOption[] getProtocolOptions();
	String getProtocolName();
}
