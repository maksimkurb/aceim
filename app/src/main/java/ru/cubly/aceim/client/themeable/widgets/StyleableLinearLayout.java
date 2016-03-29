package ru.cubly.aceim.client.themeable.widgets;

import ru.cubly.aceim.client.AceIMActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

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
