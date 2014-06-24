package com.datascan.app.batterytestapp.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseIntArray;

/**
 * This class uses singleton pattern, and is keeping useful options states. Call
 * getSharedBox method and then get flags you care about
 * 
 * @author yue
 * 
 */
public class SharedBox {
	private static SharedBox sharedBox = new SharedBox();

	// storage
	private boolean flag_scan = true;
	private boolean flag_sound = true;
	private boolean flag_network = true;
	private boolean flag_brightness = false;
	private boolean flag_vibrate = false;
	private boolean flag_gps = false;
	private boolean flag_bt = false;
	private boolean flag_scan_enable_all = true;
	private int scans = 0;

	private ArrayList<SparseIntArray> scanStrategyLists = new ArrayList<SparseIntArray>();
	private ArrayList<SparseIntArray> networkStrategyLists = new ArrayList<SparseIntArray>();

	private static final String TAG = "SharedBox";

	public static final int TAG_SCAN_INTERVAL = 1;
	public static final int TAG_SCAN_TIMES = 2;
	public static final int TAG_SCAN_PARAM = 3;
	public static final int TAG_SCAN_VALUE = 4;
	public static final int TAG_SCAN_ENABLE_ALL = 5;// 1 when enable all, 0 when
													// disabled all
	public static final int TAG_NETWORK_DIRECTION = 6; // 1 when upload, 2 when
														// download
	public static final int TAG_NETWORK_TIME_BASED = 7;
	public static final int TAG_NETWORK_SCANS_BASED = 8;

	public static final int WIFI_UPLOAD = 1;
	public static final int WIFI_DOWNLOAD = 2;

	/**
	 * Auto enable Wi-Fi when use and disable it after transaction
	 */
	public static final int TAG_NETWORK_AUTO_WIFI = 10;

	/**
	 * Keep Wi-Fi awake
	 */
	public static final int TAG_NETWORK_KEEP_WIFI = 11;

	public static final int CATEGORY_SCAN = 100;
	public static final int CATEGORY_PARAM = 101;
	public static final int CATEGORY_SETTINGS = 102;
	public static final int CATEGORY_NETWORK_TIME_BASED = 103;
	public static final int CATEGORY_NETWORK_SCANS_BASED = 104;
	public static final int CATEGORY_NETWORK_DIRECTION = 105;
	public static final int STRATEGY_NETWORK = 106;
	public static final int STRATEGY_SCAN = 107;

	public static final int DOWNLOAD_FILE = 108;
	public static final int UPLOAD_FILE = 109;

	public static final int ENABLE_ALL = 1;
	public static final int DISABLE_ALL = 3;
	public static final int RESET_ALL = 2;

	private static Context context;

	private SharedBox() {
	}

	public static void clean() {
		sharedBox = new SharedBox();
	}

	public static SharedBox getSharedBox() {
		if (sharedBox == null) {
			return new SharedBox();
		} else {
			return sharedBox;
		}
	}
	
	public static void setContext(Context mcontext){
		context = mcontext;
	}
	

	public int getScans() {
		return scans;
	}

	public void setScans(int scans) {
		this.scans = scans;
	}

	public void increaseScans() {
		scans++;
		Log.d(TAG, "scans"+scans);
		sleepProcess();
	}

	private void sleepProcess() {
		int sleepInterval = PrefHelper.getSleepIntervalPref(context);
		if (sleepInterval != -1 && scans == sleepInterval) {
			restCounter();
			doBroadcast();
		}
	}

	private void restCounter() {
		scans = 0;
	}

	private void doBroadcast() {
		Log.d(TAG,"scans doBroadcast");
		BroadcastCenter.broadcast(context, BroadcastCenter.SLEEP);
	}

	public boolean isFlag_scan() {
		return flag_scan;
	}

	public void setFlag_scan(boolean flag_scan) {
		this.flag_scan = flag_scan;
	}

	public boolean isFlag_sound() {
		return flag_sound;
	}

	public void setFlag_sound(boolean flag_sound) {
		this.flag_sound = flag_sound;
	}

	public boolean isFlag_network() {
		return flag_network;
	}

	public void setFlag_network(boolean flag_network) {
		this.flag_network = flag_network;
	}

	public boolean isFlag_brightness() {
		return flag_brightness;
	}

	public void setFlag_brightness(boolean flag_brightness) {
		this.flag_brightness = flag_brightness;
	}

	public boolean isFlag_vibrate() {
		return flag_vibrate;
	}

	public void setFlag_vibrate(boolean flag_vibrate) {
		this.flag_vibrate = flag_vibrate;
	}

	public boolean isFlag_gps() {
		return flag_gps;
	}

	public void setFlag_gps(boolean flag_gps) {
		this.flag_gps = flag_gps;
	}

	public boolean isFlag_bt() {
		return flag_bt;
	}

	public void setFlag_bt(boolean flag_bt) {
		this.flag_bt = flag_bt;
	}

	public ArrayList<SparseIntArray> getScanStrategyLists() {
		if (scanStrategyLists == null) {
			return new ArrayList<SparseIntArray>();
		}
		return scanStrategyLists;
	}

	public void setScanStrategyLists(ArrayList<SparseIntArray> strategyLists) {
		this.scanStrategyLists = strategyLists;
	}

	public int getScanStrategySize() {
		if (scanStrategyLists != null) {
			return scanStrategyLists.size();
		} else {
			return -1;
		}
	}

	public boolean isFlag_scan_enable_all() {
		return flag_scan_enable_all;
	}

	public void setFlag_scan_enable_all(boolean flag_scan_enable_all) {
		this.flag_scan_enable_all = flag_scan_enable_all;
	}

	public void setNetworkStrategyLists(
			ArrayList<SparseIntArray> networkStrategyLists) {
		this.networkStrategyLists = networkStrategyLists;
	}

	public ArrayList<SparseIntArray> getNetworkStrategyLists() {
		if (networkStrategyLists == null) {
			return new ArrayList<SparseIntArray>();
		}
		return networkStrategyLists;
	}

	public int getNetworkStrategySize() {
		if (networkStrategyLists != null) {
			return networkStrategyLists.size();
		} else {
			return -1;
		}
	}

}
