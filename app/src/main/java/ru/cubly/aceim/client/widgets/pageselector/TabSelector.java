package ru.cubly.aceim.client.widgets.pageselector;

import ru.cubly.aceim.client.page.Page;
import ru.cubly.aceim.client.widgets.HorizontalListView;
import android.content.Context;
import android.util.AttributeSet;

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
