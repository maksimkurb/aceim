package ru.cubly.aceim.app.page.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

import ru.cubly.aceim.api.dataentity.FileProgress;
import ru.cubly.aceim.app.OldMainActivity;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.dataentity.ActivityResult;
import ru.cubly.aceim.app.dataentity.listeners.IHasFilePicker;
import ru.cubly.aceim.app.dataentity.listeners.IHasFileProgress;
import ru.cubly.aceim.app.page.Page;
import ru.cubly.aceim.app.widgets.bottombar.BottomBarButton;

public class Utilities extends Page implements  IHasFilePicker, IHasFileProgress{
	
	private static Util[] UTILS;
	
	private UtilitiesAdapter mAdapter;
	
	public Utilities() {}

	@Override
	public Drawable getIcon(Context context) {
		return context.getResources().getDrawable(android.R.drawable.ic_menu_myplaces);
	}

	@Override
	public String getTitle(Context context) {
		return context.getString(R.string.utils);
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
		View view = inflater.inflate(R.layout.page_utils, null);
		
		UTILS = new Util[]{new AccountImporter(getMainActivity())};
		
		BottomBarButton closeBtn = (BottomBarButton) view.findViewById(R.id.cancel);
		closeBtn.setOnClickListener(mRemoveMeClickListener);
		
		mAdapter = new UtilitiesAdapter(getMainActivity(), Arrays.asList(UTILS));
		
		ListView list = (ListView) view.findViewById(R.id.list);
		list.setAdapter(mAdapter);
		
		return view;
	}
	
	interface Util {
		
		View getView(LayoutInflater inflater);
	}

	private class UtilitiesAdapter extends ArrayAdapter<Util> {

		public UtilitiesAdapter(Context context, List<Util> objects) {
			super(context, android.R.id.title, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			
			Util u = getItem(position);
			
			if (convertView != null && convertView.getTag() == u) {
				view = convertView;
			} else {
				view = constructFromUtil(u);
			}
			
			view.setTag(u);
			
			return view;
		}

		private View constructFromUtil(Util u) {
			return u.getView(LayoutInflater.from(getContext()));
		}
	}

	@Override
	public void onFileProgress(FileProgress progress) {
		if (progress.getServiceId() > -1) {
			return;
		}
		
		for (Util u : UTILS) {
			if (u instanceof IHasFileProgress) {
				((IHasFileProgress)u).onFileProgress(progress);
				mAdapter.notifyDataSetInvalidated();
			}
		}
	}

	@Override
	public void onFilePicked(ActivityResult result, OldMainActivity activity) {
		for (Util u : UTILS) {
			if (u instanceof IHasFilePicker) {
				((IHasFilePicker)u).onFilePicked(result, activity);
				mAdapter.notifyDataSetInvalidated();
			}
		}
	} 
}
