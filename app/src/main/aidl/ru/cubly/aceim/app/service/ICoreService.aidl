package ru.cubly.aceim.app.service;

import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.BuddyGroup;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.api.dataentity.FileMessage;
import ru.cubly.aceim.api.dataentity.FileProgress;
import ru.cubly.aceim.api.dataentity.OnlineInfo;
import ru.cubly.aceim.api.dataentity.ServiceMessage;
import ru.cubly.aceim.api.dataentity.PersonalInfo;
import ru.cubly.aceim.api.dataentity.Message;
import ru.cubly.aceim.api.dataentity.TextMessage;
import ru.cubly.aceim.app.service.IUserInterface;
import ru.cubly.aceim.api.dataentity.MultiChatRoom;
import ru.cubly.aceim.api.dataentity.ProtocolOption;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.dataentity.AccountOptionKeys;
import ru.cubly.aceim.app.dataentity.GlobalOptionKeys;

interface ICoreService {

	void saveInstanceState(in Bundle bundle);
	Bundle restoreInstanceState();

	void registerCallback(IUserInterface callback);
	
	List<ProtocolOption> getProtocolOptions(String protocolServiceClassName, byte serviceId);
	
	ru.cubly.aceim.app.dataentity.Account createAccount(String protocolServiceClassName, in List<ProtocolOption> options);
	void deleteAccount(in ru.cubly.aceim.app.dataentity.Account account);
	void editAccount(in ru.cubly.aceim.app.dataentity.Account account, in List<ProtocolOption> options, String protocolServicePackageName);
	
	long sendMessage(in ru.cubly.aceim.api.dataentity.Message message);
	void sendLocation(in Buddy buddy);
	
	List<ru.cubly.aceim.app.dataentity.Account> getAccounts(boolean disabledToo);
	List<ProtocolResources> getAllProtocolResources(boolean getProtocolInfo);
	
	Buddy getBuddy(byte serviceId, String buddyProtocolUid);
	List<Buddy> getBuddies(byte serviceId, in List<String> buddyProtocolUid);
	ru.cubly.aceim.app.dataentity.Account getAccount(byte serviceId);
	
	void connect(byte serviceId);
	void disconnect(byte serviceId);
	void connectAll();
	void disconnectAll();
	
	void notifyUnread(in ru.cubly.aceim.api.dataentity.Message message, in Buddy buddy);
	void resetUnread(in Buddy buddy);
	//void setUIVisible(boolean visible);
	
	void addBuddy(in Buddy buddy);
	void removeBuddy(in Buddy buddy);
	void renameBuddy(in Buddy buddy);
	void moveBuddy(in Buddy buddy);
	
	void addGroup(in BuddyGroup group);
	void removeGroup(in BuddyGroup group);
	void renameGroup(in BuddyGroup group);
	void setGroupCollapsed(byte serviceId, String groupId, boolean collapsed);
	
	void requestBuddyShortInfo(byte serviceId, String uid);
	void requestBuddyFullInfo(byte serviceId, String uid);
	
	void respondMessage(in ru.cubly.aceim.api.dataentity.Message msg, boolean accept);
	void cancelFileTransfer(byte serviceId, long messageId);
	void uploadAccountPhoto(byte serviceId, String filename);
	void removeAccountPhoto(byte serviceId);
	
	List<ru.cubly.aceim.api.dataentity.Message> getLastMessages(in Buddy buddy);
	List<ru.cubly.aceim.api.dataentity.Message> getMessages(in Buddy buddy, int startFrom, int maxMessagesToRead);
	boolean deleteMessagesHistory(in Buddy buddy);
	
	void setFeature(String featureId, in OnlineInfo info);
	void sendTyping(byte serviceId, String buddyUid);
	void editBuddy(in Buddy buddy);
	
	void leaveChat(byte serviceId, String chatId);
	void joinChat(byte serviceId, String chatId);
	
	void importAccounts(String password, in FileProgress progress); 
	void exportAccounts(String password, in FileProgress progress);
	void exit(boolean terminate);
}