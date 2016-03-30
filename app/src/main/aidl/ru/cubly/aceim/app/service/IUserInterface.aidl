package ru.cubly.aceim.app.service;

import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.api.dataentity.OnlineInfo;
import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.api.dataentity.ItemAction;
import ru.cubly.aceim.api.dataentity.Message;
import ru.cubly.aceim.api.dataentity.PersonalInfo;
import ru.cubly.aceim.api.dataentity.MessageAckState;
import ru.cubly.aceim.api.dataentity.ConnectionState;
import ru.cubly.aceim.api.dataentity.FileProgress;
import ru.cubly.aceim.api.dataentity.InputFormFeature;

interface IUserInterface {
	void onProtocolUpdated(in ProtocolResources resources, in ItemAction action);
	void onConnectionStateChanged(byte serviceId, in ConnectionState connState, int extraParameter);
	void onAccountStateChanged(in OnlineInfo info);
	void onContactListUpdated(in ru.cubly.aceim.app.dataentity.Account account);
	void onBuddyStateChanged(in List<Buddy> buddies);
	void onAccountUpdated(in ru.cubly.aceim.app.dataentity.Account account, in ItemAction action);
	
	void onMessage(in ru.cubly.aceim.api.dataentity.Message message);
	void onMessageAck(byte serviceId, long messageId, String senderUid, in MessageAckState ackState);
	
	void onAccountIcon(byte serviceId);
	void onBuddyIcon(byte serviceId, String buddyProtocolUid);
	
	void onFileProgress(in FileProgress progress);
	void onSearchResult(byte serviceId, in List<PersonalInfo> infoList);
	void onPersonalInfo(in PersonalInfo info);
	void showFeatureInputForm(byte serviceId, String uid, in InputFormFeature feature);
	
	void terminate();
}