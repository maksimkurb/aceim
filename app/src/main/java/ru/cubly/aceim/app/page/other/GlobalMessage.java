package ru.cubly.aceim.app.page.other;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import java.util.List;

import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.page.Page;
import ru.cubly.aceim.app.widgets.bottombar.BottomBar;
import ru.cubly.aceim.app.widgets.bottombar.BottomBarButton;
import ru.cubly.aceim.app.widgets.bottombar.BottomBarButtonInfo;

public class GlobalMessage extends Page {
	
	private static final LayoutParams BUTTON_LAYOUT = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	
	private final String title;
	private final Drawable icon;
	private final int textId;
	private final List<BottomBarButtonInfo> buttons;
	
	public GlobalMessage(String title, Drawable icon, int textId, List<BottomBarButtonInfo> buttons){
		this.title = title;
		this.icon = icon;
		this.textId = textId;
		this.buttons = buttons;
	}
	
	@Override
	public View createView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
		View view = inflater.inflate(R.layout.page_global_message, group, false);
		
		TextView text = (TextView) view.findViewById(R.id.text);
		text.setText(textId);
		
		BottomBar bar = (BottomBar) view.findViewById(R.id.bottom_bar);
		for (BottomBarButtonInfo info: buttons) {
			BottomBarButton button = new BottomBarButton(getMainActivity(), null);
			button.setLayoutParams(BUTTON_LAYOUT);
			button.setImageDrawable(info.icon);
			button.setOnClickListener(info);
			bar.addView(button);
		}		
		
		return view;
	}

	@Override
	public Drawable getIcon(Context context) {
		return icon;
	}

	@Override
	public String getTitle(Context context) {
		return title;
	}

}
