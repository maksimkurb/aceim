package ru.cubly.aceim.app.page.contactlist;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.ConnectionState;
import ru.cubly.aceim.api.dataentity.OnlineInfo;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.utils.ViewUtils;
import ru.cubly.aceim.app.widgets.bottombar.ContactListBottomBar;

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
		View view = inflater.inflate(R.layout.page_contact_list_double, group, false);
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
