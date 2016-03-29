package ru.cubly.aceim.api.dataentity;

import ru.cubly.aceim.api.utils.Utils;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Abstract message entity.
 * 
 * @author SergiyP
 *
 */
public abstract class BaseMessage extends Entity implements Parcelable {
	/**
	 * BaseMessage text.
	 */
	private String text;
	
	/**
	 * Contact's protocol UID (sender or recipient).
	 */
	private final String contactUid;	
	
	/**
	 * Chat participant UID.
	 */
	private String contactDetail;
	
	/**
	 * Sending/receiving time.
	 */
	private long time;	
	
	/**
	 * BaseMessage ID.
	 */
	private long messageId;	
	
	/**
	 * If true, message is incoming, outgoing otherwise.
	 */
	private boolean isIncoming = true;

	protected BaseMessage(Parcel in) {
		super(in);
		text = in.readString();
		contactUid = in.readString();
		contactDetail = in.readString();
		messageId = in.readLong();
		time = in.readLong();
		isIncoming = in.readByte() > 0;	
	}
	
	protected BaseMessage(byte serviceId, String contactUid) {
		super(serviceId);
		this.contactUid = contactUid;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		
		dest.writeString(text);
		dest.writeString(contactUid);
		dest.writeString(contactDetail);
		dest.writeLong(messageId);
		dest.writeLong(time);
		dest.writeByte((byte) (isIncoming ? 1 : 0));
	}
	
	public static final Parcelable.Creator<BaseMessage> CREATOR = new Parcelable.Creator<BaseMessage>() {
		public BaseMessage createFromParcel(Parcel in) {
			return Utils.unparcelEntity(in, BaseMessage.class);
		}

		public BaseMessage[] newArray(int size) {
			return new BaseMessage[size];
		}
	};

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the messageId
	 */
	public long getMessageId() {
		return messageId;
	}

	/**
	 * @param messageId the messageId to set
	 */
	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	/**
	 * @return the isIncoming
	 */
	public boolean isIncoming() {
		return isIncoming;
	}

	/**
	 * @param isIncoming the isIncoming to set
	 */
	public void setIncoming(boolean isIncoming) {
		this.isIncoming = isIncoming;
	}

	/**
	 * @return the contactUid
	 */
	public String getContactUid() {
		return contactUid;
	}

	/**
	 * @return the contactName
	 */
	public String getContactDetail() {
		return contactDetail;
	}

	/**
	 * @param contactDetail the contactName to set
	 */
	public void setContactDetail(String contactDetail) {
		this.contactDetail = contactDetail;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " from " + contactUid + " from " + getServiceId(); 
	}
}
