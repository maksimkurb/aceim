package ru.cubly.aceim.app.page.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.cubly.aceim.api.dataentity.FileProgress;
import ru.cubly.aceim.app.OldMainActivity;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.dataentity.ActivityResult;
import ru.cubly.aceim.app.dataentity.listeners.IHasFilePicker;
import ru.cubly.aceim.app.dataentity.listeners.IHasFileProgress;
import ru.cubly.aceim.app.page.utils.Utilities.Util;
import ru.cubly.aceim.app.utils.DialogUtils;
import ru.cubly.aceim.app.utils.ViewUtils;
import ru.cubly.aceim.app.widgets.bottombar.BottomBarButton;

public class AccountImporter implements Util, IHasFilePicker, IHasFileProgress {
	
	private static final byte REQUEST_CODE = (byte) 42;
	private static final long MAX_PROGRESS = 1000;
	
	private final OnClickListener mGetFileClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/*.aceim");
			try {
				mActivity.startActivityForResult(intent, REQUEST_CODE);
			} catch (Exception e) {
				ViewUtils.showAlertToast(mActivity, android.R.drawable.ic_dialog_alert, R.string.no_app_for_picking_file_found, null);
			}
		}
	};

	private final OldMainActivity mActivity;
	
	private BottomBarButton button;
	private ProgressBar progress;

	public AccountImporter(OldMainActivity activity) {
		this.mActivity = activity;
	}

	@Override
	public View getView(LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.util_item_file_progress, null);
		
		TextView title = (TextView) view.findViewById(android.R.id.title);
		title.setText(R.string.import_accounts);
		
		button = (BottomBarButton) view.findViewById(R.id.button);
		button.setImageResource(android.R.drawable.ic_menu_upload);
		
		progress = (ProgressBar) view.findViewById(R.id.progress);
		
		button.setOnClickListener(mGetFileClickListener);
		
		return view;
	}

	@Override
	public void onFilePicked(final ActivityResult result, final OldMainActivity activity) {
		if (result.getRequestCode() != REQUEST_CODE || result.getResultCode() != Activity.RESULT_OK) {
			return;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCancelable(true);
		
		final View view = LayoutInflater.from(activity).inflate(R.layout.password_dialog, null);
		view.findViewById(R.id.reenter_password).setVisibility(View.GONE);
		((TextView)view.findViewById(R.id.title)).setText(R.string.password);
		builder.setView(view);
		
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText pw1 = (EditText) view.findViewById(R.id.password_1);
				//EditText pw2 = (EditText) view.findViewById(R.id.password_2);
				
				String pw = pw1.getText().toString();
				if (TextUtils.isEmpty(pw)) {
					ViewUtils.showAlertToast(activity, android.R.drawable.ic_dialog_alert, R.string.X_cannot_be_empty, activity.getString(R.string.password));
					return;
				}
				
				button.setEnabled(false);
				
				progress.setVisibility(View.VISIBLE);
				
				FileProgress p = new FileProgress((byte) -1, REQUEST_CODE, result.getData().getData().getPath(), 0, 0, false, "", null);
				
				try {
					activity.getCoreService().importAccounts(pw, p);
				} catch (RemoteException e) {
					activity.onRemoteException(e);
				}
				
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});	
		
		DialogUtils.showBrandedDialog(builder.create());
	}

	@Override
	public void onFileProgress(FileProgress p) {
		if (p.getServiceId() > -1 || p.getMessageId() != REQUEST_CODE) {
			return;
		}
		
		progress.setVisibility((p.getTotalSizeBytes() > 0 && p.getSentBytes() >= p.getTotalSizeBytes()) || p.getError() != null ? View.INVISIBLE : View.VISIBLE);
		button.setEnabled((p.getTotalSizeBytes() > 0 && p.getSentBytes() >= p.getTotalSizeBytes()) || p.getError() != null);
		progress.setIndeterminate(p.getTotalSizeBytes() < 1);
		progress.setProgress((int) (MAX_PROGRESS * p.getSentBytes() / p.getTotalSizeBytes()));
		
		((View)progress.getParent()).setBackgroundResource(p.getError() != null ? R.drawable.criteria_bad : 0);
	}
}
