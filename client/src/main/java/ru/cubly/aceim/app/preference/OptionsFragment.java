/**
 * Credits to Fr4gg0r ( http://forum.xda-developers.com/member.php?u=2811098 )
 */
package ru.cubly.aceim.app.preference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import ru.cubly.aceim.api.utils.Logger;
import ru.cubly.aceim.app.R;
import ru.cubly.aceim.app.page.Page;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

abstract class OptionsFragment extends Page implements OnPreferenceChangeListener, OnPreferenceClickListener {
    
	private int xmlId;
    private String sharedPreferenceName;
    
    private PreferenceManager mPreferenceManager;
    
    /**
     * The starting request code given out to preference framework.
     */
    private static final int FIRST_REQUEST_CODE = 100;
    
    private static final int MSG_BIND_PREFERENCES = 0;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                
                case MSG_BIND_PREFERENCES:
                    bindPreferences();
                    break;
            }
        }
    };
    
    public OptionsFragment(int xmlId){
        this(xmlId, null);
    }
    
    public OptionsFragment(int xmlId, String sharedPreferenceName){
        this.xmlId = xmlId;
        this.sharedPreferenceName = sharedPreferenceName;
    }
    
    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle b){
        postBindPreferences();
        View v = inflater.inflate(R.layout.options, null);
        ImageView icon = (ImageView) v.findViewById(R.id.icon);
        TextView title = (TextView) v.findViewById(R.id.title);
        
        icon.setImageDrawable(getIcon(getActivity()));
        title.setText(getTitle(getActivity()));
        
        return v;
    }
    
    @Override
    public void onDestroyView(){
    	mHandler.removeMessages(MSG_BIND_PREFERENCES);
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        if(b != null)
            xmlId = b.getInt("xml");
        mPreferenceManager = onCreatePreferenceManager();
        if (sharedPreferenceName != null)
        	mPreferenceManager.setSharedPreferencesName(sharedPreferenceName);
        addPreferencesFromResource(xmlId);
        postBindPreferences();
        onPreferenceAttached(getPreferenceScreen(), xmlId);
    }

    @Override
    public void onStop(){
        super.onStop();
        try{
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityStop");
            m.setAccessible(true);
            m.invoke(mPreferenceManager);
        }catch(Exception e){
            Logger.log(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try{
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityDestroy");
            m.setAccessible(true);
            m.invoke(mPreferenceManager);
        }catch(Exception e){
            Logger.log(e);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("xml", xmlId);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityResult", int.class, int.class, Intent.class);
            m.setAccessible(true);
            m.invoke(mPreferenceManager, requestCode, resultCode, data);
        }catch(Exception e){
            Logger.log(e);
        }
    }

    /**
     * Posts a message to bind the preferences to the list view.
     * <p>
     * Binding late is preferred as any custom preference types created in
     * {@link #onCreate(Bundle)} are able to have their views recycled.
     */
    private void postBindPreferences() {
        if (mHandler.hasMessages(MSG_BIND_PREFERENCES)) return;
        mHandler.obtainMessage(MSG_BIND_PREFERENCES).sendToTarget();
    }
    
    private void bindPreferences() {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
        	View v = getView();
            preferenceScreen.bind((ListView) v.findViewById(R.id.list));
        }
    }
    
    /**
     * Creates the {@link PreferenceManager}.
     * 
     * @return The {@link PreferenceManager} used by this activity.
     */
    private PreferenceManager onCreatePreferenceManager() {
        try{
            Constructor<PreferenceManager> c = PreferenceManager.class.getDeclaredConstructor(Activity.class, int.class);
            c.setAccessible(true);
            PreferenceManager preferenceManager = c.newInstance(this.getActivity(), FIRST_REQUEST_CODE);
            return preferenceManager;
        }catch(Exception e){
            Logger.log(e);
            return null;
        }
    }
    
    /**
     * Returns the {@link PreferenceManager} used by this activity.
     * @return The {@link PreferenceManager}.
     */
    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    /**
     * Sets the root of the preference hierarchy that this activity is showing.
     * 
     * @param preferenceScreen The root {@link PreferenceScreen} of the preference hierarchy.
     */
    public void setPreferenceScreen(PreferenceScreen preferenceScreen){
        try{
            Method m = PreferenceManager.class.getDeclaredMethod("setPreferences", PreferenceScreen.class);
            m.setAccessible(true);
            boolean result = (Boolean) m.invoke(mPreferenceManager, preferenceScreen);
            if (result && preferenceScreen != null) {
                postBindPreferences();
            }
        }catch(Exception e){
            Logger.log(e);
        }
    }
    
    /**
     * Gets the root of the preference hierarchy that this activity is showing.
     * 
     * @return The {@link PreferenceScreen} that is the root of the preference
     *         hierarchy.
     */
    public PreferenceScreen getPreferenceScreen(){
        try{
            Method m = PreferenceManager.class.getDeclaredMethod("getPreferenceScreen");
            m.setAccessible(true);
            return (PreferenceScreen) m.invoke(mPreferenceManager);
        }catch(Exception e){
            Logger.log(e);
            return null;
        }
    }
    
    /**
     * Adds preferences from activities that match the given {@link Intent}.
     * 
     * @param intent The {@link Intent} to query activities.
     */
    public void addPreferencesFromIntent(Intent intent) {
        throw new RuntimeException("too lazy to include this bs");
    }
    
    /**
     * Inflates the given XML resource and adds the preference hierarchy to the current
     * preference hierarchy.
     * 
     * @param preferencesResId The XML resource ID to inflate.
     */
    public void addPreferencesFromResource(int preferencesResId) {   
        try{
            Method m = PreferenceManager.class.getDeclaredMethod("inflateFromResource", Context.class, int.class, PreferenceScreen.class);
            m.setAccessible(true);
            PreferenceScreen prefScreen = (PreferenceScreen) m.invoke(mPreferenceManager, getActivity(), preferencesResId, getPreferenceScreen());
            setPreferenceScreen(prefScreen);
        } catch(Exception e){
            Logger.log(e);
        }
    }
    
    /**
     * Finds a {@link Preference} based on its key.
     * 
     * @param key The key of the preference to retrieve.
     * @return The {@link Preference} with the key, or null.
     * @see PreferenceGroup#findPreference(CharSequence)
     */
    public Preference findPreference(CharSequence key) {
        if (mPreferenceManager == null) {
            return null;
        }
        return mPreferenceManager.findPreference(key);
    }
    
    abstract void onPreferenceAttached(PreferenceScreen root, int xmlId);
}
