package ru.cubly.aceim.app.page;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.lang.reflect.Constructor;
import java.util.List;

import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.EntityWithID;
import ru.cubly.aceim.api.dataentity.InputFormFeature;
import ru.cubly.aceim.api.dataentity.OnlineInfo;
import ru.cubly.aceim.api.dataentity.PersonalInfo;
import ru.cubly.aceim.api.service.ApiConstants;
import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.api.utils.Logger.LoggerLevel;
import ru.cubly.aceim.app.OldMainActivity;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.dataentity.listeners.IHasAccount;
import ru.cubly.aceim.app.dataentity.listeners.IHasBuddy;
import ru.cubly.aceim.app.page.about.About;
import ru.cubly.aceim.app.page.accounteditor.AccountEditor;
import ru.cubly.aceim.app.page.accounts.Accounts;
import ru.cubly.aceim.app.page.chat.Chat;
import ru.cubly.aceim.app.page.contactlist.ContactList;
import ru.cubly.aceim.app.page.history.History;
import ru.cubly.aceim.app.page.inputform.InputFormFeaturePage;
import ru.cubly.aceim.app.page.other.GlobalMessage;
import ru.cubly.aceim.app.page.other.MasterPassword;
import ru.cubly.aceim.app.page.other.SearchResult;
import ru.cubly.aceim.app.page.other.Splash;
import ru.cubly.aceim.app.page.personalinfo.PersonalInfoPage;
import ru.cubly.aceim.app.page.transfers.FileTransfers;
import ru.cubly.aceim.app.page.utils.Utilities;
import ru.cubly.aceim.app.screen.Screen;
import ru.cubly.aceim.app.utils.LinqRules.AccountByProtocolUidLinqRule;
import ru.cubly.aceim.app.utils.LinqRules.ChatLinqRule;
import ru.cubly.aceim.app.utils.LinqRules.PageIdLinqRule;
import ru.cubly.aceim.app.utils.LinqRules.PersonalInfoLinqRule;
import ru.cubly.aceim.app.utils.ViewUtils;
import ru.cubly.aceim.app.utils.linq.KindaLinq;
import ru.cubly.aceim.app.utils.linq.KindaLinqRule;
import ru.cubly.aceim.app.widgets.bottombar.BottomBarButtonInfo;

public abstract class Page extends Fragment {
	private OldMainActivity mActivity;
		
	private View cachedView = null;
	private Bundle cachedBundle = null;
	
	public abstract Drawable getIcon(Context context);	
	public abstract String getTitle(Context context);
	protected abstract View createView(LayoutInflater inflater, ViewGroup group, Bundle saved);
	
