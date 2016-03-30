package ru.cubly.aceim.app.themeable.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import ru.cubly.aceim.app.AceIMActivity;

public class StyleableLinearLayout extends LinearLayout {

	public StyleableLinearLayout(Context context) {
		super(context);
	}

	public StyleableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (context instanceof AceIMActivity) {
			((AceIMActivity)context).setStyle(this, attrs);
		}
	}
}
