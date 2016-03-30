package ru.cubly.aceim.app.page.other;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.page.Page;

public class Splash extends Page {

	@Override
	public View createView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
		return inflater.inflate(R.layout.page_splash, group, false);
	}

	@Override
	public Drawable getIcon(Context context) {
		return context.getResources().getDrawable(R.drawable.ic_launcher);
	}

	@Override
	public String getTitle(Context context) {
		return context.getResources().getString(R.string.wait);
	}
}
