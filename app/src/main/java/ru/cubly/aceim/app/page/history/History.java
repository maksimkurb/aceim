package ru.cubly.aceim.app.page.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.Message;
import ru.cubly.aceim.api.dataentity.MessageAckState;
import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.app.MainActivity;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.app.dataentity.listeners.IHasBuddy;
import ru.cubly.aceim.app.dataentity.listeners.IHasMessages;
import ru.cubly.aceim.app.service.ServiceUtils;
import ru.cubly.aceim.app.utils.ViewUtils;
import ru.cubly.aceim.app.page.Page;
import ru.cubly.aceim.app.page.chat.ChatMessageHolder;
import ru.cubly.aceim.app.widgets.adapters.MessagesAdapter;
import ru.cubly.aceim.app.widgets.bottombar.BottomBarButton;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class History extends Page implements IHasMessages, IHasBuddy {
	
	private static final String SAVE_PARAM_MESSAGES = "messages";
	private static final String SAVE_PARAM_FROM = "startFrom";
	
	private static final int MAX_MESSAGES_PER_READ = 25;

	private final Buddy mBuddy;

	private final ArrayList<ChatMessageHolder> mMessageHolders = new ArrayList<ChatMessageHolder>(25);
	private MessagesAdapter mMessageAdapter;
	
	private SwipeRefreshLayout mSwiper;
	private ListView mMessages;
	
	private BottomBarButton mDeleteBtn;
	private BottomBarButton mExportBtn;
	private BottomBarButton mCopyBtn;
	private BottomBarButton mCancelBtn;
	
	private int startFrom = 0;
	
	private final OnClickListener mDeleteClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			createConfirmDeleteDialog();			
		}
	};
	
	private final OnRefreshListener mRefreshListener = new OnRefreshListener() {
		
		@Override
		public void onRefresh() {
			mSwiper.setRefreshing(true);
			getStoredMessagesAsync();
		}
	};
	
	private final OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {
		
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			copyMode(view);
			return false;
		}
	};
	
	private final OnClickListener mCopyClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			String selectedText = mMessageAdapter.grabSelectedText(mMessages);			
			ServiceUtils.toClipboard(getMainActivity(),selectedText, getMainActivity().getString(R.string.default_key_value_format, getTitle(getMainActivity()), mBuddy.getSafeName()));
			readWriteMode();
			ViewUtils.showInformationToast(getMainActivity(), android.R.drawable.ic_menu_agenda, R.string.copied_to_clipboard, null);
		}
	};
	
	private final OnClickListener mCancelCopyListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			readWriteMode();
		}
	};

	public History(Buddy buddy) {
		this.mBuddy = buddy;
	}
	
	private void getStoredMessagesAsync() {
		AsyncTask<Void, Void, List<ChatMessageHolder>> messagesGetter = new AsyncTask<Void, Void, List<ChatMessageHolder>>() {

			@Override
			protected List<ChatMessageHolder> doInBackground(Void... params) {
				return getStoredMessages();
			}
			
			@Override
			protected void onPostExecute(List<ChatMessageHolder> messages){
				for (int i=messages.size()-1; i>=0; i--) {
					ChatMessageHolder holder = messages.get(i);
					mMessageAdapter.insert(holder, 0);
				}
				mSwiper.setRefreshing(false);
			}
		};
		
		messagesGetter.execute();
	}

	@Override
	public View createView(LayoutInflater inflater, ViewGroup group, Bundle saved){
		View view = inflater.inflate(R.layout.history, group, false); 
		
		mMessages = (ListView) view.findViewById(R.id.messages);
		mSwiper = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
		
		mDeleteBtn = (BottomBarButton) view.findViewById(R.id.delete);
		mExportBtn = (BottomBarButton) view.findViewById(R.id.export);
		mCopyBtn = (BottomBarButton) view.findViewById(R.id.copy);
		mCancelBtn = (BottomBarButton) view.findViewById(R.id.cancel);
		
		if (mMessageAdapter == null) {
			MainActivity activity = getMainActivity();
			try {
				Account a = activity.getCoreService().getAccount(mBuddy.getServiceId());
				mMessageAdapter = new MessagesAdapter(activity, a, mBuddy, activity.getThemesManager().getViewResources().getHistoryMessageItemLayout(), mMessageHolders);
			} catch (RemoteException e) {
				Logger.log(e);
			}
		}
		
		mMessages.setAdapter(mMessageAdapter);		
		mMessages.setOnItemLongClickListener(mItemLongClickListener);
		
		mSwiper.setOnRefreshListener(mRefreshListener);
		mSwiper.setColorSchemeResources(R.color.chat_message_sender_color_sent, R.color.chat_message_sender_color_received, R.color.chat_message_sender_color_service, R.color.background);
		
		mExportBtn.setVisibility(View.GONE);
		
		initClickListeners();
		recoverFromStoredData(saved);
		
		return view;
	}
	
	private void initClickListeners() {
		mDeleteBtn.setOnClickListener(mDeleteClickListener);
		mCopyBtn.setOnClickListener(mCopyClickListener);
		mCancelBtn.setOnClickListener(mCancelCopyListener);
	}

	private List<ChatMessageHolder> getStoredMessages() {
		List<ChatMessageHolder> messageHolders;
		try {
			List<Message> messages = getMainActivity().getCoreService().getMessages(mBuddy, startFrom, MAX_MESSAGES_PER_READ);
			messageHolders = new ArrayList<ChatMessageHolder>(messages.size());
			
			Account account = getMainActivity().getCoreService().getAccount(mBuddy.getServiceId());
			
			messageHolders.addAll(ViewUtils.wrapMessages(mBuddy, account, messages));
			
			startFrom += MAX_MESSAGES_PER_READ;
		} catch (RemoteException e) {
			getMainActivity().onRemoteException(e);
			messageHolders = Collections.emptyList();
		}
		
		return messageHolders;
	}
	
	@Override
	public void onSaveInstanceState(Bundle saver){
		saver.putParcelableArrayList(SAVE_PARAM_MESSAGES, mMessageHolders);
		saver.putInt(SAVE_PARAM_FROM, startFrom);
	}
	
	@Override
	public Bundle getPageDataForStorage(){
		Bundle saver = new Bundle();
		
		onSaveInstanceState(saver);
		
		return saver;
	}
	
	private void recoverFromStoredData(Bundle saved){
		if (mMessageHolders.size() > 0) {
			return;
		}
		
		if (saved != null && saved.containsKey(SAVE_PARAM_MESSAGES)) {
			startFrom = saved.getInt(SAVE_PARAM_FROM);	
			List<ChatMessageHolder> result = saved.getParcelableArrayList(SAVE_PARAM_MESSAGES);
			mMessageHolders.addAll(result);
		} else if (mMessageHolders.size() < 1) {
			getStoredMessagesAsync();				
		} 
	}

	@Override
	public Drawable getIcon(Context context) {
		return context.getResources().getDrawable(android.R.drawable.ic_menu_recent_history);
	}

	@Override
	public String getTitle(Context context) {
		return context.getString(R.string.history_header, mBuddy.getSafeName());
	}

	@Override
	public void onMessageReceived(Message message) {
		if (!hasMessagesOfBuddy(message.getServiceId(), message.getContactUid())) {
			return;
		}
		
		mMessageAdapter.add(new ChatMessageHolder(message, mBuddy.getSafeName()));
		
		if (getMainActivity().getScreen().getSelectedPage() == this) {
			ViewUtils.showAlertToast(getMainActivity(), R.drawable.ic_message, R.string.new_message_added, null);
		}
	}

	@Override
	public void onMessageAckReceived(long messageId, MessageAckState ack) {}

	@Override
	public boolean hasMessagesOfBuddy(byte serviceId, String buddyProtocolUid) {
		return serviceId == mBuddy.getServiceId() && buddyProtocolUid.equals(mBuddy.getProtocolUid());
	}

	@Override
	public void onBuddyStateChanged(List<Buddy> buddy) {}
	
	@Override
	public Buddy getBuddy() {
		return mBuddy;
	}

	@Override
	public void onBuddyIcon(byte serviceId, String protocolUid) {}

	@Override
	public boolean hasThisBuddy(byte serviceId, String protocolUid) {
		return serviceId == mBuddy.getServiceId() && protocolUid.equals(mBuddy.getProtocolUid());
	}
	
	private void createConfirmDeleteDialog() {
		final MainActivity activity = getMainActivity();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setCancelable(true)
			.setIcon(getIcon(activity))
			.setTitle(getTitle(activity))
			.setMessage(R.string.confirm_delete_history)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					boolean result = false;
					
					try {
						result = activity.getCoreService().deleteMessagesHistory(mBuddy);
					} catch (RemoteException e) {
						activity.onRemoteException(e);
					}
					
					if (result) {
						mMessageAdapter.clear();
					}
					
					dialog.dismiss();
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			
		builder.create().show();
	}
	
	private void copyMode(View starter) {
		mMessageAdapter.setCopyMode(true, starter);
		
		mDeleteBtn.setVisibility(View.GONE);
		
		mCopyBtn.setVisibility(View.VISIBLE);
		mCancelBtn.setVisibility(View.VISIBLE);
	}
	
	private void readWriteMode() {
		mMessageAdapter.setCopyMode(false, null);
		
		mDeleteBtn.setVisibility(View.VISIBLE);
		
		mCopyBtn.setVisibility(View.GONE);
		mCancelBtn.setVisibility(View.GONE);
	}
}
