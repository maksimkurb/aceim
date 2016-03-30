package ru.cubly.aceim.app.page.contactlist;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.cubly.aceim.app.OldMainActivity;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.page.contactlist.ContactListUpdater.ContactListModelGroup;
import ru.cubly.aceim.app.themeable.dataentity.ContactListItemThemeResource;

public class GridContactListAdapter extends ContactListAdapter {

	public GridContactListAdapter(List<ContactListModelGroup> groups, ProtocolResources resources) {
		super(groups, resources);
	}
	
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		OldMainActivity activity = (OldMainActivity) parent.getContext();
		ContactListItemThemeResource ctr = activity.getThemesManager().getViewResources().getGridItemLayout();
		
		return constructChildViewFromThemeResource(groupPosition, childPosition, isLastChild, convertView, parent, ctr);
	}

	
}
