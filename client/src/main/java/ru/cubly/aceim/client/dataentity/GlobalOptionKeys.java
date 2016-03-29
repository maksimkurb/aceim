package ru.cubly.aceim.client.dataentity;

import ru.cubly.aceim.client.Constants.OptionKey;
import android.os.Parcel;
import android.os.Parcelable;

public enum GlobalOptionKeys implements OptionKey, Parcelable {
	SCREEN_TYPE,
	MASTER_PASSWORD,
	SMILEYS_IN_DIALOG,
	TEXT_SMILEYS,
	SCREEN_ORIENTATION,
	SOUND_NOTIFICATION_TYPE,
	SOUND_NOTIFICATION_VOLUME,
	LED_BLINKER,
	MESSAGE_SOUND_ONLY,
	STATUSBAR_NOTIFICATION_TYPE,
	FORCE_DRAW_WALLPAPER,
	AUTOCONNECT,
	RECONNECTION_ATTEMPTS,
	LOG_TO_FILE,
	THEME;

	@Override
	public String getStringKey() {
		return toString();
	}

	@Override
	public OptionKey fromStringKey(String key) {
		return GlobalOptionKeys.valueOf(key);
	}	

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(name());
	}

	public static final Parcelable.Creator<GlobalOptionKeys> CREATOR = new Parcelable.Creator<GlobalOptionKeys>() {
		public GlobalOptionKeys createFromParcel(Parcel in) {
			return GlobalOptionKeys.valueOf(in.readString());
		}

		public GlobalOptionKeys[] newArray(int size) {
			return new GlobalOptionKeys[size];
		}
	};
}
