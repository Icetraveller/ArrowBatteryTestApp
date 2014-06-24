package com.datascan.app.batterytestapp.util;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;

/**
 * This class provide all necessary methods to manipulate GPS
 * @author yue
 *
 */
public class GPSHelper {

	private Context context;
	private LocationManager gpsManager;
	
	/**
	 * Constructor
	 * @param context caller's activity
	 */
	public GPSHelper(Context context) {
		this.context = context;
		
		//get gps manager service
		getGPSManager();
	}
	
	/**
	 * get gps manager service
	 */
	private void getGPSManager(){
		gpsManager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
	}
	
	
	public boolean getGPSStatus(){
		return gpsManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ;
	}
	
	public void setGPS(boolean enabled){
		if(enabled){
			turnGPSOn();
		}else{
			turnGPSOff();
		}
	}
	
	/**
	 * Since we can not directly manipulate GPS, we send data to settings activity to do work
	 */
	private void turnGPSOn()
	{
	     Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
	     intent.putExtra("enabled", true);
	     this.context.sendBroadcast(intent);

	    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	    if(!provider.contains("gps")){ //if gps is disabled
	        final Intent poke = new Intent();
	        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"); 
	        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	        poke.setData(Uri.parse("3")); 
	        this.context.sendBroadcast(poke);


	    }
	}
	
	/**
	 * Since we can not directly manipulate GPS, we send data to settings activity to do work
	 */
	private void turnGPSOff()
	{
	    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	    if(provider.contains("gps")){ //if gps is enabled
	        final Intent poke = new Intent();
	        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
	        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
	        poke.setData(Uri.parse("3")); 
	        this.context.sendBroadcast(poke);
	    }
	}

}
