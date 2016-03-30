package ru.cubly.aceim.app.page.contactlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.androidquery.AQuery;

import java.util.List;

import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.BuddyGroup;
import ru.cubly.aceim.api.service.ApiConstants;
import ru.cubly.aceim.app.OldMainActivity;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.page.contactlist.ContactListUpdater.ContactListModelGroup;
import ru.cubly.aceim.app.themeable.dataentity.ContactListItemThemeResource;
import ru.cubly.aceim.app.utils.ViewUtils;

public abstract class ContactListAdapter extends BaseExpandableListAdapter {

	protected final List<ContactListModelGroup> mGroupList;
	protected final ProtocolResources mResources;
	
	protected AQuery mAq = null;

	public ContactListAdapter(List<ContactListModelGroup> groups, ProtocolResources resources) {
		this.mGroupList = groups;
		this.mResources = resources;
	}

	@Override
	public ContactListModelGroup getGroup(int groupPosition) {
		return mGroupList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroupList.size();
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		return mGroupList.get(groupPosition).getBuddyList().size();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	@Override
	public Buddy getChild(int groupPosition, int childPosition) {
		return mGroupList.get(groupPosition).getBuddyList().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return getChild(groupPosition, childPosition).getId();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, final ViewGroup parent) {
		final ContactListModelGroup g = getGroup(groupPosition);

		View item;
		if (convertView == null) {
			Context context = parent.getContext();
			LayoutInflater inflater = LayoutInflater.from(context);
			item = (View) inflater.inflate(R.layout.group_header_item, null);
		} else {
			item = convertView;
		}
		
		if (mAq == null) {
			mAq = new AQuery(item);
		} else {
			mAq.recycle(item);
		}
		
		mAq.id(android.R.id.text1).text(g.getName());
		mAq.id(android.R.id.text2).text(getOnlinesOfflinesText(parent.getContext(), g));

		if (g.isCollapsed()) {
			((ExpandableListView) parent).collapseGroup(groupPosition);
		} else {
			((ExpandableListView) parent).expandGroup(groupPosition);
		}

		item.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				g.setCollapsed(!g.isCollapsed());
				if (g.isCollapsed()) {
					((ExpandableListView) parent).collapseGroup(groupPosition);
				} else {
					((ExpandableListView) parent).expandGroup(groupPosition);
				}
			}
		});
		
		if (!g.isPredefined()) {
			item.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					((OldMainActivity)parent.getContext()).onBuddyGroupContextMenuRequest(g, mResources);
					return true;
				}
			});
		}
		
		
		return item;
	}
	
	private String getOnlinesOfflinesText(Context context, BuddyGroup parent) {
		int onlines = 0;
		int offlines = 0;
		for (Buddy b : parent.getBuddyList()) {
			if (b.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1) > -1) {
				onlines++;
			} else {
				offlines++;
			}			
		}
		
		StringBuilder contentText = new StringBuilder();
		if (onlines > 0) {
			contentText.append(onlines);
			contentText.append(" ");
			contentText.append(context.getString(R.string.online));
		}			
		if (onlines > 0 && offlines > 0) {
			contentText.append(", ");
		}
		if (offlines > 0) {
			contentText.append(offlines);
			contentText.append(" ");
			contentText.append(context.getString(R.string.offline));
		}
		
		return contentText.toString();
	}
	
	protected View constructChildViewFromThemeResource(int groupPosition, int childPosition, boolean isLastChild, View convertView, final ViewGroup parent, ContactListItemThemeResource ctr) {
		Buddy buddy = getChild(groupPosition, childPosition);
		
		View view = convertView;
		
		if (convertView == null) {
			view = ctr.getView();
		}		
		
		if (view.getTag() == null || view.getTag() != buddy) {
			view.setTag(buddy);
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					OldMainActivity activity = (OldMainActivity) parent.getContext();
					Buddy buddy = (Buddy) v.getTag();
					activity.onChatRequest(buddy);
				}
			});
			
			view.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					OldMainActivity activity = (OldMainActivity) parent.getContext();
					Buddy buddy = (Buddy) v.getTag();
					activity.onBuddyContextMenuRequest(buddy, mResources);
					return true;
				}
			});
		} 
		
		ViewUtils.fillBuddyPlaceholder(parent.getContext(), buddy, view, mResources, ctr, childPosition, groupPosition, (AbsListView) parent);
		//mAq.recycle(view);
		//ViewUtils.fillBuddyPlaceholder(parent.getContext(), buddy, mAq, mResources, ctr);
		
		return view;
	}
}
