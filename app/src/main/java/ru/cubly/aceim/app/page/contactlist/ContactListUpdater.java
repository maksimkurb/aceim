package ru.cubly.aceim.app.page.contactlist;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.BuddyGroup;
import ru.cubly.aceim.api.dataentity.MultiChatRoom;
import ru.cubly.aceim.api.service.ApiConstants;
import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.app.Constants;
import ru.cubly.aceim.app.MainActivity;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.app.dataentity.AccountOptionKeys;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.utils.LinqRules.BuddyLinqRule;
import ru.cubly.aceim.app.utils.linq.KindaLinq;
import ru.cubly.aceim.app.utils.linq.KindaLinqRule;
import android.content.SharedPreferences;
import android.content.res.Resources;

final class ContactListUpdater {

	private final byte mServiceId;
	private final ContactListAdapter mAdapter;

	private final ContactListModelGroup unreadGroup;
	private final ContactListModelGroup offlineGroup;
	private final ContactListModelGroup notInListGroup;
	private final ContactListModelGroup onlineGroup;
	private final ContactListModelGroup chatsGroup;
	private final ContactListModelGroup noGroup;

	private final List<ContactListModelGroup> mContactListGroups = Collections.synchronizedList(new ArrayList<ContactListModelGroup>());

	private boolean showGroups = false;
	private boolean showOffline = true;
	
	private volatile boolean canStartRedraw = true;

	ContactListUpdater(byte serviceId, Resources resources, Class<? extends ContactListAdapter> adapterClass, ProtocolResources protocolResources) {
		this.mAdapter = initAdapter(adapterClass, protocolResources, mContactListGroups);
		this.mServiceId = serviceId;
		
		unreadGroup = initGroup(Constants.VIEWGROUP_UNREAD, resources.getString(R.string.unread_group), true);
		offlineGroup = initGroup(Constants.VIEWGROUP_OFFLINE, resources.getString(R.string.offline_group), true);
		notInListGroup = initGroup(Constants.VIEWGROUP_NOT_IN_LIST, resources.getString(R.string.not_in_list_group), true);
		onlineGroup = initGroup(Constants.VIEWGROUP_ONLINE, resources.getString(R.string.online_group), true);
		chatsGroup = initGroup(Constants.VIEWGROUP_CHATS, resources.getString(R.string.chats_group), true);
		noGroup = initGroup(Constants.VIEWGROUP_NOGROUP, resources.getString(R.string.no_group), true);
	}

	private static final ContactListAdapter initAdapter(Class<? extends ContactListAdapter> adapterClass, ProtocolResources protocolResources, List<ContactListModelGroup> contactListGroups) {
		try {
			Constructor<? extends ContactListAdapter> constructor = adapterClass.getConstructor(List.class, ProtocolResources.class);
			return constructor.newInstance(contactListGroups, protocolResources);
		} catch (Exception e) {
			Logger.log(e);
		}
		return null;
	}

	private final ContactListModelGroup initGroup(String groupId, String name, boolean predefined) {
		ContactListModelGroup g = new ContactListModelGroup(groupId, null, mServiceId, predefined);
		g.setName(name);
		return g;
	}

	private final ContactListModelGroup initGroup(BuddyGroup origin) {
		ContactListModelGroup g = initGroup(origin.getId(), origin.getName(), false);
		g.setCollapsed(origin.isCollapsed());
		return g;
	}

	public void onContactListUpdated(Account account, MainActivity activity) {
		SharedPreferences p = activity.getSharedPreferences(account.getAccountId(), 0);

		showGroups = p.getBoolean(AccountOptionKeys.SHOW_GROUPS.name(), Boolean.parseBoolean(activity.getString(R.string.default_show_groups)));
		showOffline = p.getBoolean(AccountOptionKeys.SHOW_OFFLINE.name(), Boolean.parseBoolean(activity.getString(R.string.default_show_offline)));

		synchronized (mContactListGroups) {
			mContactListGroups.clear();

			unreadGroup.getBuddyList().clear();
			offlineGroup.getBuddyList().clear();
			notInListGroup.getBuddyList().clear();
			onlineGroup.getBuddyList().clear();
			chatsGroup.getBuddyList().clear();
			noGroup.getBuddyList().clear();
			
			List<Buddy> noGroupBuddies = account.getNoGroupBuddies();
			
			synchronized (noGroupBuddies) {
				for (Buddy buddy : noGroupBuddies) {

					BuddyGroup targetGroup = getTargetGroup(buddy, null);
					if (targetGroup != null) {
						targetGroup.getBuddyList().add(buddy);
					}
				}
			}
			
			List<BuddyGroup> groups = account.getBuddyGroupList();
			
			synchronized (groups) {
				for (BuddyGroup origin : groups) {

					ContactListModelGroup viewGroup;
					if (showGroups && !origin.getId().equals(ApiConstants.NO_GROUP_ID)) {
						viewGroup = initGroup(origin);
					} else {
						viewGroup = null;
					}

					for (Buddy buddy : origin.getBuddyList()) {

						BuddyGroup targetGroup = getTargetGroup(buddy, viewGroup);
						if (targetGroup != null) {
							targetGroup.getBuddyList().add(buddy);
						}
					}

					if (viewGroup != null) {
						mContactListGroups.add(viewGroup);
					}
				}
			}
			
			if (!showGroups) {
				mContactListGroups.add(onlineGroup);
			}

			showPredefinedGroups();

			if (!showGroups && showOffline) {
				mContactListGroups.add(offlineGroup);
			}

			for (BuddyGroup group : mContactListGroups) {
				Collections.sort(group.getBuddyList());
			}

			mAdapter.notifyDataSetChanged();
		}
	}

