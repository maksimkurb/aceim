package ru.cubly.aceim.app.page.accounts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.androidquery.AQuery;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.cubly.aceim.api.dataentity.ListFeature;
import ru.cubly.aceim.api.service.ApiConstants;
import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.app.AceImException;
import ru.cubly.aceim.app.OldMainActivity;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.app.dataentity.AccountOptionKeys;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.page.Page;
import ru.cubly.aceim.app.utils.DialogUtils;
import ru.cubly.aceim.app.utils.ViewUtils;

public class AccountsAdapter extends ArrayAdapter<Account> {

	private final Map<Account, AccountClickListener> mAccounts = new HashMap<Account, AccountClickListener>();

	public AccountsAdapter(OldMainActivity activity, List<Account> objects) {
		super(activity, R.layout.item_account, R.id.label_username, objects);

		fillClickListeners(objects);
	}

	private void fillClickListeners(List<Account> objects) {
		if (objects == null) {
			return;
		}
		
		OldMainActivity activity = (OldMainActivity) getContext();

		for (Account acc : objects) {
			
			AccountClickListener l = new AccountClickListener(acc, activity.getProtocolResourcesForAccount(acc));
			mAccounts.put(acc, l);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		
		AQuery aq = new AQuery(v);

		CheckBox disableBtn = (CheckBox) v.findViewById(R.id.btn_disable);
		ImageButton deleteBtn = (ImageButton) v.findViewById(R.id.btn_delete);
		
		final Account a = getItem(position);
		AccountClickListener l = mAccounts.get(a);
		Resources r;
		if (l.mResources != null) {
			try {
				r = l.mResources.getNativeResourcesForProtocol(getContext().getPackageManager());
				ListFeature status = (ListFeature) l.mResources.getFeature(ApiConstants.FEATURE_STATUS);
				if (status != null) {
					aq.id(R.id.image_protocol).image(r.getDrawable(status.getDrawables()[0]));
				}

				aq.id(R.id.btn_search_in_play).visibility(View.INVISIBLE);
				disableBtn.setVisibility(View.VISIBLE);			
			} catch (AceImException e) {
				Logger.log(e);
				return new FrameLayout(getContext());
			}
		} else {
			disableBtn.setVisibility(View.INVISIBLE);	
			aq.id(R.id.btn_search_in_play).visibility(View.VISIBLE).clicked(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = ViewUtils.getSearchPluginsInPlayStoreIntent(a);
					getContext().startActivity(i);
				}
			});
		}
		
		ViewUtils.fillIcon(R.id.image_icon, aq, a.getFilename(), getContext());
		
		disableBtn.setChecked(a.isEnabled());
		disableBtn.setOnClickListener(l);
		deleteBtn.setOnClickListener(l);
		v.setOnClickListener(l);

		return v;
	}

	private class AccountClickListener implements OnClickListener {

		final Account mAccount;
		final ProtocolResources mResources;

		public AccountClickListener(Account acc, ProtocolResources resources) {
			this.mAccount = acc;
			this.mResources = resources;
		}

		@Override
		public void onClick(View v) {
			final OldMainActivity activity = (OldMainActivity) getContext();
			if (v instanceof ImageButton) {
				if (v.getId() == R.id.btn_delete) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

					builder.setMessage(String.format(getContext().getResources().getString(R.string.are_you_sure_you_want_to_remove), mAccount.getSafeName())).setCancelable(false)
							.setPositiveButton(getContext().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									activity.accountRemoved(mAccount);
								}
							}).setNegativeButton(getContext().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							});
					AlertDialog dialog = builder.create();
					DialogUtils.showBrandedDialog(dialog);		
				}
			} else if (v instanceof CompoundButton) {
				final CompoundButton buttonView = (CompoundButton) v;
				AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

				int askId = buttonView.isChecked() ? R.string.this_account_to_be_enabled : R.string.this_account_to_be_disabled;
				builder.setMessage(String.format(getContext().getResources().getString(askId), mAccount.getSafeName())).setCancelable(false)
						.setPositiveButton(getContext().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mAccount.setEnabled(buttonView.isChecked());
								getContext()
									.getSharedPreferences(mAccount.getAccountId(), 0)
									.edit()
									.putBoolean(AccountOptionKeys.DISABLED.name(), !mAccount.isEnabled())
									.commit();
								try {
									activity.getCoreService().editAccount(mAccount, null, mAccount.getProtocolServicePackageName());
								} catch (RemoteException e) {
									activity.onRemoteException(e);
								}
							}
						}).setNegativeButton(getContext().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								buttonView.setChecked(!buttonView.isChecked());
							}
						});
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				Page.addAccountEditorPage(activity.getScreen(), mAccount);
			}
		}
	}
	
	@Override
	public void add(Account acc){
		super.add(acc);
		AccountClickListener l = new AccountClickListener(acc, ((OldMainActivity)getContext()).getProtocolResourcesForAccount(acc));
		mAccounts.put(acc, l);
	}
	
	@Override
	public void addAll(Collection<? extends Account> collection){
		for (Account acc : collection) {
			add(acc);
			AccountClickListener l = new AccountClickListener(acc, ((OldMainActivity)getContext()).getProtocolResourcesForAccount(acc));
			mAccounts.put(acc, l);
		}
	}
	
	@Override
	public void addAll(Account ... items){
		for (Account acc : items) {
			add(acc);
			AccountClickListener l = new AccountClickListener(acc, ((OldMainActivity)getContext()).getProtocolResourcesForAccount(acc));
			mAccounts.put(acc, l);
		}
	}
	
	@Override
	public void insert(Account acc, int index) {
		super.insert(acc, index);
		AccountClickListener l = new AccountClickListener(acc, ((OldMainActivity)getContext()).getProtocolResourcesForAccount(acc));
		mAccounts.put(acc, l);
	}
	
	@Override
	public void remove(Account object) {
		super.remove(object);
		mAccounts.remove(object);
	}
}
