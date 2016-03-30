package ru.cubly.aceim.app.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;

import ru.cubly.aceim.api.dataentity.Buddy;
import ru.cubly.aceim.api.dataentity.ConnectionState;
import ru.cubly.aceim.api.dataentity.ListFeature;
import ru.cubly.aceim.api.dataentity.Message;
import ru.cubly.aceim.api.dataentity.MultiChatRoom;
import ru.cubly.aceim.api.dataentity.OnlineInfo;
import ru.cubly.aceim.api.dataentity.ProtocolServiceFeature;
import ru.cubly.aceim.api.service.ApiConstants;
import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.api.utils.Logger.LoggerLevel;
import ru.cubly.aceim.app.AceImException;
import ru.cubly.aceim.app.Constants;
import ru.cubly.aceim.app.OldMainActivity;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.dataentity.Account;
import ru.cubly.aceim.app.dataentity.GlobalOptionKeys;
import ru.cubly.aceim.app.dataentity.ProtocolResources;
import ru.cubly.aceim.app.page.Page;
import ru.cubly.aceim.app.page.about.About;
import ru.cubly.aceim.app.page.chat.Chat;
import ru.cubly.aceim.app.page.chat.ChatMessageHolder;
import ru.cubly.aceim.app.page.history.History;
import ru.cubly.aceim.app.preference.OptionsActivity;
import ru.cubly.aceim.app.themeable.dataentity.ContactThemeResource;
import ru.cubly.expandablegrid.ExpandableGridView;

public final class ViewUtils {

	private ViewUtils() {
	}

	@SuppressWarnings("unchecked")
	private static final Class<? extends Page>[] ALLOWED_PAGES_FOR_STORING = new Class[] { Chat.class, History.class, About.class };

	static final String BUDDYICON_FILEEXT = ".ico";
	static final String BUDDYICONHASH_FILEEXT = ".hash";

	@SuppressLint("DefaultLocale")
	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	/**
	 * Checks if the device is a tablet or a phone - another version
	 * 
	 * @param activityContext
	 *            The Activity Context.
	 * @return Returns true if the device is a Tablet
	 */
	public static boolean isTablet(Context context) {
		Logger.log("isTablet checking", LoggerLevel.VERBOSE);
		if (Build.VERSION.SDK_INT < 11) {
			return false;
		} else if (Build.VERSION.SDK_INT < 14) {
			return true;
		} else {
			try {
				// Compute screen size
				DisplayMetrics dm = context.getResources().getDisplayMetrics();
				float screenWidth = dm.widthPixels / dm.densityDpi;
				float screenHeight = dm.heightPixels / dm.densityDpi;
				double size = Math.sqrt(Math.pow(screenWidth, 2) + Math.pow(screenHeight, 2));
				// Tablet devices should have a screen size greater than 6
				// inches
				return size > 6;
			} catch (Throwable t) {
				Logger.log(t);
				return false;
			}
		}
	}

	public static void showAlertToast(Context context, int iconId, int textId, String params) {
		showInformationToast(context, iconId, textId, params);
	}

	public static void showInformationToast(Context context, Object icon, int textId, String params) {
		Logger.log("Show info toast", LoggerLevel.VERBOSE);
		View v = LayoutInflater.from(context).inflate(R.layout.info_toast, null);
		ImageView iconView = (ImageView) v.findViewById(R.id.icon);
		TextView text = (TextView) v.findViewById(R.id.text);

		if (icon == null) {
			// iconView.getLayoutParams().width = 0;
		} else if (icon instanceof Integer) {
			iconView.setImageResource((Integer) icon);
		} else if (icon instanceof Drawable) {
			iconView.setImageDrawable((Drawable) icon);
		} else if (icon instanceof Bitmap) {
			iconView.setImageBitmap((Bitmap) icon);
		}

		if (params != null) {
			String contentText = context.getString(textId, params);
			text.setText(contentText);
		} else {
			text.setText(textId);
		}

		Toast t = createToast(context, v);
		t.setDuration(Toast.LENGTH_LONG);

		int offset = context.getResources().getDimensionPixelSize(R.dimen.default_padding);

		t.setGravity(Gravity.LEFT | Gravity.TOP, offset, offset);
		t.show();
	}

