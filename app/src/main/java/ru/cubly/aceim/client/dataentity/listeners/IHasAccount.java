package ru.cubly.aceim.client.dataentity.listeners;

import aceim.api.dataentity.Buddy;
import aceim.api.dataentity.ConnectionState;
import aceim.api.dataentity.OnlineInfo;
import ru.cubly.aceim.client.dataentity.Account;

public interface IHasAccount extends IHasBuddy {

	public Account getAccount();
	public void onOnlineInfoChanged(OnlineInfo info);
	public Buddy getBuddyWithParameters(byte serviceId, String protocolUid);
	public void onConnectionStateChanged(ConnectionState connState, int extraParameter);
	public void onContactListUpdated(Account account);
	public void onAccountIcon(byte serviceId);
}
