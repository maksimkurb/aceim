package ru.cubly.aceim.app.widgets.pickers;

import android.content.Intent;
import android.view.View;

import ru.cubly.aceim.api.dataentity.tkv.TKV;
import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.app.OldMainActivity;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.service.ServiceUtils;
import ru.cubly.aceim.app.utils.ViewUtils;

public class FilePickerListener extends PickerListenerBase {

	public FilePickerListener(TKV tkv, ValuePickedListener listener, OldMainActivity activity) {
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
