package ru.cubly.aceim.app.page.chat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.BuddyGroup;
import ru.cubly.aceim.api.dataentity.ConnectionState;
import ru.cubly.aceim.api.dataentity.FileInfo;
import ru.cubly.aceim.api.dataentity.FileMessage;
import ru.cubly.aceim.api.dataentity.Message;
import ru.cubly.aceim.api.dataentity.MessageAckState;
import ru.cubly.aceim.api.dataentity.MultiChatRoom;
import ru.cubly.aceim.api.dataentity.OnlineInfo;
import ru.cubly.aceim.api.dataentity.TextMessage;
import ru.cubly.aceim.api.service.ApiConstants;
import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.api.utils.Logger.LoggerLevel;
import ru.cubly.aceim.api.utils.Utils;
import ru.cubly.aceim.app.MainActivity;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.SmileysManager;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.app.dataentity.AccountOptionKeys;
import ru.cubly.aceim.app.dataentity.ActivityResult;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.dataentity.listeners.IHasAccount;
import ru.cubly.aceim.app.dataentity.listeners.IHasBuddy;
import ru.cubly.aceim.app.dataentity.listeners.IHasFilePicker;
import ru.cubly.aceim.app.dataentity.listeners.IHasMessages;
import ru.cubly.aceim.app.service.ServiceUtils;
import ru.cubly.aceim.app.themeable.dataentity.ContactThemeResource;
import ru.cubly.aceim.app.utils.LinqRules.BuddyLinqRule;
import ru.cubly.aceim.app.utils.ViewUtils;
import ru.cubly.aceim.app.utils.linq.KindaLinq;
import ru.cubly.aceim.app.page.Page;
import ru.cubly.aceim.app.widgets.ResizeableRelativeLayout;
import ru.cubly.aceim.app.widgets.ResizeableRelativeLayout.OnResizeListener;
import ru.cubly.aceim.app.widgets.adapters.MessagesAdapter;
import ru.cubly.aceim.app.widgets.bottombar.BottomBarButton;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.androidquery.AQuery;

public class Chat extends Page implements IHasBuddy, IHasAccount, IHasMessages, IHasFilePicker {
	
	private static final String SAVE_PARAM_MESSAGES = "messages";
	private static final String SAVE_PARAM_URI = "uri";
	private static final String SAVE_PARAM_TEXT = "text";
	
	private static ContactThemeResource sContactResource;
	private static int sChatParticipantItemWidth = 0;
	
	private final Buddy mBuddy;
	private final Account mAccount;
	private final ProtocolResources mProtocolResources;
	
	private static InputMethodManager sInputMethodManager;
	
	private final ArrayList<ChatMessageHolder> mMessageHolders = new ArrayList<ChatMessageHolder>(25);
	private MessagesAdapter mMessageAdapter;
	
	private CharSequence mEditorUnsavedContent = null;
	private Uri mAwaitingUri;
	
	private EditText mEditor;
	private BottomBarButton mSendBtn;
	private BottomBarButton mSmileyBtn;
	private BottomBarButton mCopyBtn;
	private BottomBarButton mQuoteBtn;
	private BottomBarButton mCancelBtn;
	
	private ListView mMessages;
	private ExpandableListView mChatBuddies;
	
	private ImageView mStatus;
	private TextView mXStatusText;
	
