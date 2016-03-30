package ru.cubly.aceim.app.page.personalinfo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.androidquery.AQuery;

import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.ListFeature;
import ru.cubly.aceim.api.dataentity.PersonalInfo;
import ru.cubly.aceim.api.dataentity.ProtocolServiceFeature;
import ru.cubly.aceim.api.dataentity.ToggleFeature;
import ru.cubly.aceim.api.service.ApiConstants;
import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.app.AceImException;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.page.Page;
import ru.cubly.aceim.app.service.ServiceUtils;
import ru.cubly.aceim.app.utils.DialogUtils;
import ru.cubly.aceim.app.utils.ViewUtils;
import ru.cubly.aceim.app.widgets.bottombar.BottomBarButton;

public class PersonalInfoPage extends Page {
	
	private final PersonalInfo mInfo;
	
	private BottomBarButton mAddBtn;
	private BottomBarButton mMoveBtn;
	private BottomBarButton mRenameBtn;
	private BottomBarButton mDeleteBtn;
	private BottomBarButton mJoinBtn;
	private BottomBarButton mLeaveBtn;
	private BottomBarButton mCopyAllBtn;

	public PersonalInfoPage(PersonalInfo info) {
		this.mInfo = info;
	}

	@Override
	public Drawable getIcon(Context context) {
		return context.getResources().getDrawable(android.R.drawable.ic_menu_info_details);
	}

	@Override
	public String getTitle(Context context) {
		return context.getString(R.string.personal_info_X, mInfo.getProtocolUid());
	}
	
	@Override
	protected View createView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
		View view = inflater.inflate(R.layout.page_personal_info, null);
		
		mCopyAllBtn = (BottomBarButton) view.findViewById(R.id.copy);
		mAddBtn = (BottomBarButton) view.findViewById(R.id.add);
		mMoveBtn = (BottomBarButton) view.findViewById(R.id.move);
		mRenameBtn = (BottomBarButton) view.findViewById(R.id.rename);
		mDeleteBtn = (BottomBarButton) view.findViewById(R.id.remove);
		mJoinBtn = (BottomBarButton) view.findViewById(R.id.join);
		mLeaveBtn = (BottomBarButton) view.findViewById(R.id.leave);
		
		AQuery aq = new AQuery(view);
		
		Buddy buddy;
		try {
			buddy = getMainActivity().getCoreService().getBuddy(mInfo.getServiceId(), mInfo.getProtocolUid());
		} catch (RemoteException e) {
			getMainActivity().onRemoteException(e);
			return view;
		}
		
		final String name = mInfo.getProperties().containsKey(PersonalInfo.INFO_NICK) ? mInfo.getProperties().getString(PersonalInfo.INFO_NICK) : mInfo.getProtocolUid();
		
		if (buddy != null) {
			ViewUtils.fillIcon(R.id.icon, aq, buddy.getFilename(), getMainActivity());
		}
		
		aq.id(R.id.name).text(name);
		aq.id(R.id.protocolUid).text(mInfo.getProtocolUid());
		
		mAddBtn.setVisibility(mInfo.isMultichat() || buddy != null ? View.GONE : View.VISIBLE);
		mDeleteBtn.setVisibility(buddy == null ? View.GONE : View.VISIBLE);
		mRenameBtn.setVisibility(buddy == null ? View.GONE : View.VISIBLE);
		mMoveBtn.setVisibility(mInfo.isMultichat() || buddy == null ? View.GONE : View.VISIBLE);
		
