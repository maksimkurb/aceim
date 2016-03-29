package ru.cubly.aceim.client.dataentity.listeners;

import ru.cubly.aceim.client.dataentity.Account;


public interface IHasAccountList extends IHasEntity {
	public void onAccountAdded(Account account);
	public void onAccountModified(Account account);
	public void onAccountRemoved(Account account);
}