	private boolean sendWithEnter = true;
	private boolean sendTyping = false;
	private boolean sendTypingGo = true;
	
	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4);
	
	private final OnClickListener mXStatusTextClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (TextUtils.isEmpty(mXStatusText.getText()) || mXStatusText.getLayoutParams().height == LayoutParams.WRAP_CONTENT) {
				mXStatusText.getLayoutParams().height = getMainActivity().getResources().getDimensionPixelSize(R.dimen.chat_buddy_status_height);
			} else {
				mXStatusText.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
			}
			
			((View)mXStatusText.getParent()).invalidate();
			((View)mXStatusText.getParent()).requestLayout();
		}
	};
	
	private final OnItemLongClickListener mItemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			copyMode(view);
			return true;
		}	
	};
	
	private final OnClickListener mCopyClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			String selectedText = mMessageAdapter.grabSelectedText(mMessages);			
			ServiceUtils.toClipboard(getMainActivity(),selectedText, getMainActivity().getString(R.string.default_key_value_format, mAccount.getSafeName(), mBuddy.getSafeName()));
			readWriteMode();
			ViewUtils.showInformationToast(getMainActivity(), android.R.drawable.ic_menu_agenda, R.string.copied_to_clipboard, null);
		}
	};
	
	private final OnClickListener mQuoteClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			String selectedText = mMessageAdapter.grabSelectedText(mMessages);			
			ViewUtils.insertToEditor(selectedText, mEditor);
			readWriteMode();
		}
	};
	
	private final OnClickListener mCancelCopyListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			readWriteMode();
		}
	};
	
	private Runnable freeToSendTypingRunable = new Runnable(){

		@Override
		public void run() {
			sendTypingGo = true;
		}
		
	};
	
	private final Runnable mScrollToEndRunnable = new Runnable() {
		
		@Override
		public void run() {
			if (mMessages.getCount() > 0) {
				mMessages.setSelection(mMessages.getCount() - 1);
			}
		}
	};
	
	private final Runnable mTypingNotificationsRunnable = new Runnable() {
		
		@Override
		public void run(){
			sendTypingNotification();
		}
	};
	
	private final OnResizeListener mResizeListener = new OnResizeListener() {
		
		@Override
		public void onSizeChanged(int w, int h, int oldw, int oldh) {
			Executors.defaultThreadFactory().newThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
					getMainActivity().runOnUiThread(mScrollToEndRunnable);
				}
			}).start();
		}
	};
	
	private final OnClickListener mSmileyButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			SmileysManager sm = getMainActivity().getSmileysManager();
			
			if (sm.isPopupShown()) {
				sm.hidePopup();
			} else {
				sm.showPopup(mEditor);
			}
		}
	};
	
	private final OnClickListener mSendClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			sendMessage(mEditor.getText().toString());
			mEditor.setText(null);
			getMainActivity().getSmileysManager().hidePopup();
			getMainActivity().runOnUiThread(mScrollToEndRunnable);
		}
	};
	
	private final TextWatcher mTextWatcher = new TextWatcher(){

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (sendTyping && sendTypingGo){
				Executors.defaultThreadFactory().newThread(mTypingNotificationsRunnable).start();
			}
		}

		@Override
		public void afterTextChanged(Editable s) {}
		
	};
	
	private final OnEditorActionListener mEditorActionListener = new OnEditorActionListener() {
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (sendWithEnter && actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN){
				sendMessage(mEditor.getText().toString());
				mEditor.setText(null);
				return true;
			} 
			return false;
		}
	};
	
	public Chat(Buddy buddy, Account account, ProtocolResources resources) {
		this.mBuddy = buddy;
		this.mAccount = account;
		this.mProtocolResources = resources;
	}

	private void sendMessage(final String text) {
		if (text == null || text.length() < 1)
			return;

		if (mAccount.getConnectionState() != ConnectionState.CONNECTED 
				|| (mBuddy instanceof MultiChatRoom && ((MultiChatRoom)mBuddy).getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1) < 0)) {
			ViewUtils.showAlertToast(getMainActivity(), R.drawable.ic_menu_login, R.string.connect_first, null);
			return;
		}
		
		final TextMessage message = new TextMessage(mBuddy.getServiceId(), mBuddy.getProtocolUid());
		message.setTime(System.currentTimeMillis());
		message.setText(text);
		message.setIncoming(false);
		
		prohibitTypingNotificationSending();
		
		mMessageAdapter.add(ViewUtils.message2MessageHolder(message, mBuddy, mAccount));
		
		AsyncTask<Message, Void, Long> messageSender = new AsyncTask<Message, Void, Long>() {

			@Override
			protected Long doInBackground(Message... params) {
				try {
					return getMainActivity().getCoreService().sendMessage(message);
				} catch (RemoteException e) {
					getMainActivity().onRemoteException(e);
					return 0L;
				}
			}
			
			@Override
			protected void onPostExecute(Long messageId) {
				message.setMessageId(messageId);
				mMessageAdapter.notifyDataSetChanged();
			}			
		};
		
		messageSender.execute(message);
	}

	@Override
	public View createView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
		initVariables();
		
		View view = inflater.inflate(R.layout.chat, group, false);
		mEditor = (EditText) view.findViewById(R.id.editor);
		
		mSendBtn = (BottomBarButton) view.findViewById(R.id.send);
		mSmileyBtn = (BottomBarButton) view.findViewById(R.id.smiley);
		mCopyBtn = (BottomBarButton) view.findViewById(R.id.copy);
		mQuoteBtn = (BottomBarButton) view.findViewById(R.id.quote);
		mCancelBtn = (BottomBarButton) view.findViewById(R.id.cancel);
		
		mMessages = (ListView) view.findViewById(R.id.messages);
		mChatBuddies = (ExpandableListView) view.findViewById(R.id.chat_buddies);
		mXStatusText = (TextView) view.findViewById(R.id.label_xstatus);
		
		if (sContactResource == null) {
			sContactResource = new ChatParticipantThemeResource(getMainActivity());
		}
		
		if (mBuddy instanceof MultiChatRoom) {
			mChatBuddies.setAdapter(new ChatParticipantsAdapter(((MultiChatRoom)mBuddy).getOccupants(), mProtocolResources));
			mChatBuddies.getLayoutParams().width = sChatParticipantItemWidth;
			mChatBuddies.setVisibility(shouldMultiChatControlBeVisible() ? View.VISIBLE : View.GONE);
		} else {
			mChatBuddies.setVisibility(View.GONE);
		}
		
		mStatus = (ImageView) view.findViewById(R.id.image_status);
		
		if (sInputMethodManager == null) {
			sInputMethodManager = (InputMethodManager) getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		}
		
		if (mMessageAdapter == null) {
			mMessageAdapter = new MessagesAdapter(getMainActivity(), mAccount, mBuddy, getMainActivity().getThemesManager().getViewResources().getChatMessageItemLayout(), mMessageHolders);
		}
		mMessages.setAdapter(mMessageAdapter);
		mMessages.setOnItemLongClickListener(mItemLongClickListener);
		
		mXStatusText.setOnClickListener(mXStatusTextClickListener);
		
		mSmileyBtn.setOnClickListener(mSmileyButtonClickListener);
		mSendBtn.setOnClickListener(mSendClickListener);
		mCopyBtn.setOnClickListener(mCopyClickListener);
		mQuoteBtn.setOnClickListener(mQuoteClickListener);
		mCancelBtn.setOnClickListener(mCancelCopyListener);
		
		mEditor.addTextChangedListener(mTextWatcher);
		mEditor.setOnEditorActionListener(mEditorActionListener);
		if (mEditorUnsavedContent != null) {
			mEditor.setText(mEditorUnsavedContent);
			mEditorUnsavedContent = null;
		}
		
		sendTyping = getMainActivity().getSharedPreferences(mAccount.getAccountId(), 0).getBoolean(AccountOptionKeys.TYPING_NOTIFICATIONS.getStringKey(), false);
		
		((ResizeableRelativeLayout)view).setResizeListener(mResizeListener);
		
		onBuddyStateChanged(Arrays.asList(mBuddy));
		onAccountIcon(mAccount.getServiceId());
		recoverFromStoredData(saved);
		
		return view;
	}
	
	private void initVariables() {
		if (mBuddy instanceof MultiChatRoom && sChatParticipantItemWidth < 1) {
			Resources themeResources = getMainActivity().getThemesManager().getCurrentThemeContext().getResources();
			sChatParticipantItemWidth = themeResources.getDimensionPixelSize(getMainActivity().getThemesManager().getViewResources().getGridItemSizeId());
		}		
	}

	@Override
	public void onSetMeSelected() {
		if (mMessageHolders.size() > 0) {
			resetUnread();		
			mScrollToEndRunnable.run();
		}
		
		manageEditorFocus();
	}
	
	@Override
	public void onSaveInstanceState(Bundle saver){
		try {
			saver.putParcelableArrayList(SAVE_PARAM_MESSAGES, mMessageHolders);
			saver.putCharSequence(SAVE_PARAM_TEXT, mEditor.getText());	
			saver.putParcelable(SAVE_PARAM_URI, mAwaitingUri);
		} catch (Exception e) {
			Logger.log(e.getMessage(), LoggerLevel.WTF);
		}
	}
	
	@Override
	public Bundle getPageDataForStorage(){
		Bundle saver = new Bundle();
		
		onSaveInstanceState(saver);
		
		return saver;
	}
	
	private void recoverFromStoredData(Bundle bundle){
		if (bundle != null) {
			bundle.setClassLoader(ChatMessageHolder.class.getClassLoader());
			
			mEditorUnsavedContent = bundle.getCharSequence(SAVE_PARAM_TEXT);
			mAwaitingUri = bundle.getParcelable(SAVE_PARAM_URI);			
		}
		
		initMessages(bundle);
	}

	private void resetUnread() {
		getMainActivity().resetUnread(mBuddy);
	}

	private void initMessages(final Bundle saved) {
		if (mMessageHolders.size() > 0) {
			return;
		}		
		
		AsyncTask<Void, Void, List<ChatMessageHolder>> messagesGetter = new AsyncTask<Void, Void, List<ChatMessageHolder>>() {

			@Override
			protected List<ChatMessageHolder> doInBackground(Void... params) {
				List<ChatMessageHolder> result;
				
				try {
					if (saved != null && saved.containsKey(SAVE_PARAM_MESSAGES) && mBuddy.getUnread() < 1) {
						result = saved.getParcelableArrayList(SAVE_PARAM_MESSAGES);
					} else if (mMessageHolders.size() < 1) {
						List<Message> lastMessages = getMainActivity().getCoreService().getLastMessages(mBuddy);
						
						result = ViewUtils.wrapMessages(mBuddy, mAccount, lastMessages);						
					} else {
						result = Collections.emptyList();
					}
					
				} catch (RemoteException e) {
					return Collections.emptyList();
				}
				
				return result;
			}
			
			@Override
			protected void onPostExecute(List<ChatMessageHolder> messages){
				mMessageHolders.addAll(messages);
				
				if (mMessageAdapter != null) {
					mMessageAdapter.notifyDataSetInvalidated();
				}
				
				resetUnread();
			}
		};
		
		messagesGetter.execute();
	}
	
	private void sendTypingNotification() {
		try {
			prohibitTypingNotificationSending();
			getMainActivity().getCoreService().sendTyping(mBuddy.getServiceId(), mBuddy.getProtocolUid());
		} catch (RemoteException e1) {
			getMainActivity().onRemoteException(e1);
		}
	}

	private void prohibitTypingNotificationSending() {
		sendTypingGo = false;
		executor.schedule(freeToSendTypingRunable, 5, TimeUnit.SECONDS);
	}

	@Override
	public Drawable getIcon(Context context) {
		Bitmap bitmap = ViewUtils.getIcon(context, mBuddy.getFilename());
		if (bitmap != null) {
			return new BitmapDrawable(context.getResources(), bitmap);
		} else {
			return context.getResources().getDrawable(R.drawable.dummy_icon);
		}		
	}

	@Override
	public String getTitle(Context context) {
		return mBuddy.getSafeName();
	}
	
	@Override
	public void onMessageReceived(Message message) {
		if (!hasMessagesOfBuddy(message.getServiceId(), message.getContactUid())) {
			return;
		}
		
		ChatMessageHolder holder = ViewUtils.message2MessageHolder(message, mBuddy, mAccount);
		
		mMessageAdapter.add(holder);
		mScrollToEndRunnable.run();
	}

	@Override
	public void onMessageAckReceived(long messageId, MessageAckState ack) {
		for (ChatMessageHolder item : mMessageHolders) {
			if (item.getMessage().getMessageId() == messageId) {
				item.setAckState(ack);
				mMessageAdapter.notifyDataSetChanged();
				break;
			}
		}
	}
	
	@Override
	public boolean hasMessagesOfBuddy(byte serviceId, String buddyProtocolUid) {
		return mBuddy.getServiceId() == serviceId && mBuddy.getProtocolUid().equals(buddyProtocolUid);
	}

	@Override
	public Account getAccount() {
		return mAccount;
	}


	@Override
	public void onOnlineInfoChanged(OnlineInfo info) {
		if (mAccount.getOnlineInfo() != info && mAccount.getServiceId() == info.getServiceId()) {
			mAccount.getOnlineInfo().merge(info);
		}
	}
	
	@Override
	public void onConnectionStateChanged(ConnectionState connState, int extraParameter) {
		if (mAccount.getConnectionState() != connState) {
			mAccount.setConnectionState(connState);
			
			if (mAccount.getConnectionState() != ConnectionState.CONNECTED) {
				for (Buddy b : mAccount.getBuddyList()) {
					ViewUtils.resetFeaturesForOffline(b.getOnlineInfo(), mProtocolResources, true);
				}
			}
		}
		
		if (connState != ConnectionState.CONNECTED) {
			onBuddyStateChanged(Arrays.asList(mBuddy));
		}
	}

	@Override
	public void onContactListUpdated(Account account) {
		Buddy b = KindaLinq.from(account.getBuddyList()).where(new BuddyLinqRule(mBuddy)).first();
		if (b != null) {
			mBuddy.merge(b);
		} else {
			//this buddy has been removed - closing this chat
			removeMe();
		}
	}

	@Override
	public void onBuddyStateChanged(List<Buddy> buddies) {
		if (buddies == null || buddies.size() < 1) return;
		
		for (Buddy buddy : buddies) {
			if (mBuddy instanceof MultiChatRoom && !(buddy instanceof MultiChatRoom)) {
				
				for (BuddyGroup occupantGroup : ((MultiChatRoom)mBuddy).getOccupants()) {
					for (Buddy occupant : occupantGroup.getBuddyList()) {
						if (occupant.getProtocolUid().equals(buddy.getProtocolUid())) {
							occupant.merge(buddy);
						}
					}
				}
			} else if (buddy.getProtocolUid().equals(mBuddy.getProtocolUid())) {
				mBuddy.merge(buddy);			
			}
		}
		
		//exit if UI is not yet initialized
		if (mStatus == null) {
			return;
		}
		
		ViewUtils.fillBuddyPlaceholder(getMainActivity(), mBuddy, new AQuery((View) mStatus.getParent()), mProtocolResources, sContactResource);
		
		if (mBuddy instanceof MultiChatRoom){
			((ChatParticipantsAdapter)mChatBuddies.getExpandableListAdapter()).notifyDataSetChanged();
		}
		
		//If xstatus has changed to empty, we must ensure it won`t disappear with WRAP_CONTENT height
		mXStatusText.getLayoutParams().height = getMainActivity().getResources().getDimensionPixelSize(R.dimen.chat_buddy_status_height);
	}

	@Override
	public Buddy getBuddy() {
		return mBuddy;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.chat_menu, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem location = menu.findItem(R.id.menuitem_send_location);
		
		if (location != null) {
			location.setVisible(mAccount.getConnectionState() == ConnectionState.CONNECTED);
		}
		
		MenuItem sendFile = menu.findItem(R.id.menuitem_send_file);
		if (sendFile != null) {
			sendFile.setVisible(
					mAccount.getConnectionState() == ConnectionState.CONNECTED
					&& mBuddy.getOnlineInfo().getFeatures().getBoolean(ApiConstants.FEATURE_FILE_TRANSFER, false));
		}
		
		MenuItem sendPhoto = menu.findItem(R.id.menuitem_send_photo);
		if (sendPhoto != null) {
			sendPhoto.setVisible(
					mAccount.getConnectionState() == ConnectionState.CONNECTED
					&& mBuddy.getOnlineInfo().getFeatures().getBoolean(ApiConstants.FEATURE_FILE_TRANSFER, false));
		}
		
		MenuItem joinChat = menu.findItem(R.id.menuitem_join_chat);
		if (joinChat != null) {
			joinChat.setVisible(
					mAccount.getConnectionState() == ConnectionState.CONNECTED
					&& mBuddy instanceof MultiChatRoom
					&& mBuddy.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1) < 0);
		}
		
		MenuItem leaveChat = menu.findItem(R.id.menuitem_leave_chat);
		if (leaveChat != null) {
			leaveChat.setVisible(
					mAccount.getConnectionState() == ConnectionState.CONNECTED
					&& mBuddy instanceof MultiChatRoom
					&& mBuddy.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1) > -1);
		}
		
		MenuItem toggleParticipants = menu.findItem(R.id.menuitem_participants);
		if (toggleParticipants != null) {
			toggleParticipants.setVisible(shouldMultiChatControlBeVisible());
			toggleParticipants.setTitle(mChatBuddies.getVisibility() == View.VISIBLE ? R.string.hide_participants : R.string.show_participants);
		}
	}
	
	private boolean shouldMultiChatControlBeVisible() {
		return mAccount.getConnectionState() == ConnectionState.CONNECTED
				&& mBuddy instanceof MultiChatRoom
				&& mBuddy.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1) > -1;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		MainActivity a = getMainActivity();
		switch(item.getItemId()) {
		case R.id.menuitem_close:
			removeMe();
			break;
		case R.id.menuitem_history:
			Page.getHistoryPage(a.getScreen(), mBuddy);
			break;
		case R.id.menuitem_send_file:
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/*");
			int requestCode = ServiceUtils.getRequestCodeForActivity(mBuddy.getEntityId().hashCode());
			
			//Android alters request code if startActivityForResult being started from a Fragment. We don't need that, so starting from an activity
			try {
				a.startActivityForResult(intent, requestCode);
			} catch (Exception e) {
				intent.setType("image/*");
				try {
					a.startActivityForResult(intent, requestCode);
				} catch (Exception e1) {
					ViewUtils.showAlertToast(a, android.R.drawable.ic_dialog_alert, R.string.no_app_for_picking_file_found, null);
				}				
			}
			break;
		case R.id.menuitem_send_photo:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			
			mAwaitingUri = Uri.fromFile(Utils.createLocalFileForReceiving(android.text.format.DateFormat.getLongDateFormat(a).format(Calendar.getInstance().getTime()) + ".jpg", mBuddy, System.currentTimeMillis()));
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mAwaitingUri);
			requestCode = ServiceUtils.getRequestCodeForActivity(mBuddy.getEntityId().hashCode());
			try {
				a.startActivityForResult(intent, requestCode);
			} catch (Exception e){
				ViewUtils.showAlertToast(a, android.R.drawable.ic_dialog_alert, R.string.no_app_for_picking_file_found, null);
			}
			break;
		case R.id.menuitem_send_location:
			try {
				a.getCoreService().sendLocation(mBuddy);
			} catch (RemoteException e) {
				a.onRemoteException(e);
			}
			break;
		case R.id.menuitem_join_chat:
			try {
				a.getCoreService().joinChat(mBuddy.getServiceId(), mBuddy.getProtocolUid());
			} catch (RemoteException e) {
				a.onRemoteException(e);
			}
			break;
		case R.id.menuitem_leave_chat:
			try {
				a.getCoreService().leaveChat(mBuddy.getServiceId(), mBuddy.getProtocolUid());
			} catch (RemoteException e) {
				a.onRemoteException(e);
			}
		case R.id.menuitem_participants:
			int v = mChatBuddies.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
			mChatBuddies.setVisibility(v);
			break;
		}
		return false;
	}

	@Override
	public void onAccountIcon(byte serviceId) {
		/*if (serviceId != mAccount.getServiceId()) {
			return;
		}
		
		Bitmap bicon = ViewUtils.getIcon(getMainActivity(), mAccount.getFilename());
		if (bicon != null) {
			mSendBtn.setImageBitmap(bicon);
		} else {
			mSendBtn.setImageResource(android.R.drawable.ic_menu_send);
		}*/
	}

	@Override
	public void onBuddyIcon(byte serviceId, String protocolUid) {}

	@Override
	public Buddy getBuddyWithParameters(byte serviceId, String protocolUid) {
		return mAccount.getServiceId() == serviceId ? mAccount.getBuddyByProtocolUid(protocolUid) : null;
	}

	@Override
	public boolean hasThisBuddy(byte serviceId, String protocolUid) {
		boolean result = serviceId == mBuddy.getServiceId() && protocolUid.equals(mBuddy.getProtocolUid());
		
		exit:
		if (!result && mBuddy instanceof MultiChatRoom) {
			for (BuddyGroup occupantGroup : ((MultiChatRoom)mBuddy).getOccupants()) {
				for (Buddy occupant : occupantGroup.getBuddyList()){
					if (occupant.getServiceId() == serviceId && occupant.getProtocolUid().equals(protocolUid)) {
						result = true;
						break exit;
					}
				}
			}
		}
		
		return result;
	}
	
	@Override
	public boolean onKeyDown(int i, KeyEvent event) {
		
		if (i == KeyEvent.KEYCODE_BACK) {
			if (getMainActivity().getSmileysManager().getSmileysPopup().isShown()) {
				getMainActivity().getSmileysManager().getSmileysPopup().hide();
			} else {
				onLeaveMe();
				Page.getContactListPage(getMainActivity(), mAccount);				
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onLeaveMe() {
		sInputMethodManager.hideSoftInputFromWindow(mEditor.getWindowToken(), 0);
		
		getMainActivity().getSmileysManager().getSmileysPopup().hide();
	}

	@Override
	public void onFilePicked(ActivityResult result, MainActivity activity) {
		if (result.getRequestCode() != ServiceUtils.getRequestCodeForActivity(mBuddy.getEntityId().hashCode()) || result.getResultCode() != Activity.RESULT_OK) {
			return;
		}
		
		String filePath;
		if (mAwaitingUri != null) {			
			filePath = mAwaitingUri.getPath();
			mAwaitingUri = null;
		} else {
			filePath = result.getData().getData().getPath();
		}
		
		File file = new File(filePath);
		
		if (!file.exists()) {
			Uri selectedImage = result.getData().getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = activity.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            file = new File(filePath);
            cursor.close();
		}
		
		if (!file.exists()) {
			Logger.log("No file found under path " + filePath, LoggerLevel.INFO);
			return;
		}
		
		Logger.log("File picked " + filePath, LoggerLevel.VERBOSE);
		
		FileInfo info = new FileInfo(mBuddy.getServiceId());
		info.setFilename(filePath);
		info.setSize(file.length());
		
		FileMessage message = new FileMessage(mBuddy.getServiceId(), mBuddy.getProtocolUid(), Arrays.asList(info));
		message.setIncoming(false);
		message.setTime(System.currentTimeMillis());
		
		try {
			activity.getCoreService().sendMessage(message);
		} catch (RemoteException e) {
			activity.onRemoteException(e);
		}
	}
	
	private void copyMode(View starter) {
		mMessageAdapter.setCopyMode(true, starter);
		
		mEditor.setVisibility(View.GONE);
		mSmileyBtn.setVisibility(View.GONE);
		mSendBtn.setVisibility(View.GONE);
		
		mQuoteBtn.setVisibility(View.VISIBLE);
		mCopyBtn.setVisibility(View.VISIBLE);
		mCancelBtn.setVisibility(View.VISIBLE);
	}
	
	private void readWriteMode() {
		mMessageAdapter.setCopyMode(false, null);
		
		mEditor.setVisibility(View.VISIBLE);
		mSmileyBtn.setVisibility(View.VISIBLE);
		mSendBtn.setVisibility(View.VISIBLE);
		
		mQuoteBtn.setVisibility(View.GONE);
		mCopyBtn.setVisibility(View.GONE);
		mCancelBtn.setVisibility(View.GONE);
		
		manageEditorFocus();
	}
	
	/**
	 * http://stackoverflow.com/questions/6977773/keyboard-not-shown-when-i-click-on-edittextview-in-android
	 */
	private void manageEditorFocus() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mEditor.requestFocus();
		} else {
			mEditor.clearFocus();
		}
	}

	@Override
	public boolean hasMenu(){
		return true;
	}
	
	private class ChatParticipantThemeResource extends ContactThemeResource {

		public ChatParticipantThemeResource(Context context) {
			super(context, 0);
			setBuddyStatusImageId(R.id.image_status);
			setIconImageId(R.id.image_icon);
			setTitleTextViewId(R.id.username);
			setXstatusTextViewId(R.id.label_xstatus);
			setExtraImageIDs(new int[] { R.id.image_extra_1, R.id.image_extra_2, R.id.image_extra_3, R.id.image_extra_4 });
		}
	}
}