	public static Toast createToast(Context context, View view) {
		Logger.log("Show toast", LoggerLevel.VERBOSE);
		Toast toast = new Toast(context);

		// RelativeLayout container = (RelativeLayout)
		// LayoutInflater.from(context).inflate(R.layout.toast_base, null);

		// container.addView(view, 0, new
		// RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
		// RelativeLayout.LayoutParams.WRAP_CONTENT));
		// container.setPadding(0, 0, 0, 0);
		toast.setView(view);
		return toast;
	}

	public static Drawable getAccountStatusIcon(Context context, Account account, ProtocolResources protocolResources) {
		try {
			Resources nRes = protocolResources.getNativeResourcesForProtocol(context.getPackageManager());

			int ic = 0;
			switch (account.getConnectionState()) {
			case CONNECTED:
				byte status = account.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1);
				return getConnectedStatusIcon(protocolResources, status, context);
			case CONNECTING:
			case DISCONNECTING:
				ic = nRes.getIdentifier(ApiConstants.RESOURCE_DRAWABLE_CONNECTING, "drawable", protocolResources.getPackageId());
				break;
			default:
				ic = nRes.getIdentifier(ApiConstants.RESOURCE_DRAWABLE_OFFLINE, "drawable", protocolResources.getPackageId());
				break;
			}

			return nRes.getDrawable(ic);
		} catch (Exception e) {
			Logger.log(e);
			return null;
		}
	}

	private static Drawable getConnectedStatusIcon(ProtocolResources protocolResources, byte status, Context context) {
		try {
			Resources nRes = protocolResources.getNativeResourcesForProtocol(context.getPackageManager());

			ListFeature statusFeature;
			if ((statusFeature = (ListFeature) protocolResources.getFeature(ApiConstants.FEATURE_STATUS)) != null && status > -1) {
				int[] stIcons = statusFeature.getDrawables();

				return nRes.getDrawable(stIcons[status]);
			} else {
				int onlineIcon = nRes.getIdentifier(ApiConstants.RESOURCE_DRAWABLE_ONLINE, "drawable", protocolResources.getPackageId());
				return nRes.getDrawable(onlineIcon);
			}
		} catch (Exception e) {
			Logger.log(e);
			return null;
		}
	}

	private static String getConnectedStatusName(ProtocolResources protocolResources, byte status, Context context) {
		try {
			Resources nRes = protocolResources.getNativeResourcesForProtocol(context.getPackageManager());

			ListFeature statusFeature;
			if ((statusFeature = (ListFeature) protocolResources.getFeature(ApiConstants.FEATURE_STATUS)) != null && status > -1) {
				int[] stNames = statusFeature.getNames();

				return nRes.getString(stNames[status]);
			} else {
				return context.getString(R.string.online);
			}
		} catch (Exception e) {
			Logger.log(e);
			return null;
		}
	}

	public static String getAccountStatusName(Context context, Account account, ProtocolResources protocolResources) {
		try {
			int ic;
			switch (account.getConnectionState()) {
			case CONNECTED:
				byte status = account.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1);
				return getConnectedStatusName(protocolResources, status, context);
			case CONNECTING:
				ic = R.string.connecting;
				break;
			default:
				ic = R.string.disconnected;
				break;
			}

			return context.getString(ic);
		} catch (Exception e) {
			Logger.log(e);
			return null;
		}
	}

	public static int getAccountStatusIcon(Context mContext, Account account) {
		int ic;

		switch (account.getConnectionState()) {
		case CONNECTED:
			ic = android.R.drawable.presence_online;
			break;
		case CONNECTING:
		case DISCONNECTING:
			ic = android.R.drawable.presence_away;
			break;
		default:
			ic = android.R.drawable.presence_offline;
			break;
		}

		return ic;
	}

	public static Drawable getBuddyStatusIcon(Context context, Buddy buddy, ProtocolResources protocolResources) {
		try {
			Resources nRes = protocolResources.getNativeResourcesForProtocol(context.getPackageManager());
			byte status = buddy.getOnlineInfo().getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1);

			if (status > -1) {
				return getConnectedStatusIcon(protocolResources, status, context);
			} else {
				int offlineIcon = nRes.getIdentifier(ApiConstants.RESOURCE_DRAWABLE_OFFLINE, "drawable", protocolResources.getPackageId());
				return nRes.getDrawable(offlineIcon);
			}
		} catch (Exception e) {
			Logger.log(e);
			return null;
		}
	}

	public static Intent getSearchPluginsInPlayStoreIntent(Account account) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("market://search?q=aceim"));
		return i;
	}

	public static Intent getOpenFileInCorrespondingApplicationIntent(String filePath) {
		MimeTypeMap mimeMap = MimeTypeMap.getSingleton();
		String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
		String mime = mimeMap.getMimeTypeFromExtension(extension);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + filePath), mime);
		return intent;
	}

	public static String getFormattedXStatus(OnlineInfo info, ConnectionState connectionState, Context context, ProtocolResources resources) {
		try {
			Resources res = resources.getNativeResourcesForProtocol(context.getPackageManager());

			byte value = -1;
			if (info.getFeatures().getByte(ApiConstants.FEATURE_STATUS, (byte) -1) < 0) {
				if (connectionState != null && connectionState == ConnectionState.CONNECTED) {
					return context.getString(R.string.online);
				} else {
					return context.getString(R.string.offline);
				}
			} else if (!TextUtils.isEmpty(info.getXstatusName()) || !TextUtils.isEmpty(info.getXstatusDescription())) {
				if (!TextUtils.isEmpty(info.getXstatusName()) && !TextUtils.isEmpty(info.getXstatusDescription())) {
					return context.getResources().getString(R.string.xstatus_text_format, info.getXstatusName(), info.getXstatusDescription());
				} else {
					return TextUtils.isEmpty(info.getXstatusDescription()) ? info.getXstatusName() : info.getXstatusDescription();
				}
			} else if ((value = info.getFeatures().getByte(ApiConstants.FEATURE_XSTATUS, (byte) -1)) > -1) {
				return res.getString(((ListFeature) resources.getFeature(ApiConstants.FEATURE_XSTATUS)).getNames()[value]);
			} else {
				value = info.getFeatures().getByte(ApiConstants.FEATURE_STATUS);
				return getConnectedStatusName(resources, value, context);
			}
		} catch (Exception e) {
			Logger.log(e);
			return "";
		}
	}

	public static void storeImageFile(Context context, byte[] bytes, String filename, String hash, Runnable runOnFinish) {
		FileAsyncSaver iconSaver = new FileAsyncSaver(context, filename + BUDDYICON_FILEEXT, bytes, runOnFinish);
		Executors.defaultThreadFactory().newThread(iconSaver).start();
		if (hash != null) {
			FileAsyncSaver hashSaver = new FileAsyncSaver(context, filename + BUDDYICONHASH_FILEEXT, hash.getBytes(), runOnFinish);
			Executors.defaultThreadFactory().newThread(hashSaver).start();
		}
	}

	private static final class FileAsyncSaver implements Runnable {

		private final String fileName;
		private final byte[] contents;
		private final Runnable runOnFinish;
		private final Context context;

		public FileAsyncSaver(Context context, String fileName, byte[] contents, Runnable runOnFinish) {
			this.fileName = fileName;
			this.contents = contents;
			this.runOnFinish = runOnFinish;
			this.context = context;
		}

		@Override
		public void run() {
			if (contents == null) {
				Logger.log("No content to save", LoggerLevel.VERBOSE);
				return;
			}

			FileOutputStream fos = null;
			try {
				fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
				fos.write(contents);
				fos.close();

				if (runOnFinish != null) {
					runOnFinish.run();
				}
			} catch (FileNotFoundException e) {
				Logger.log(e.toString(), LoggerLevel.WTF);
			} catch (Exception e) {
				Logger.log(e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						Logger.log(e);
					}
				}
			}
		}
	}

	public static Intent getOpenOptionsIntent(OldMainActivity oldMainActivity, Account account) {
		Intent i = new Intent(oldMainActivity, OptionsActivity.class);
		if (account != null) {
			i.putExtra(Constants.INTENT_EXTRA_ACCOUNT, account);
		}

		return i;
	}

	public static boolean allowPageStoring(Page page) {
		for (Class<? extends Page> pageClass : ALLOWED_PAGES_FOR_STORING) {
			if (pageClass == page.getClass()) {
				return true;
			}
		}
		return false;
	}

	public static Class<? extends Page> getPageClassByPageId(String pageId) {
		for (Class<? extends Page> cls : ALLOWED_PAGES_FOR_STORING) {
			if (pageId.equals(cls.getSimpleName())) {
				return cls;
			}
		}

		return null;
	}

	public static String getFileNameFromPath(String filePath) {
		return filePath.contains(File.separator) ? filePath.substring(filePath.lastIndexOf(File.separator) + File.separator.length(), filePath.length()) : filePath;
	}

	public static Uri stringAsIntentDataUri(String string) {
		return Uri.parse("aceim://" + string);
	}

	public static void removeIcon(Context context, String filename) {
		context.deleteFile(filename + BUDDYICON_FILEEXT);
	}

	public static void fillBuddyPlaceholder(Context context, Buddy buddy, View container, ProtocolResources protocolResources, ContactThemeResource themeResources, int position, int groupPosition, AbsListView parent) {

		AQuery aq = new AQuery(container);

		if (position < 0 || parent == null) {
			fillBuddyPlaceholder(context, buddy, aq, protocolResources, themeResources);
			return;
		}
		
		View targetForDelay;
		int itemPosition = 0;
		
		if (parent instanceof ExpandableGridView) {
			targetForDelay = container.getParent() != null ? (View) container.getParent() : container;
			for (; itemPosition<parent.getChildCount(); itemPosition++) {
				if (parent.getChildAt(itemPosition) == targetForDelay) break;
			}
		} else {
			targetForDelay = container;
			itemPosition = position;
		}
		
		boolean shouldDelay;
		if (parent instanceof ExpandableGridView) {
			shouldDelay = AQueryUtils.shouldDelay(groupPosition, itemPosition, targetForDelay, parent, buddy.getFilename());
		} else if (parent instanceof ExpandableListView) {
			shouldDelay = aq.shouldDelay(groupPosition, itemPosition, itemPosition == (parent.getChildCount() - 1), targetForDelay, parent, buddy.getFilename());
		} else {
			shouldDelay = aq.shouldDelay(position, container, parent, buddy.getFilename());
		}

		aq.id(themeResources.getTitleTextViewId()).text(buddy.getSafeName());
		
		if (shouldDelay) {
			aq.id(themeResources.getBuddyStatusImageId()).image(null, 1f);
			aq.id(themeResources.getIconImageId()).image(null, 1f);
		} else {
			fillBuddyPlaceholder(context, buddy, aq, protocolResources, themeResources);
		}
	}

	public static void fillBuddyPlaceholder(Context context, Buddy buddy, AQuery aq, ProtocolResources protocolResources, ContactThemeResource themeResources) {
		
		aq.id(themeResources.getBuddyStatusImageId()).image(getBuddyStatusIcon(context, buddy, protocolResources));
		aq.id(themeResources.getXstatusTextViewId()).text(getFormattedXStatus(buddy.getOnlineInfo(), null, context, protocolResources));

		Resources res;
		try {
			res = protocolResources.getNativeResourcesForProtocol(context.getPackageManager());
		} catch (AceImException e) {
			Logger.log(e);
			return;
		}

		int imagesIndex = 0;

		int[] extraImageIDs = themeResources.getExtraImageIDs();

		Bundle features = buddy.getOnlineInfo().getFeatures();
		synchronized (features) {
			for (String featureId : features.keySet()) {
				ProtocolServiceFeature feature = protocolResources.getFeature(featureId);

				if (feature == null) {
					Logger.log("Unknown protocol feature: " + featureId, LoggerLevel.INFO);
					continue;
				}

				if (!feature.isShowInIconList()) {
					continue;
				}

				if (feature instanceof ListFeature && !feature.getFeatureId().equals(ApiConstants.FEATURE_STATUS)) {
					ListFeature lf = (ListFeature) feature;
					byte value = buddy.getOnlineInfo().getFeatures().getByte(featureId, (byte) -1);

					if (value > -1) {
						aq.id(extraImageIDs[imagesIndex]).visibility(View.VISIBLE).image(res.getDrawable(lf.getDrawables()[value]));
						imagesIndex++;
					}
				} else {
					if (feature.getIconId() != 0) {
						aq.id(extraImageIDs[imagesIndex]).visibility(View.VISIBLE).image(res.getDrawable(feature.getIconId()));
						imagesIndex++;
					}
				}
			}
		}
		for (int i = imagesIndex; i < extraImageIDs.length; i++) {
			aq.id(extraImageIDs[i]).visibility(View.GONE);
		}

		fillIcon(themeResources.getIconImageId(), aq, buddy.getFilename(), context);
	}

	public static void fillAccountPlaceholder(Context context, Account account, View container, ProtocolResources protocolResources) {
		AQuery aq = new AQuery(container);

		int[] extraImageIDs = new int[] { R.id.image_extra_1, R.id.image_extra_2, R.id.image_extra_3, R.id.image_extra_4 };

		aq.id(R.id.image_status).image(getAccountStatusIcon(context, account, protocolResources));
		aq.id(R.id.label_xstatus).text(getFormattedXStatus(account.getOnlineInfo(), account.getConnectionState(), context, protocolResources));

		Resources res;
		try {
			res = protocolResources.getNativeResourcesForProtocol(context.getPackageManager());
		} catch (Exception e) {
			Logger.log(e);
			return;
		}

		int imagesIndex = 0;

		Bundle features = account.getOnlineInfo().getFeatures();
		synchronized (features) {
			for (String featureId : features.keySet()) {
				ProtocolServiceFeature feature = protocolResources.getFeature(featureId);

				if (feature == null) {
					Logger.log("Unknown protocol feature: " + featureId, LoggerLevel.INFO);
					continue;
				}

				if (!feature.isShowInIconList() || feature.getFeatureId().equals(ApiConstants.FEATURE_STATUS)) {
					continue;
				}

				if (feature instanceof ListFeature) {
					ListFeature lf = (ListFeature) feature;
					byte value = account.getOnlineInfo().getFeatures().getByte(featureId, (byte) -1);

					if (value > -1) {
						aq.id(extraImageIDs[imagesIndex]).visibility(View.VISIBLE).image(res.getDrawable(lf.getDrawables()[value]));
						imagesIndex++;
					}
				} else {
					if (feature.getIconId() != 0) {
						aq.id(extraImageIDs[imagesIndex]).visibility(View.VISIBLE).image(res.getDrawable(feature.getIconId()));
						imagesIndex++;
					}
				}
			}
		}
		for (int i = imagesIndex; i < extraImageIDs.length; i++) {
			aq.id(extraImageIDs[i]).visibility(View.GONE);
		}
		fillIcon(R.id.image_icon, aq, account.getFilename(), context);
	}

	@SuppressWarnings("deprecation")
	public static void setWallpaperMode(Activity activity, View target) {
		boolean forceDrawWallpaper = activity.getSharedPreferences(Constants.SHARED_PREFERENCES_GLOBAL, 0).getBoolean(GlobalOptionKeys.FORCE_DRAW_WALLPAPER.name(),
				Boolean.parseBoolean(activity.getString(R.string.default_force_draw_wallpaper)));

		if (forceDrawWallpaper) {
			BitmapDrawable wallpaper = (BitmapDrawable) activity.getWallpaper();

			if (wallpaper == null) {
				Logger.log("Unsupported wallpaper", LoggerLevel.DEBUG);
				return;
			}

			wallpaper.setDither(false);

			if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				wallpaper.setGravity(wallpaper.getBitmap().getHeight() <= wallpaper.getBitmap().getWidth() ? Gravity.CENTER_HORIZONTAL | Gravity.FILL_VERTICAL : Gravity.CENTER_VERTICAL | Gravity.FILL_HORIZONTAL);
			} else {
				wallpaper.setGravity(wallpaper.getBitmap().getHeight() <= wallpaper.getBitmap().getWidth() ? Gravity.CENTER_VERTICAL | Gravity.FILL_HORIZONTAL : Gravity.CENTER_HORIZONTAL | Gravity.FILL_VERTICAL);
			}

			target.setBackgroundDrawable(wallpaper);
		} else {
			target.setBackgroundColor(Color.TRANSPARENT);
		}
	}

	public static void resetFeaturesForOffline(OnlineInfo info, ProtocolResources mProtocolResources, boolean resetStatus) {
		for (String featureKey : new ArrayList<String>(info.getFeatures().keySet())) {
			ProtocolServiceFeature feature = mProtocolResources.getFeature(featureKey);

			if (feature == null || !feature.isAvailableOffline()) {
				info.getFeatures().remove(featureKey);
			}
		}

		if (resetStatus) {
			info.getFeatures().remove(ApiConstants.FEATURE_STATUS);
		}
	}

	public static File getBitmapFile(Context context, String filename) {
		return new File(context.getFilesDir().getAbsolutePath() + File.separator + filename + BUDDYICON_FILEEXT);
	}

	public static String getIconHash(Context context, String filename) {
		try {
			return new Scanner(new File(context.getFilesDir().getAbsolutePath() + File.separator + filename + BUDDYICONHASH_FILEEXT)).useDelimiter("\\A").next();
		} catch (Exception e) {
			return null;
		}
	}

	public static Bitmap getIcon(Context context, String filename) {
		return getIcon(context, filename, -1, -1);
	}

	public static Bitmap getIcon(Context context, String filename, int width, int height) {
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(filename + BUDDYICON_FILEEXT);
		} catch (Exception e) {
		}

		if (fis == null)
			return null;

		BitmapFactory.Options options = new BitmapFactory.Options();

		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(fis, null, options);

		if (!checkAvailableRamForBitmap(options.outHeight, options.outWidth))
			return null;

		try {
			fis = context.openFileInput(filename + BUDDYICON_FILEEXT);
		} catch (Exception e) {
		}

		options.inJustDecodeBounds = false;
		options.inDither = true;
		options.inScaled = false;
		options.inPurgeable = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;

		Bitmap b = BitmapFactory.decodeStream(fis, null, options);
		if (b == null || (width < 1 && height < 1)) {
			return b;
		} else {
			if (width < 1 || height < 1) {
				if (width < 1) {
					width = height * options.outWidth / options.outHeight;
				}

				if (height < 1) {
					height = width * options.outHeight / options.outWidth;
				}
			}

			Bitmap scaled = Bitmap.createScaledBitmap(b, width, height, false);
			b.recycle();

			return scaled;
		}
	}

	private static synchronized final boolean checkAvailableRamForBitmap(int h, int w) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			if ((h * w * 2) > (Debug.getNativeHeapFreeSize() * 0.75)) {
				Logger.log("LOW MEMORY " + Runtime.getRuntime().freeMemory());
				return false;
			}
		} else {
			if ((h * w * 2) > (Runtime.getRuntime().freeMemory() * 0.75)) {
				Logger.log("LOW MEMORY " + Runtime.getRuntime().freeMemory());
				return false;
			}
		}

		return true;
	}

	public static boolean hasIcon(Context context, String filename) {
		FileInputStream fis = null;
		try {
			fis = context.openFileInput(filename + BUDDYICON_FILEEXT);
			fis.close();
		} catch (Exception e) {
		}

		return fis != null;
	}

	public static void fillIcon(int imageIcon, View v, String filename, Context context) {
		fillIcon(imageIcon, new AQuery(v), filename, context);
	}

	public static void fillIcon(int imageIcon, AQuery aq, String filename, Context context) {
		String hash = getIconHash(context, filename);

		if (TextUtils.isEmpty(hash) || !hash.equals(aq.id(imageIcon).getTag())) {
			BitmapAjaxCallback callback = new BitmapAjaxCallback();
			File file = getBitmapFile(context, filename);
			callback
			// .animation(android.R.anim.slide_in_left)
			.memCache(true).fallback(R.drawable.dummy_icon).file(file).url(filename);
			aq.id(imageIcon).image(callback).tag(hash);
		}
	}

	public static List<ChatMessageHolder> wrapMessages(Buddy buddy, Account account, List<Message> messages) {
		if (messages == null) {
			return Collections.emptyList();
		}

		List<ChatMessageHolder> messageHolders = new ArrayList<ChatMessageHolder>(messages.size());

		for (Message m : messages) {
			messageHolders.add(message2MessageHolder(m, buddy, account));
		}

		return messageHolders;
	}

	public static void removeAccountIcons(Account account, Context context) {
		if (account == null)
			return;

		for (Buddy buddy : account.getBuddyList()) {
			context.deleteFile(buddy.getFilename() + BUDDYICON_FILEEXT);
		}

		context.deleteFile(account.getFilename() + BUDDYICON_FILEEXT);
	}

	public static boolean isSmileyReadOnly(String smiley) {
		return smiley != null && smiley.startsWith("! ");
	}

	public static String escapeOmittableSmiley(String smiley) {
		return isSmileyReadOnly(smiley) ? smiley.substring(2) : smiley;
	}

	public static void insertToEditor(String text, EditText editor) {
		if (editor == null || text == null)
			return;

		int start = Math.max(editor.getSelectionStart(), 0);
		int end = Math.max(editor.getSelectionEnd(), 0);
		editor.getText().replace(Math.min(start, end), Math.max(start, end), text, 0, text.length());

		editor.setSelection(Math.max(start, end) + text.length());
	}

	public static ChatMessageHolder message2MessageHolder(Message message, Buddy buddy, Account account) {
		String senderName;
		if (message.isIncoming()) {
			if (TextUtils.isEmpty(message.getContactDetail())) {
				senderName = buddy.getSafeName();
			} else {
				if (buddy instanceof MultiChatRoom) {
					Buddy b = ((MultiChatRoom) buddy).findOccupantByUid(message.getContactDetail());
					if (b != null) {
						senderName = b.getSafeName();
					} else {
						senderName = message.getContactDetail();
					}
				} else {
					senderName = buddy.getSafeName();
				}
			}
		} else {
			senderName = account.getSafeName();
		}

		return new ChatMessageHolder(message, senderName);
	}

	public static void contextIndependentURLSpans(Spannable spannable) {
		if (spannable == null) {
			return;
		}

		URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);

		for (URLSpan span : spans) {
			int start = spannable.getSpanStart(span);
			int end = spannable.getSpanEnd(span);

			spannable.removeSpan(span);
			spannable.setSpan(new ContextIndependentURLSpan(span.getURL()), start, end, 0);
		}
	}
}
