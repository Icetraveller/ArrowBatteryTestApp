package com.datascan.app.batterytestapp.util;

import android.content.Context;
import android.content.Intent;

public class BroadcastCenter {
	public static final int SLEEP = 1;
	public static final int WAKE = 2;
	public static final int MAX_BRIGHTNESS = 3;
	public static final int MIN_BRIGHTNESS = 4;

	public static final String BROADCAST_SLEEP = "broadcast_sleep";
	public static final String BROADCAST_WAKE = "broadcast_wake";
	public static final String BROADCAST_MAX_BRIGHTNESS = "broadcast_max_brightness";
	public static final String BROADCAST_MIN_BRIGHTNESS = "broadcast_min_brightness";

	public static void broadcast(Context context, int type) {
		Intent intent = null;
		switch (type) {
		case SLEEP: {
			intent = new Intent(BROADCAST_SLEEP);
			break;
		}
		case WAKE: {
			intent = new Intent(BROADCAST_WAKE);
			break;
		}
		case MAX_BRIGHTNESS: {
			intent = new Intent(BROADCAST_MAX_BRIGHTNESS);
			break;
		}
		case MIN_BRIGHTNESS: {
			intent = new Intent(BROADCAST_MIN_BRIGHTNESS);
			break;
		}
		}
		if (intent != null) {
			context.sendBroadcast(intent);
		}

	}

}
