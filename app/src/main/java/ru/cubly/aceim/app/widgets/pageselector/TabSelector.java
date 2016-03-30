package ru.cubly.aceim.app.widgets.pageselector;

import android.content.Context;
import android.util.AttributeSet;

import ru.cubly.aceim.app.page.Page;
import ru.cubly.aceim.app.widgets.HorizontalListView;

public class TabSelector extends HorizontalListView implements PageSelector {
	
	public TabSelector(Context context) {
		super(context);
	}

	public TabSelector(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public TabSelector(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public PageAdapter getPageAdapter() {
		return (PageAdapter) getAdapter();
	}

	@Override
	public void setPageAdapter(PageAdapter adapter) {
		setAdapter(adapter);
	}

	@Override
	public void setSelectedPage(Page page) {
		setSelected(page);
	}
}
