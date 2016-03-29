package ru.cubly.aceim.client.dataentity;

import java.lang.reflect.Constructor;

import aceim.api.utils.Logger;
import ru.cubly.aceim.client.AceImException;
import ru.cubly.aceim.client.AceImException.AceImExceptionReason;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

public abstract class PluginResources implements Parcelable {

	private final String packageId;
	private String pluginName;
	private Resources nativeProtocolResources;

	protected PluginResources(String packageName, Resources protocolResources) {
		this.packageId = packageName;
		this.nativeProtocolResources = protocolResources;
	}	

	public Resources getNativeResourcesForProtocol(PackageManager packageManager) throws AceImException {
		if (nativeProtocolResources == null || pluginName == null) {
			if (packageManager != null) {
				ApplicationInfo info;
				try {
					info = packageManager.getApplicationInfo(packageId, 0);
					nativeProtocolResources = packageManager.getResourcesForApplication(info);
					pluginName = nativeProtocolResources.getString(info.labelRes);
				} catch (NameNotFoundException e) {
					throw new AceImException(e, AceImExceptionReason.EXCEPTION);
				}
			} else {
				throw new AceImException(AceImExceptionReason.RESOURCE_NOT_INITIALIZED);
			}
		}
		return nativeProtocolResources;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		// The trick to support inheritance.
		out.writeString(getClass().getName());
		
		out.writeString(packageId);
		out.writeString(pluginName);
	}

	public static final Parcelable.Creator<PluginResources> CREATOR = new Parcelable.Creator<PluginResources>() {
		@SuppressWarnings("unchecked")
		public PluginResources createFromParcel(Parcel in) {
			String className = in.readString();
			
			try {
				Class<? extends PluginResources> cls = (Class<? extends PluginResources>) Class.forName(className);
				Class<?>[] paramTypes = { Parcel.class }; 
				Constructor<? extends PluginResources> constructor = cls.getConstructor(paramTypes);
				return constructor.newInstance(in);
			} catch (Exception e) {
				Logger.log(e);
			}
			
			return null;
		}

		public PluginResources[] newArray(int size) {
			return new PluginResources[size];
		}
	};
	
	protected PluginResources(Parcel in){
		packageId = in.readString();
		pluginName = in.readString();
	}

	/**
	 * @return the packageName
	 */
	public String getPackageId() {
		return packageId;
	}

	@Override
	public String toString() {
		return getPluginName();
	}
	
	public String getInfo(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			Resources r = pm.getResourcesForApplication(getPackageId());
			
			int infoId = r.getIdentifier("info", "string", getPackageId());
			
			if (infoId == 0) {
				Logger.log("No info found for plugin " + getPackageId());
				return null;
			}
			
			return r.getString(infoId);
		} catch (Exception e) {
			Logger.log(e);
		}
		
		return null;
	}

	/**
	 * @return the pluginName
	 */
	public String getPluginName() {
		return pluginName;
	}
	
	protected void setPluginName(String pluginName) {
		if (this.pluginName == null) {
			this.pluginName = pluginName;
		}
	}
}
