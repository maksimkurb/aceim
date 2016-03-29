package ru.cubly.aceim.client.widgets.pickers;

import aceim.api.dataentity.tkv.TKV;
import ru.cubly.aceim.client.MainActivity;
import android.view.View.OnClickListener;

public abstract class PickerListenerBase implements OnClickListener {

	protected final TKV tkv;
	protected final ValuePickedListener listener;
	protected final MainActivity activity;
	
	public PickerListenerBase(TKV tkv, ValuePickedListener listener, MainActivity activity) {
		this.tkv = tkv;
		this.listener = listener;
		this.activity = activity;
	}
	
	public interface ValuePickedListener {
		void onValuePicked(String value);
	}
}
