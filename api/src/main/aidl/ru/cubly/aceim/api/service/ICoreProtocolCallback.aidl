package ru.cubly.aceim.api.service;

import ru.cubly.aceim.api.dataentity.ConnectionState;
import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.BuddyGroup;
import ru.cubly.aceim.api.dataentity.BaseMessage;
import ru.cubly.aceim.api.dataentity.FileMessage;
import ru.cubly.aceim.api.dataentity.OnlineInfo;
import ru.cubly.aceim.api.dataentity.PersonalInfo;
import ru.cubly.aceim.api.dataentity.ItemAction;
import ru.cubly.aceim.api.dataentity.MessageAckState;
import ru.cubly.aceim.api.dataentity.FileProgress;
import ru.cubly.aceim.api.dataentity.InputFormFeature;

interface ICoreProtocolCallback {

	void connectionStateChanged(byte serviceId, in ConnectionState connState, int extraParameter);
	void iconBitmap(byte serviceId, String ownerUid, in byte[] data, String hash);
	void buddyListUpdated(byte serviceId, in List<BuddyGroup> buddyList);
	void message(in BaseMessage message);
	void buddyStateChanged(in List<OnlineInfo> infos);
	void notification(byte serviceId, String message);
	void accountStateChanged(in OnlineInfo info);
	void personalInfo(in PersonalInfo info, boolean isShortInfo);
	void searchResult(byte serviceId, in List<PersonalInfo> infoList);
	void groupAction(in ItemAction action, in BuddyGroup newGroup);
	void buddyAction(in ItemAction action, in Buddy newBuddy);
	void fileProgress(in FileProgress progress);
	void messageAck(byte serviceId, String ownerUid, long messageId, in MessageAckState state);
	void typingNotification(byte serviceId, String ownerUid);
	String requestPreference(byte serviceId, String preferenceName);
	void accountActivity(byte serviceId, String text);	
	void multiChatParticipants(byte serviceId, String chatUid, in List<BuddyGroup> participantList);
	void showFeatureInputForm(byte serviceId, String uid, in InputFormFeature feature);
}