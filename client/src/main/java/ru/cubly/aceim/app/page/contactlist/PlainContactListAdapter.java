package ru.cubly.aceim.app.page.contactlist;

import java.util.List;

import ru.cubly.aceim.app.MainActivity;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.themeable.dataentity.ContactListItemThemeResource;
import ru.cubly.aceim.app.page.contactlist.ContactListUpdater.ContactListModelGroup;
import android.view.View;
import android.view.ViewGroup;

public class PlainContactListAdapter extends ContactListAdapter {

	public PlainContactListAdapter(List<ContactListModelGroup> groups, ProtocolResources resources) {
		super(groups, resources);
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent) {
		final MainActivity activity = (MainActivity) parent.getContext();
		ContactListItemThemeResource ctr = activity.getThemesManager().getViewResources().getListItemLayout();
		
		return constructChildViewFromThemeResource(groupPosition, childPosition, isLastChild, convertView, parent, ctr);
	}

}
