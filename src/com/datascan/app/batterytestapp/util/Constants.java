package com.datascan.app.batterytestapp.util;

import java.util.HashMap;

/**
 * This is class contains constants
 * 
 * @author yue
 * 
 */
public class Constants {

	/**
	 * Options enum
	 * 
	 * @author yue
	 * 
	 */
	public enum Options {
		Scan, Network, Display, Sound, GPS, Vibrate, BT, IO
	}

	/**
	 * @return method returns the string array of option names
	 */
	public static String[] optionNames() {
		Options[] options = Options.values();
		String[] names = new String[options.length];

		for (int i = 0; i < options.length; i++) {
			names[i] = options[i].name();
		}
		return names;
	}

	private static final String networkSSID = "DSWireless";
	private static final String networkPass = "backtothefuture2010";
	private static HashMap<String, String> WifiMap = new HashMap<String, String>();
	static {
		WifiMap.put(networkSSID, networkPass);
		//WifiMap.put("Your Network SSID", "Your Password");
	}
	public static HashMap<String, String> getWifiMap(){
		return WifiMap;
	}
	
}
