package ru.cubly.aceim.app.screen;

import java.util.List;

import ru.cubly.aceim.api.dataentity.ConnectionState;
import ru.cubly.aceim.app.Constants;
import ru.cubly.aceim.app.MainActivity;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.app.dataentity.GlobalOptionKeys;
import ru.cubly.aceim.app.screen.pano.PanoScreen;
import ru.cubly.aceim.app.screen.simple.SimpleScreen;
import ru.cubly.aceim.app.screen.tablet.TabletScreen;
import ru.cubly.aceim.app.utils.ViewUtils;
import ru.cubly.aceim.app.utils.linq.KindaLinqRule;
import ru.cubly.aceim.app.page.Page;
import ru.cubly.aceim.app.page.contactlist.ContactList;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

@SuppressLint("InlinedApi")
public abstract class Screen extends FrameLayout {
	
	private final boolean requireDirtyMenuButtonHack;
	
	public Screen(MainActivity activity) {
		super(activity);
		
		ViewUtils.setWallpaperMode(getActivity(), this);
		requireDirtyMenuButtonHack = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && (activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_XLARGE) > 0;
	}

	public MainActivity getActivity() {
		return (MainActivity) getContext();
	}

	public static final Screen getScreen(MainActivity activity) {
		//TODO rename key_view_type
		String screenName = activity.getSharedPreferences(Constants.SHARED_PREFERENCES_GLOBAL, 0)
				.getString(GlobalOptionKeys.SCREEN_TYPE.name(), activity.getString(R.string.default_screen_type));
		
		if (screenName == null || screenName.equals("Auto")) {
			if (ViewUtils.isTablet(activity)) {
				screenName = TabletScreen.class.getName();
			} else {
				screenName = SimpleScreen.class.getName();
			}
		}
		
		//TODO support 3rd party screens?
		if (screenName.equals(PanoScreen.class.getName())) {
			return new PanoScreen(activity);
		} else if (screenName.equals(TabletScreen.class.getName())) {
			return new TabletScreen(activity);
		} else {
			return new SimpleScreen(activity);
		}		
	}
	
	protected final OnLongClickListener mMenuButtonLongClickListener = new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			List<Page> pages = findPagesByRule(new KindaLinqRule<Page>() {

				@Override
				public boolean match(Page t) {
					return t instanceof ContactList;
				}
			});
			
			for (Page page : pages) {
				Account a = ((ContactList)page).getAccount();
				if (a.getConnectionState() == ConnectionState.DISCONNECTED) {
					try {
						getActivity().getCoreService().connect(a.getServiceId());
					} catch (RemoteException e) {
						getActivity().onRemoteException(e);
					}
				}
			}
			
			return true;
		}
	}; 
	
	protected final OnClickListener mMenuButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (requireDirtyMenuButtonHack) {
				Configuration config = getContext().getResources().getConfiguration();
				config.screenLayout &= ~Configuration.SCREENLAYOUT_SIZE_XLARGE;
				config.screenLayout |= Configuration.SCREENLAYOUT_SIZE_LARGE;
			}
			
			getActivity().openOptionsMenu();
		}
	};
	
	protected OnClickListener mTabClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setSelectedPage(((Page)v.getTag()).getPageId());
		}
	};
	
	public boolean onCurrentPageKeyDown(int i, KeyEvent event) {
		return getSelectedPage().onKeyDown(i, event);
	}
	
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		getSelectedPage().onCreateOptionsMenu(menu, menuInflater);
	}

	public void onPrepareOptionsMenu(Menu menu) {
		if (requireDirtyMenuButtonHack) {
			Configuration config = getContext().getResources().getConfiguration();
			config.screenLayout &= ~Configuration.SCREENLAYOUT_SIZE_LARGE;
			config.screenLayout |= Configuration.SCREENLAYOUT_SIZE_XLARGE;
		}
		
		getSelectedPage().onPrepareOptionsMenu(menu);
	}

	public void onOptionsItemSelected(MenuItem item) {
		getSelectedPage().onOptionsItemSelected(item);
	}

	public abstract void addPage(Page page, boolean setAsCurrent);
	public abstract Page findPage(String pageId);
	//public abstract void removePage(String pageId);
	public abstract void removePage(Page page);
	public abstract void setSelectedPage(String pageId);
	public abstract Page getSelectedPage();
	public abstract Page getSelectedContactList();	
	public abstract void onPageChanged(String pageId);	
	public abstract List<Page> findPagesByRule(KindaLinqRule<Page> rule);
	public abstract List<Page> getAllPages();

	public abstract void updateTabWidget(Page p);

	public abstract void storeScreenSpecificData(Bundle bundle);
	public abstract void recoverScreenSpecificData(Bundle bundle);
}
