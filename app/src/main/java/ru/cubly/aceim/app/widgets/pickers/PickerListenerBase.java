package ru.cubly.aceim.app.widgets.pickers;

import android.view.View.OnClickListener;

import ru.cubly.aceim.api.dataentity.tkv.TKV;
import ru.cubly.aceim.app.OldMainActivity;

public abstract class PickerListenerBase implements OnClickListener {

	protected final TKV tkv;
	protected final ValuePickedListener listener;
	protected final OldMainActivity activity;
	
	public PickerListenerBase(TKV tkv, ValuePickedListener listener, OldMainActivity activity) {
		this.tkv = tkv;
		this.listener = listener;
		this.activity = activity;
	}
	
	public interface ValuePickedListener {
		void onValuePicked(String value);
	}
}
