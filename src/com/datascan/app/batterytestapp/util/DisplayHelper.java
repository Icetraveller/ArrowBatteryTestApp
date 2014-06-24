package com.datascan.app.batterytestapp.util;

import com.datascan.app.batterytestapp.MainActivity;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Window;
import android.view.WindowManager;

/**
 * This class should restore origin brightness when finished
 * 
 * @author yue
 * 
 */
public class DisplayHelper {

	public static final float MAX_BRIGHTNESS = 0.8f;
	public static final float MIN_BRIGHTNESS = 0.1f;

	private float brightness;
	// Window object, that will store a reference to the current window
	private Window window;

	private Context context;

	public DisplayHelper(Context context) {
		this.context = context;
	}

	/**
	 * This class can set brightness of the screen
	 * 
	 * @param f
	 *            between 0 to 1 (min to max)
	 */
	public void setBrightness(boolean flag) {
		if (flag)
			setToMax();
		else
			setToAuto();
	}

	/**
	 * Set display brightness to maximum
	 */
	public void setToMax() {
		//change to manual so that we can modify it
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		window = ((MainActivity) context).getWindow();
		
		int brightnessInt = (int)MAX_BRIGHTNESS*255;
		if(brightnessInt <1) brightnessInt =1;
		Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, brightnessInt); 
		WindowManager.LayoutParams lp = window.getAttributes();
		// store old brightness
		brightness = lp.screenBrightness;
		lp.screenBrightness = MAX_BRIGHTNESS;
		window.setAttributes(lp);
	}
	/**
	 * Set display brightness to minimum
	 */
	public void setToMin() {
		//change to manual so that we can modify it
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		window = ((MainActivity) context).getWindow();
		
		int brightnessInt = (int)MIN_BRIGHTNESS*255;
		if(brightnessInt <1) brightnessInt =1;
		Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, 1); 
		WindowManager.LayoutParams lp = window.getAttributes();
		// store old brightness
		brightness = lp.screenBrightness;
		lp.screenBrightness = MIN_BRIGHTNESS;
		window.setAttributes(lp);
	}

	/**
	 * Change brightness to automatic
	 */
	public void setToAuto() {
		Settings.System.putInt(context.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, (int)brightness*255); 
		window = ((MainActivity) context).getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.screenBrightness = brightness;
		window.setAttributes(lp);
		Settings.System.putInt(context.getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS_MODE,
				Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
	}

	/**
	 * This should be call when exit
	 */
	public void restoreBrightness() {
		window = ((MainActivity) context).getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();

		lp.screenBrightness = brightness;
		window.setAttributes(lp);
	}

	/**
	 * get display status so that switch in option of listview can be updated
	 * @return
	 */
	public boolean getDisplayStatus() {
		try {
			int value = Settings.System.getInt(context.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE);
			if (value == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC)
				return false;
			else
				return true;
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