		mJoinBtn.setVisibility(mInfo.isMultichat() && (buddy == null || buddy.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1) < 0) ? View.VISIBLE : View.GONE);
		mLeaveBtn.setVisibility(mInfo.isMultichat() && buddy != null && (buddy.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1) > -1) ? View.VISIBLE : View.GONE);
		
		final Account a;
		try {
			a = getMainActivity().getCoreService().getAccount(mInfo.getServiceId());
		} catch (RemoteException e1) {
			getMainActivity().onRemoteException(e1);
			return view;
		}
		
		mAddBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!mInfo.isMultichat()) {
					Buddy buddy = new Buddy(mInfo.getProtocolUid(), a.getProtocolUid(), a.getProtocolName(), mInfo.getServiceId());
					buddy.setGroupId(ApiConstants.NOT_IN_LIST_GROUP_ID);
					buddy.setName(name.toString());
					
					DialogUtils.showAddBuddyDialog(buddy, a, getMainActivity());
				}
			}
		});
		
		mDeleteBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogUtils.showConfirmRemoveBuddyDialog(a.getBuddyByProtocolUid(mInfo.getProtocolUid()), getMainActivity());
			}
		});
		
		mRenameBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogUtils.showBuddyRenameDialog(a.getBuddyByProtocolUid(mInfo.getProtocolUid()), getMainActivity());
			}
		});
		
		mMoveBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DialogUtils.showBuddyMoveDialog(a.getBuddyByProtocolUid(mInfo.getProtocolUid()), a, getMainActivity());
			}
		});
		
		mJoinBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					getMainActivity().getCoreService().joinChat(mInfo.getServiceId(), mInfo.getProtocolUid());
				} catch (RemoteException e) {
					getMainActivity().onRemoteException(e);
				}
			}
		});
		
		mLeaveBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					getMainActivity().getCoreService().leaveChat(mInfo.getServiceId(), mInfo.getProtocolUid());
				} catch (RemoteException e) {
					getMainActivity().onRemoteException(e);
				}
			}
		});
		
		mCopyAllBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String text = personalInfo2Text();
				ServiceUtils.toClipboard(getMainActivity(), text, getTitle(getMainActivity()));
				
				ViewUtils.showInformationToast(getMainActivity(), android.R.drawable.ic_menu_agenda, R.string.copied_to_clipboard, null);
			}
		});
		
		/*MultiChatRoom page_chat = new MultiChatRoom(mInfo.getProtocolUid(), a.getProtocolUid(), a.getProtocolName(), mInfo.getServiceId());
		page_chat.setName(mInfo.getProperties().getString(PersonalInfo.INFO_NICK));*/
		
		LinearLayout container = (LinearLayout) view.findViewById(R.id.container);
		
		for (String key : mInfo.getProperties().keySet()) {
			if (key.equals(PersonalInfo.INFO_NICK)) continue; 
			
			View item = inflater.inflate(R.layout.item_personal_info, null);
			
			AQuery aqi = new AQuery(item);
			
			ImageView iicon = (ImageView) item.findViewById(R.id.icon);
			//TODO fix
			iicon.getLayoutParams().width = 0;
			
			aqi.id(R.id.key).text(key);
			aqi.id(R.id.value).text(mInfo.getProperties().getString(key));
			
			container.addView(item);
		}
		
		if (buddy != null) {
			ProtocolResources res = getMainActivity().getProtocolResourcesForAccount(a);
			try {
				Resources protocolResources = res.getNativeResourcesForProtocol(getMainActivity().getPackageManager());
				
				for (String key : buddy.getOnlineInfo().getFeatures().keySet()) {
					ProtocolServiceFeature f = res.getFeature(key);
					if (f == null) {
						continue;
					}
					
					View item = inflater.inflate(R.layout.item_personal_info, null);
					
					AQuery aqi = new AQuery(item);
					
					aqi.id(R.id.key).text(f.getFeatureName());
					
					if (f instanceof ListFeature) {
						ListFeature lf = (ListFeature) f;
						byte v = buddy.getOnlineInfo().getFeatures().getByte(key, (byte) -1);
						if (v > -1) {
							aqi.id(R.id.value).text(protocolResources.getString(lf.getNames()[v]));							
						}
					} else if (f instanceof ToggleFeature) {
						aqi.id(R.id.value).text(Boolean.toString(((ToggleFeature)f).getValue()));
					}
				}
			} catch (AceImException e) {
				Logger.log(e);
			}
		}
		
		return view;
	}
	
	private String personalInfo2Text() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(mInfo.getProtocolUid());
		sb.append("\n\n");
		
		for (String property : mInfo.getProperties().keySet()) {
			sb.append(property);
			sb.append(": ");
			sb.append(mInfo.getProperties().get(property));
			sb.append("\n");
		}
		
		return sb.toString();
	}

	/**
	 * @return the mInfo
	 */
	public PersonalInfo getInfo() {
		return mInfo;
	}
}
