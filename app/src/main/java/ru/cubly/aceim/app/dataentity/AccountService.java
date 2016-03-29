package ru.cubly.aceim.app.dataentity;

import java.util.concurrent.ScheduledFuture;

import ru.cubly.aceim.app.service.ProtocolService;

public class AccountService {

	private final Account account;
	private final ProtocolService protocolService;
	
	private byte connectionAttempts;
	
	private ScheduledFuture<?> connectionTimeoutAction;
	
	public AccountService(Account account, ProtocolService protocolService) {
		this.account = account;
		this.protocolService = protocolService;
	}

	/**
	 * @return the account
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * @return the protocolService
	 */
	public ProtocolService getProtocolService() {
		return protocolService;
	}

	/**
	 * @param connectionTimeoutAction the connectionTimeoutAction to set
	 */
	public void setConnectionTimeoutAction(ScheduledFuture<?> connectionTimeoutAction) {
		resetConnectionTimeout();
		this.connectionTimeoutAction = connectionTimeoutAction;
	}

	public void resetConnectionTimeout() {
		if (connectionTimeoutAction != null) {
			connectionTimeoutAction.cancel(false);
		}
		
		connectionTimeoutAction = null;
	}
	
	public boolean isUnderConnectionMonitoring() {
		return connectionTimeoutAction != null;
	}

	/**
	 * @return the connectionAttempts
	 */
	public byte getConnectionAttempts() {
		return connectionAttempts;
	}

	/**
	 * @param connectionAttempts the connectionAttempts to set
	 */
	public void setConnectionAttempts(byte connectionAttempts) {
		this.connectionAttempts = connectionAttempts;
	}
}
