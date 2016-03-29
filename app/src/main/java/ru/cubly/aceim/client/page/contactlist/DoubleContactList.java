package ru.cubly.aceim.client.page.contactlist;

import aceim.api.dataentity.Buddy;
import aceim.api.dataentity.ConnectionState;
import aceim.api.dataentity.OnlineInfo;
import aceim.app.R;
import ru.cubly.aceim.client.dataentity.Account;
import ru.cubly.aceim.client.dataentity.ProtocolResources;
import ru.cubly.aceim.client.utils.ViewUtils;
import ru.cubly.aceim.client.widgets.bottombar.ContactListBottomBar;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class DoubleContactList extends ContactList {

	private ExpandableListView mListView;
	private ContactListBottomBar mBottomBar;
	
	public DoubleContactList(Account mAccount, ProtocolResources mProtocolResources, Resources applicationResources) {
		super(mAccount, mProtocolResources, applicationResources);
	}

	@Override
	public void onConnectionStateChanged(ConnectionState connState, int extraParameter) {
		super.onConnectionStateChanged(connState, extraParameter);
		if (mBottomBar != null) {
			mBottomBar.onConnectionStateChanged(connState, extraParameter);	
		}	
	}

	@Override
	protected View onCreateContactListView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
		View view = inflater.inflate(R.layout.double_contact_list, group, false);
		mListView = (ExpandableListView) view.findViewById(R.id.grid);
		mListView.setAdapter(getAdapter());
		mBottomBar = (ContactListBottomBar) view.findViewById(R.id.bottom_bar);
		
		return view;
	}
	
	@Override
	public void onAccountIcon(byte serviceId) {
		ViewUtils.fillAccountPlaceholder(getMainActivity(), mAccount, mBottomBar, mProtocolResources);
	}
	
	@Override
	public void onBuddyIcon(byte serviceId, String protocolUid){
		Buddy b = mAccount.getBuddyByProtocolUid(protocolUid);
		
		if (b == null){
			return;
		}
		
		View v = mListView.findViewWithTag(b);
		
		if (v != null) {
			ViewUtils.fillIcon(R.id.image_icon, v, b.getFilename(), getMainActivity());	
		} 
	}
	
	@Override
	public void onOnlineInfoChanged(OnlineInfo info) {
		super.onOnlineInfoChanged(info);
		
		ViewUtils.fillAccountPlaceholder(getMainActivity(), mAccount, mBottomBar, mProtocolResources);
	}


	@Override
	protected Class<? extends ContactListAdapter> getContactListAdapterClassName() {
		return DoubleListAdapter.class;
	}
}
