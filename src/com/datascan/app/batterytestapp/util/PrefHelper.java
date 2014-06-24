package com.datascan.app.batterytestapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * This class is meant to help set preference for next time use
 * @author yue
 *
 */
public class PrefHelper {
	
	private static final String TAG = "PrefHelper";
	
	public static final String PREF_KEY_WIFI_AUTO_SWITCH = "pref_key_wifi_auto_switch";
	public static final String PREF_SLEEP_DURATION = "sleep_duration_preference";
	public static final String PREF_SLEEP_INTERVAL = "sleep_interval_preference";
	public static final String PREF_SCREEN_DIM_80 = "screen_dim_80";
	public static final String PREF_SCREEN_DIM_0 = "screen_dim_0";
	
	
	public static void setWifiAutoSwitchPref(Context context, boolean enabled){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(PREF_KEY_WIFI_AUTO_SWITCH, enabled);
		editor.commit();
	}
	
	public static boolean getWifiAutoSwitchPref(Context context){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean enabled = sharedPreferences.getBoolean(PREF_KEY_WIFI_AUTO_SWITCH, false);
		return enabled;
	}
	
	
	public static int getSleepDurationPref(Context context){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int sleepDuartion = Integer.parseInt(sharedPreferences.getString(PREF_SLEEP_DURATION, "10"));
		return sleepDuartion*60*1000;
	}
	
	public static int getSleepIntervalPref(Context context){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int sleepInterval = Integer.parseInt(sharedPreferences.getString(PREF_SLEEP_INTERVAL, "1440"));
		return sleepInterval;
	}
	
	public static int getScreenDim80(Context context){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int sleepDuartion = Integer.parseInt(sharedPreferences.getString(PREF_SCREEN_DIM_80, "1"));
		return sleepDuartion*60*1000;
	}
	public static int getScreenDim0(Context context){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		int sleepDuartion = Integer.parseInt(sharedPreferences.getString(PREF_SCREEN_DIM_0, "2"));
		return sleepDuartion*60*1000;
	}
	
	

}