	protected final OnClickListener mRemoveMeClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			removeMe();
		}
	};
	
	protected Page() {
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved){
		if (cachedView == null) {
			cachedView = createView(inflater, group, cachedBundle);
			cachedBundle = null;
		} else {
			if (cachedView.getParent() != null) {
				((ViewGroup)cachedView.getParent()).removeView(cachedView);
			}
		}
		
		onSetMeSelected();
		
		return cachedView;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		return true;
	}

	public String getPageId() {
		if (this instanceof Chat || this instanceof History){
			return getPageIdForEntityWithId(this.getClass(), ((IHasBuddy)this).getBuddy());
		} else if (this instanceof ContactList || this instanceof FileTransfers){
			return getPageIdForEntityWithId(this.getClass(), ((IHasAccount)this).getAccount());
		}
		return getClass().getSimpleName();
	}
	
	protected void removeMe() {
		onLeaveMe();
		this.cachedView = null;
		remove(getMainActivity().getScreen(), this);
	}
	
	public static void addPasswordPage(Screen screen) {
		screen.addPage(new MasterPassword(), true);		
	}
	
	public static void getContactListPage(OldMainActivity activity, Account account) {
		Screen screen = activity.getScreen();
		Page page = getPage(screen, new PageIdLinqRule(getPageIdForEntityWithId(ContactList.class, account)));
		if (page == null) {
			page = ContactList.createContactListPage(activity, account);
			if (page != null) screen.addPage(page, true);
		} else {
			screen.setSelectedPage(page.getPageId());
		}
	}
	public static void addSplash(Screen screen) {
		screen.addPage(new Splash(), true);
	}
	
	public static void addAccountManagerPage(Screen screen, List<Account> accounts) {
		screen.addPage(new Accounts(accounts), true);
	}
	
	public interface PageChangedListener {		
		public void onPageChanged(String pageId);
	}

	public static void removeSplash(Screen screen) {
		Page splash = screen.findPage(Splash.class.getSimpleName());
		if (splash != null) {
			screen.removePage(splash);	
		}	
	}
	
	public static void remove(Screen screen, Page page) {
		screen.removePage(page);
		if (page instanceof IHasAccount) {
			getContactListPage(screen.getActivity(), ((IHasAccount)page).getAccount());
		}
	}
	
	public static void addMessagePage(Screen screen, String title, Drawable icon, int textId, List<BottomBarButtonInfo> buttons) {
		screen.addPage(new GlobalMessage(title, icon, textId, buttons), true);
	}
	
	public static void addAccountEditorPage(Screen screen, Account account) {
		screen.addPage(new AccountEditor(account), true);
	}
	
	public static void addSearchResultPage(Screen screen, Account account, List<PersonalInfo> infoList, boolean setAsCurent) {
		screen.addPage(new SearchResult(account, infoList), true);
	}
	
	public static void getChatPage(Screen screen, Buddy buddy, Account account) {
		Page page = getPage(screen, new ChatLinqRule(buddy));
		if (page == null) {
			screen.addPage(new Chat(buddy, account, screen.getActivity().getProtocolResourcesForAccount(account)), true);
		} else {
			screen.setSelectedPage(page.getPageId());
		}		
	}
	
	public static void getHistoryPage(Screen screen, Buddy buddy) {
		Page page = getPage(screen, new PageIdLinqRule(getPageIdForEntityWithId(History.class, buddy)));
		if (page == null) {
			screen.addPage(new History(buddy), true);
		} else {
			screen.setSelectedPage(page.getPageId());
		}		
	}	
	
	public static void getInputFormPage(OldMainActivity oldMainActivity, InputFormFeature iff, OnlineInfo info, ProtocolResources resources) {
		Screen screen = oldMainActivity.getScreen();
		Page page = getPage(screen, new PageIdLinqRule(getPageIdForInputFormPage(iff, info)));
		
		if (page == null) {
			screen.addPage(new InputFormFeaturePage(iff, info, resources), true);
		} else {
			screen.setSelectedPage(page.getPageId());
		}
	}
	
	public static void getUtilsPage(Screen screen) {
		Page page = getPage(screen, new PageIdLinqRule(Utilities.class.getSimpleName()));
		if (page == null) {
			screen.addPage(new Utilities(), true);
		} else {
			screen.setSelectedPage(page.getPageId());
		}
	}
	
	public static void getAboutPage(Screen screen) {
		
		Page page = getPage(screen, new PageIdLinqRule(About.class.getSimpleName()));
		if (page == null) {
			screen.addPage(new About(), true);
		} else {
			screen.setSelectedPage(page.getPageId());
		}
	}
	
	public static void getFileTransfersPage(Screen screen, Account account) {
		Page page = getPage(screen, new PageIdLinqRule(getPageIdForEntityWithId(FileTransfers.class, account)));
		if (page == null) {
			screen.addPage(new FileTransfers(account), false);
		} else {
			screen.setSelectedPage(page.getPageId());
		}
	}
	
	public static void getPersonalInfoPage(Screen screen, PersonalInfo info) {
		Page page = getPage(screen, new PersonalInfoLinqRule(info));
		if (page == null) {
			screen.addPage(new PersonalInfoPage(info), true);
		} else {
			screen.setSelectedPage(page.getPageId());
		}
	}
	
	private static Page getPage(Screen screen, KindaLinqRule<Page> rule) {
		List<Page> pages = screen.findPagesByRule(rule);
		if (pages.size() < 1) {
			return null;
		} else {
			return pages.get(0);
		}
	}
	
	public static <T> String getPageIdForEntityWithId(Class<T> classOfT, EntityWithID entity) {
		return classOfT.getSimpleName() + " " + entity.getEntityId();
	}
	
	public static String getPageIdForInputFormPage(InputFormFeature iff, OnlineInfo info) {
		return iff.getFeatureId() + " " + info.getProtocolUid() + " " + info.getServiceId();
	}
	
	@SuppressWarnings("unchecked")
	public static void recoverPageById(List<Account> accounts, String pageId, Bundle savedState, OldMainActivity oldMainActivity) {
		Logger.log("Recovering page " + pageId, LoggerLevel.VERBOSE);
		
		String[] pageIdParts = pageId.split(Character.toString(ApiConstants.GENERAL_DIVIDER));
		
		Class<? extends Page> pageClass = ViewUtils.getPageClassByPageId(pageIdParts[0]);
		
		if (pageClass == null) {
			return;
		}
		
		Constructor<? extends Page> c = (Constructor<? extends Page>) pageClass.getConstructors()[0];
		
		Class<?>[] params = c.getParameterTypes();
		Object[] args = new Object[params.length];
		
		Page page;
		
		for (int i = 0; i<params.length; i++) {
			Class<?> param = params[i];
			
			//TODO refactor?
			Account a = KindaLinq.from(accounts).where(new AccountByProtocolUidLinqRule(pageIdParts[1])).first();
			if (param == Account.class) {
				if (a == null) return;
				
				args[i] = a;
			} else if (param == Buddy.class) {
				if (a == null) return;
				
				Buddy b = a.getBuddyByProtocolUid(pageIdParts[3]);				
				if (b == null) return;
				
				args[i] = b;
			} else if (param == ProtocolResources.class) {
				if (a == null) return;
				
				ProtocolResources res = oldMainActivity.getProtocolResourcesForAccount(a);
				if (res == null) return;
				
				args[i] = res;
			}
		}
		
		try {
			page = c.newInstance(args);
		} catch (Exception e) {
			Logger.log(e);
			return;
		}
		
		page.cachedBundle = savedState;
		
		oldMainActivity.getScreen().addPage(page, false);
	}
	
	public void setMainActivity(OldMainActivity activity){
		this.mActivity = activity;
	}
	
	protected OldMainActivity getMainActivity() {
		if (mActivity == null) {
			try {
				while ((mActivity = (OldMainActivity) getActivity()) == null) {
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				Logger.log(e);
			}
		}
		
		return mActivity;
	}
	
	public boolean onKeyDown(int i, KeyEvent event) {
		
		if (i == KeyEvent.KEYCODE_BACK) {
			removeMe();
			return true;
		}
		
		return false;
	}
	
	public boolean hasMenu() {
		return false;
	}
	
	public Bundle getPageDataForStorage() {
		return null;
	}
	
	public void onSetMeSelected() {}
	public void onLeaveMe() {}
}
