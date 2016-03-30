package ru.cubly.aceim.app.themeable.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import ru.cubly.aceim.app.AceIMActivity;

public class StyleableRelativeLayout extends RelativeLayout {

	public StyleableRelativeLayout(Context context) {
		super(context);
	}

	public StyleableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (context instanceof AceIMActivity) {
			((AceIMActivity)context).setStyle(this, attrs);
		}
	}

	public StyleableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (context instanceof AceIMActivity) {
			((AceIMActivity)context).setStyle(this, attrs);
		}
	}

}
