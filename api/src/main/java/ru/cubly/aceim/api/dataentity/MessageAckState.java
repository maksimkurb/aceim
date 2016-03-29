package ru.cubly.aceim.api.dataentity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * BaseMessage acknowledgement state.
 */
public enum MessageAckState implements Parcelable {
	/**
	 * BaseMessage reached server.
	 */
	SERVER_ACK,
	
	/**
	 * BaseMessage reached recipient.
	 */
	RECIPIENT_ACK,
	
	/**
	 * Recipient have read the message.
	 */
	READ_ACK;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name());
	}
	
	public static final Creator<MessageAckState> CREATOR = new Creator<MessageAckState>() {
        @Override
        public MessageAckState createFromParcel(final Parcel source) {
            return MessageAckState.valueOf(source.readString());
        }

        @Override
        public MessageAckState[] newArray(final int size) {
            return new MessageAckState[size];
        }
    };
}
