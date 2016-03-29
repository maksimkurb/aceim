package ru.cubly.aceim.client.screen.pano;

import static ru.cubly.aceim.client.utils.linq.KindaLinq.from;

import java.util.ArrayList;
import java.util.List;

import ru.cubly.aceim.client.MainActivity;
import aceim.app.R;
import ru.cubly.aceim.client.screen.Screen;
import ru.cubly.aceim.client.utils.linq.KindaLinq;
import ru.cubly.aceim.client.utils.linq.KindaLinqRule;
import ru.cubly.aceim.client.page.Page;
import ru.cubly.aceim.client.page.contactlist.ContactList;
import ru.cubly.aceim.client.page.other.Splash;
import ru.cubly.aceim.client.widgets.HorizontalListView;
import ru.cubly.aceim.client.widgets.bottombar.BottomBarButton;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;

public class PanoScreen extends Screen {

	private final HorizontalListView mList;
	private final BottomBarButton mMenuButton;
	private final PanoPageAdapter mPageAdapter;
	
	private Page mSelectedPage;
	
	private final List<Page> mPages = new ArrayList<Page>();
	
	private final OnHierarchyChangeListener mTabChangedListener = new OnHierarchyChangeListener() {
		
		@Override
		public void onChildViewRemoved(View parent, View child) {}
		
		@Override
		public void onChildViewAdded(View parent, View child) {
			setSelectedPage((Page) child.getTag());
		}
	};
	
	public PanoScreen(MainActivity activity) {
		super(activity);
		LayoutInflater.from(activity).inflate(R.layout.screen_pano, this);
		
		mList = (HorizontalListView) findViewById(R.id.horizontal_list);
		mList.setOnHierarchyChangeListener(mTabChangedListener);
		
		mMenuButton = (BottomBarButton) findViewById(R.id.menu_button);
		mMenuButton.setOnClickListener(mMenuButtonClickListener);
		mMenuButton.setOnLongClickListener(mMenuButtonLongClickListener);
		
		mPageAdapter = new PanoPageAdapter(activity, mTabClickListener, mPages);
		mPageAdapter.setNotifyOnChange(true);
		
		int width = activity.getResources().getDisplayMetrics().widthPixels;
		int height = activity.getResources().getDisplayMetrics().heightPixels;
		
		int pageWidth = (int) (0.8 * (width < height ? width : height));
		
		mPageAdapter.setPageWidth(pageWidth);
		mList.setAdapter(mPageAdapter);
	}

	@Override
	public void addPage(Page page, boolean setAsCurrent) {
		page.setMainActivity(getActivity());
		
		mPageAdapter.add(page);
		
		if (setAsCurrent) {
			setSelectedPage(page);
		}
	}

	@SuppressLint("NewApi")
	private void setMenuButtonAvailability() {
		mMenuButton.setVisibility(
				Build.VERSION.SDK_INT <= 10 || (Build.VERSION.SDK_INT >= 14 && ViewConfiguration.get(getContext()).hasPermanentMenuKey()) ? 
						View.GONE : 
							View.VISIBLE);
	}

	@Override
	public Page findPage(final String pageId) {
		return from(mPages).where(new KindaLinqRule<Page>() {
			
			@Override
			public boolean match(Page t) {
				return t.getPageId().equals(pageId);
			}
		}).first();
	}

	@Override
	public void onPageChanged(String pageId) {}

	@Override
	public void setSelectedPage(String pageId) {
		Page page = findPage(pageId);
		
		setSelectedPage(page);
		
		if (!(page instanceof Splash) && page.hasMenu()) {
			setMenuButtonAvailability();
		} else {
			mMenuButton.setVisibility(View.GONE);
		}
	}

	@Override
	public List<Page> findPagesByRule(KindaLinqRule<Page> rule) {
		return KindaLinq.from(mPages).where(rule).all();
	}

	@Override
	public Page getSelectedPage() {
		return mSelectedPage;
	}

	@Override
	public Page getSelectedContactList() {
		return (mSelectedPage instanceof ContactList) ? mSelectedPage : null;
	}

	@Override
	public void removePage(Page page) {
		mPageAdapter.remove(page);
		
		if (mPages.size() > 0) {
			setSelectedPage(mPages.get(0).getPageId());
		} else {
			getActivity().exitApplication();
		}
	}

	@Override
	public void updateTabWidget(Page p) {
		View tab = mList.findViewWithTag(p);
		
		if (tab == null) {
			return;
		}
		
		View tabWidget = tab.findViewById(R.id.indicator);
		
		mPageAdapter.fillWithImageAndTitle(tabWidget, p);
	}

	@Override
	public List<Page> getAllPages() {
		return mPages;
	}

	@Override
	public void storeScreenSpecificData(Bundle bundle) {}

	@Override
	public void recoverScreenSpecificData(Bundle bundle) {}

	private void setSelectedPage(Page page) {
		this.mSelectedPage = page;
		
		mList.setSelected(page);
	}	
}