	private void showPredefinedGroups() {
		if (unreadGroup.getBuddyList().size() > 0) {
			mContactListGroups.add(0, unreadGroup);
		}

		if (showOffline && mContactListGroups.size() > 0) {
			int offlineGroupId = 0;
			
			for (;offlineGroupId < mContactListGroups.size(); offlineGroupId++) {
				ContactListModelGroup g = mContactListGroups.get(offlineGroupId);
				if (g == offlineGroup){
					break;
				}
			}
			
			if (chatsGroup.getBuddyList().size() > 0) {
				mContactListGroups.add(offlineGroupId, chatsGroup);
			}

			if (noGroup.getBuddyList().size() > 0) {
				mContactListGroups.add(offlineGroupId, noGroup);
			}
		} else {
			if (chatsGroup.getBuddyList().size() > 0) {
				mContactListGroups.add(chatsGroup);
			}

			if (noGroup.getBuddyList().size() > 0) {
				mContactListGroups.add(noGroup);
			}
		}
		
		if (notInListGroup.getBuddyList().size() > 0) {
			mContactListGroups.add(notInListGroup);
		}
	}

	private BuddyGroup getTargetGroup(Buddy buddy, BuddyGroup idGroup) {
		if (buddy.getUnread() > 0) {
			return unreadGroup;
		} else if (buddy instanceof MultiChatRoom) {
			return chatsGroup;
		} else if (buddy.getGroupId() != null && buddy.getGroupId().equals(ApiConstants.NOT_IN_LIST_GROUP_ID)) {
			return notInListGroup;
		} else {
			if (showGroups) {
				if (buddy.getGroupId() == null || buddy.getGroupId().equals(ApiConstants.NO_GROUP_ID)) {
					return noGroup;
				} else if (showOffline || buddy.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1) > -1) {
					return idGroup;
				} else {
					return null;
				}
			} else {
				if (buddy.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1) < 0){
					return offlineGroup;
				} else {
					return onlineGroup;
				}
			}
		} 	
	}

	public void onBuddyStateChanged(List<Buddy> buddies) {
		if (buddies == null || buddies.size() < 1) return;
		
		synchronized (mContactListGroups) {
			for (final Buddy buddy : buddies) {
				// We may need the actual protocol group for a view with "show groups",
				// so create a spare group link for it
				BuddyGroup idGroup = null;
				for (BuddyGroup viewGroup : mContactListGroups) {
					Buddy old = KindaLinq.from(viewGroup.getBuddyList()).where(new BuddyLinqRule(buddy)).first();

					if (old != null) {
						
						viewGroup.getBuddyList().remove(old);

						if (showGroups && buddy.getGroupId().equals(viewGroup.getId())) {
							idGroup = viewGroup;
						}
					}
				}

				if (showGroups && idGroup == null) {
					idGroup = KindaLinq.from(mContactListGroups).where(new KindaLinqRule<ContactListModelGroup>() {

						@Override
						public boolean match(ContactListModelGroup t) {
							return t.getId().equals(buddy.getGroupId());
						}
					}).first();
				}

				BuddyGroup target = getTargetGroup(buddy, idGroup);
				if (target != null) {
					target.getBuddyList().add(buddy);
					Collections.sort(target.getBuddyList());
				}
			}			
		}

		if (canStartRedraw) {
			canStartRedraw = false;
			
			mContactListGroups.remove(unreadGroup);
			mContactListGroups.remove(notInListGroup);
			mContactListGroups.remove(chatsGroup);
			mContactListGroups.remove(noGroup);

			showPredefinedGroups();

			mAdapter.notifyDataSetChanged();
			
			canStartRedraw = true;
		}
	}

	public ContactListAdapter getAdapter() {
		return mAdapter;
	}

	public void onBuddyIcon(byte serviceId, String protocolUid) {
		//mAdapter.notifyDataSetChanged();
	}
	
	public static class ContactListModelGroup extends BuddyGroup {
		
		private final boolean predefined;

		private ContactListModelGroup(String id, String accountId, Byte serviceId, boolean predefined) {
			super(id, accountId, serviceId);
			this.predefined = predefined;
		}

		/**
		 * @return the predefined
		 */
		public boolean isPredefined() {
			return predefined;
		}		
	}
}
