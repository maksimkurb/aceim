package ru.cubly.aceim.client.widgets.pickers;

import aceim.api.dataentity.tkv.TKV;
import aceim.api.utils.Logger;
import ru.cubly.aceim.client.MainActivity;
import aceim.app.R;
import ru.cubly.aceim.client.service.ServiceUtils;
import ru.cubly.aceim.client.utils.ViewUtils;
import android.content.Intent;
import android.view.View;

public class FilePickerListener extends PickerListenerBase {

	public FilePickerListener(TKV tkv, ValuePickedListener listener, MainActivity activity) {
		super(tkv, listener, activity);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		try {
			activity.startActivityForResult(intent, ServiceUtils.getRequestCodeForActivity(tkv.getKey().hashCode()));
		} catch (Exception e) {
			Logger.log(e);
			ViewUtils.showAlertToast(activity, android.R.drawable.ic_menu_info_details, R.string.no_app_for_picking_file_found, null);
		}
	}
}
