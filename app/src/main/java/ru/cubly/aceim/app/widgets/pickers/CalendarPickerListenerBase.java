package ru.cubly.aceim.app.widgets.pickers;

import android.app.AlertDialog;
import android.view.View;

import java.text.DateFormat;
import java.util.Calendar;

import ru.cubly.aceim.api.dataentity.tkv.TKV;
import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.app.OldMainActivity;

public abstract class CalendarPickerListenerBase extends PickerListenerBase {

	protected abstract AlertDialog createDialog(Calendar cal);

	protected final DateFormat format;

	protected CalendarPickerListenerBase(TKV tkv, ValuePickedListener listener, OldMainActivity activity, DateFormat format) {
		super(tkv, listener, activity);
		this.format = format;
	}

	@Override
	public void onClick(View v) {
		final Calendar cal = Calendar.getInstance();
		long millis;
		try {
			if (tkv.getValue() != null) {
				millis = Long.parseLong(tkv.getValue());
			} else {
				millis = System.currentTimeMillis();
			}
		} catch (NumberFormatException e) {
			Logger.log(e);
			millis = System.currentTimeMillis();
		}

		cal.setTimeInMillis(millis);

		createDialog(cal).show();
	}

	public DateFormat getFormat() {
		return format;
	}
}
