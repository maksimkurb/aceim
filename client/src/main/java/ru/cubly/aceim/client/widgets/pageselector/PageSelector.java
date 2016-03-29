package ru.cubly.aceim.client.widgets.pageselector;

import ru.cubly.aceim.client.page.Page;

public interface PageSelector {

	public PageAdapter getPageAdapter();
	
	public void setPageAdapter(PageAdapter adapter);
	
	public void setSelectedPage(Page page);
}
