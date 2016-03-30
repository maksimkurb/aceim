package ru.cubly.aceim.app.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.api.utils.Logger.LoggerLevel;
import ru.cubly.aceim.app.OldMainActivity;
import ru.cubly.aceim.app.page.NoPage;
import ru.cubly.aceim.app.page.Page;

public final class PageManager {
	
	private NoPage mNoPageView = null;
	private final int mPageHolderId;
	private Page mSelectedPage;
	private final OldMainActivity mActivity;
	private final List<Page> mPages = new ArrayList<Page>();

	public PageManager(int pageHolderId, OldMainActivity activity) {
		this.mPageHolderId = pageHolderId;
		this.mActivity = activity;
		
		init();
	}
	
	private void init() {
		mActivity.getSupportFragmentManager()
				.beginTransaction()
				.disallowAddToBackStack()
				.attach(getNoPageView())
				.replace(mPageHolderId, getNoPageView())
				.commit();
	}

	public void onPageChanged(Page newSelectedPage) {
		Logger.log("Page changed to " + newSelectedPage, LoggerLevel.VERBOSE);
		if (newSelectedPage == null) {
			return;
		}
		
		FragmentManager fm = mActivity.getSupportFragmentManager();
		Page newPage = (Page) fm.findFragmentByTag(newSelectedPage.getPageId());
		
		FragmentTransaction ft = fm.beginTransaction().disallowAddToBackStack();
		
		if (mSelectedPage != null) {
			mSelectedPage.onLeaveMe();
			ft.detach(mSelectedPage);
		}
		
		if (newPage == null) {
			newPage = newSelectedPage;
			ft.attach(newPage);				
		}
		
		mSelectedPage = newPage;
		ft.replace(mPageHolderId, mSelectedPage);
		
		ft.commitAllowingStateLoss();
	}
	
	public void onPageRemoved(Page page){
		if (page == null) {
			return;
		}
		
		FragmentManager fm = mActivity.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction().disallowAddToBackStack();
		
		ft.remove(page);
		
		if (mPages.size() < 1) {
			mSelectedPage = null;
			ft.replace(mPageHolderId, getNoPageView());
		}
		ft.commit();
	}

	private Fragment getNoPageView() {
		if (mNoPageView == null) {
			mNoPageView = new NoPage();
		}
		return mNoPageView;
	}

	/**
	 * @return the mSelectedPage
	 */
	public Page getSelectedPage() {
		return mSelectedPage;
	}

	/**
	 * @param mSelectedPage the mSelectedPage to set
	 */
	public void setSelectedPage(Page mSelectedPage) {
		this.mSelectedPage = mSelectedPage;
	}

	/**
	 * @return the mPages
	 */
	public List<Page> getPages() {
		return mPages;
	}
}
