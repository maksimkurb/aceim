package ru.cubly.aceim.app.dataentity.listeners;

import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.ConnectionState;
import ru.cubly.aceim.api.dataentity.OnlineInfo;
import ru.cubly.aceim.app.dataentity.Account;

public interface IHasAccount extends IHasBuddy {

	public Account getAccount();
	public void onOnlineInfoChanged(OnlineInfo info);
	public Buddy getBuddyWithParameters(byte serviceId, String protocolUid);
	public void onConnectionStateChanged(ConnectionState connState, int extraParameter);
	public void onContactListUpdated(Account account);
	public void onAccountIcon(byte serviceId);
}
