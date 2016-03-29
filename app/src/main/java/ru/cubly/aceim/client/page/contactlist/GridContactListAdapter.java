package ru.cubly.aceim.client.page.contactlist;

import java.util.List;

import ru.cubly.aceim.client.MainActivity;
import ru.cubly.aceim.client.dataentity.ProtocolResources;
import ru.cubly.aceim.client.themeable.dataentity.ContactListItemThemeResource;
import ru.cubly.aceim.client.page.contactlist.ContactListUpdater.ContactListModelGroup;
import android.view.View;
import android.view.ViewGroup;

public class GridContactListAdapter extends ContactListAdapter {

	public GridContactListAdapter(List<ContactListModelGroup> groups, ProtocolResources resources) {
		super(groups, resources);
	}
	
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		MainActivity activity = (MainActivity) parent.getContext();
		ContactListItemThemeResource ctr = activity.getThemesManager().getViewResources().getGridItemLayout();
		
		return constructChildViewFromThemeResource(groupPosition, childPosition, isLastChild, convertView, parent, ctr);
	}

	
}
