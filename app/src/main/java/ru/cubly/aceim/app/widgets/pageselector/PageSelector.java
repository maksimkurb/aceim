package ru.cubly.aceim.app.widgets.pageselector;

import ru.cubly.aceim.app.page.Page;

public interface PageSelector {

	public PageAdapter getPageAdapter();
	
	public void setPageAdapter(PageAdapter adapter);
	
	public void setSelectedPage(Page page);
}
