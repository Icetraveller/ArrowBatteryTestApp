package com.datascan.app.batterytestapp.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * This class is a helper to modify WiFi, BlueTooth, or other network things
 * 
 * @author yue
 * 
 */
public class NetworkHelper {
	private WifiManager wifiManager;
	private BluetoothAdapter bluetoothAdapter;
	private Context context;

	private static final String TAG = "NetworkHelper";

	public NetworkHelper(Context context) {
		this.context = context;
		getWiFiManager();
		getBluetoothAdapter();
	}

	private void getWiFiManager() {
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	private void getBluetoothAdapter() {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	public boolean getWifiStatus() {
		int state = wifiManager.getWifiState();
		switch(state){
			case WifiManager.WIFI_STATE_DISABLED:
			case WifiManager.WIFI_STATE_DISABLING:
				return false;
			case WifiManager.WIFI_STATE_ENABLED:
			case WifiManager.WIFI_STATE_ENABLING:
				return true;
			case WifiManager.WIFI_STATE_UNKNOWN:
				break;
		}
		return false;
	}

	public void setWiFi(boolean enabled) {
		wifiManager.setWifiEnabled(enabled);
	}

	public boolean getBlueToothStatus() {
		return bluetoothAdapter.isEnabled();
	}

	public void setBlueTooth(boolean enabled) {
		if (enabled)
			bluetoothAdapter.enable();
		else
			bluetoothAdapter.disable();
	}

	public boolean isWifiConnected() {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}

	public void connectToWifi() {
		if (isWifiConnected())
			return;
		wifiManager.startScan();
		Log.d(TAG, "Scanning");
	}

}
