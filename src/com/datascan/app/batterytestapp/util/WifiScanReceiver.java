package com.datascan.app.batterytestapp.util;

import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiScanReceiver extends BroadcastReceiver {
	private WifiManager wifiManager;
	private HashMap<String, String> wifiMap = new HashMap<String, String>();
	
	private Context context;
	private static final String TAG = "WifiScanReceiver";
	
	private void MapWifi() {
		wifiMap = Constants.getWifiMap();
	}
	
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		MapWifi();
		List<ScanResult> wifiList = wifiManager.getScanResults();

		for (ScanResult sr : wifiList) {
			if (sr.SSID == null)
				return;

			String password = wifiMap.get(sr.SSID);
			if (password != null) {
				createAccessPoint(sr.SSID, password);
				break;
			}
		}
		
	}
	
	public boolean isWifiConnected() {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}
	
	public void createAccessPoint(String ssid, String passkey) {
		WifiConfiguration wc = new WifiConfiguration();
		wc.SSID = "\"" + ssid + "\"";
		wc.preSharedKey = "\"" + passkey + "\"";
		wc.status = WifiConfiguration.Status.ENABLED;
	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	    wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	    
	    List<WifiConfiguration> wifiConfList = wifiManager.getConfiguredNetworks();
	    if(!wifiConfList.contains(wc)){
	    	int netId = wifiManager.addNetwork(wc);
	    	wifiManager.enableNetwork(netId, true);
	    	wifiManager.reconnect();
	    }
	    
	}
}
