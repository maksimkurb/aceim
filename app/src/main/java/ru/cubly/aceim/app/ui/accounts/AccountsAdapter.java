package ru.cubly.aceim.app.ui.accounts;

import android.content.DialogInterface;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.core.AceIM;
import ru.cubly.aceim.app.core.BasicListAdapter;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.app.dataentity.AccountOptionKeys;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder> implements BasicListAdapter<Account> {

    private final List<Account> mAccounts = new ArrayList<>();

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);

        ViewHolder vh = new ViewHolder(v, new ViewHolder.IAccountViewHolderListener() {
            @Override
            public void onAccountDeleteClicked(View caller, int position) {
                final Account account = mAccounts.get(position);
                new AlertDialog.Builder(caller.getContext())
                        .setMessage(String.format(caller.getResources().getString(R.string.are_you_sure_you_want_to_remove), account.getSafeName()))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    AceIM.getInstance().getCoreService().deleteAccount(account);
                                } catch (RemoteException e) {
                                    AceIM.getInstance().handleException(e);
                                }
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }

            @Override
            public void onAccountActiveCheckedChanged(final CompoundButton caller, int position, final boolean isAccountActive) {
                int askId = isAccountActive ? R.string.this_account_to_be_enabled : R.string.this_account_to_be_disabled;
                final Account account = mAccounts.get(position);
                new AlertDialog.Builder(caller.getContext())
                        .setMessage(String.format(caller.getResources().getString(askId), account.getSafeName()))
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                account.setEnabled(isAccountActive);
                                caller.getContext() // TODO: get rid of this hack and save settings through core
                                        .getSharedPreferences(account.getAccountId(), 0)
                                        .edit()
                                        .putBoolean(AccountOptionKeys.DISABLED.name(), !account.isEnabled())
                                        .commit();
                                try {
                                    AceIM.getInstance().getCoreService().editAccount(account, null, account.getProtocolServicePackageName());
                                } catch (RemoteException e) {
                                    AceIM.getInstance().handleException(e);
                                }
                            }
                        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                caller.toggle(); // Revert ui changes
                            }
                        })
                        .show();
            }

            @Override
            public void onAccountSettingsClicked(View caller, int position) {

            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mAccounts.size();
    }


/*
    private class AccountListener {
        final Account mAccount;
        final ProtocolResources mProtocolResources;

        public AccountListener(Account account, ProtocolResources protocolResources) {
            this.mAccount = account;
            this.mProtocolResources = protocolResources;
        }

        public void onAccountDeleteClicked(final View v) {
            new AlertDialog.Builder(v.getContext())
                    .setMessage(String.format(v.getResources().getString(R.string.are_you_sure_you_want_to_remove), mAccount.getSafeName()))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                AceIM.getInstance().getCoreService().deleteAccount(mAccount);
                            } catch (RemoteException e) {
                                AceIM.getInstance().handleException(e);
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        public void onAccountActivityChanged(final View v, final boolean isActive) {
            int askId = isActive ? R.string.this_account_to_be_disabled : R.string.this_account_to_be_enabled;
            new AlertDialog.Builder(v.getContext())
                    .setMessage(String.format(v.getResources().getString(askId), mAccount.getSafeName()))
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mAccount.setEnabled(isActive);
                            v.getContext() // TODO: get rid of this hack and save settings through core
                                    .getSharedPreferences(mAccount.getAccountId(), 0)
                                    .edit()
                                    .putBoolean(AccountOptionKeys.DISABLED.name(), !mAccount.isEnabled())
                                    .commit();
                            try {
                                AceIM.getInstance().getCoreService().editAccount(mAccount, null, mAccount.getProtocolServicePackageName());
                            } catch (RemoteException e) {
                                AceIM.getInstance().handleException(e);
                            }
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            ((ToggleButton) v).toggle(); // Revert ui changes
                        }
                    })
                    .show();
        }
    }
*/

    @Override
    public boolean add(Account object) {


        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Account> collection) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean contains(Object object) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean remove(Object object) {
        return false;
    }

    @Override
    public boolean remove(int position) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }


    /*
    * View holder
    */
    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView accountAvatarImage;
        CircleImageView accountOnlineStatus;

        TextView accountName;

        ImageView protocolImage;
        TextView protocolName;

        ToggleButton accountActive;
        ImageButton accountDelete;

        public ViewHolder(View itemView, final IAccountViewHolderListener listener) {
            super(itemView);
            accountAvatarImage = (CircleImageView) itemView.findViewById(R.id.account_avatar);
            accountOnlineStatus = (CircleImageView) itemView.findViewById(R.id.account_online_status);
            accountName = (TextView) itemView.findViewById(R.id.account_name);
            protocolImage = (ImageView) itemView.findViewById(R.id.protocol_image);
            protocolName = (TextView) itemView.findViewById(R.id.protocol_name);
            accountActive = (ToggleButton) itemView.findViewById(R.id.account_active);
            accountDelete = (ImageButton) itemView.findViewById(R.id.account_delete);

            accountActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listener.onAccountActiveCheckedChanged(buttonView, getAdapterPosition(), isChecked);
                }
            });
            accountDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAccountDeleteClicked(v, getAdapterPosition());
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onAccountSettingsClicked(v, getAdapterPosition());
                }
            });
        }

        public interface IAccountViewHolderListener {
            void onAccountDeleteClicked(View caller, int position);
            void onAccountActiveCheckedChanged(CompoundButton caller, int position, boolean isAccountActive);
            void onAccountSettingsClicked(View caller, int position);
        }
    }
}
