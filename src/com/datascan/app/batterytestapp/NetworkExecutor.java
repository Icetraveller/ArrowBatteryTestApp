package com.datascan.app.batterytestapp;

import java.util.ArrayList;
import static com.datascan.app.batterytestapp.util.SharedBox.*;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.TextView;
import android.widget.Toast;

import com.datascan.app.batterytestapp.util.HttpHelper;
import com.datascan.app.batterytestapp.util.NetworkHelper;
import com.datascan.app.batterytestapp.util.PrefHelper;
import com.datascan.app.batterytestapp.util.ScanHelper;
import com.datascan.app.batterytestapp.util.SharedBox;

public class NetworkExecutor extends Thread {
	private static final String TAG = "NetworkExecutor";
	private Context context;
	private TextView tv;
	private NetworkHelper networkHelper;
	private HttpHelper httpHelper;
	private ArrayList<SparseIntArray> strategyLists;

	// wait to be ready for further scanning
	private static final long PUBLIC_INTERVAL = 500;
	private static final String NEW_ITERATION = "Start new iteration";

	private boolean isInterrupted = false;
	private static boolean WORKING = true;

	private static final int WAITFORWIFI = 5000;

	// cycleCounter is used to count the current position in an iteration
	private int cycleCounter = 0;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            caller's Activity
	 */
	public NetworkExecutor(Context context) {
		this.context = context;

		if (networkHelper == null)
			networkHelper = new NetworkHelper(context);

		// locate statusbar textview in main activity for display use later
		tv = (TextView) ((MainActivity) context).findViewById(R.id.statusbar);
		readOptions();
	}

	public void pause() {
		// pause thread from now on
		WORKING = false;
		this.interrupt();
		if (httpHelper != null)
			httpHelper.notiftCaller();
	}

	public void resumeExecute() {
		try {
			if (!this.isAlive()) {
				this.start();
			}
		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
		}
//		readOptions();
		WORKING = true;
	}

	/**
	 * Read info and flags from sharedbox and reset to initial state
	 */
	private void readOptions() {
		SharedBox sharedBox = SharedBox.getSharedBox();
		strategyLists = sharedBox.getNetworkStrategyLists();

		cycleCounter = 0;
	}

	int direction = -1;
	int time = 0; // based on time
	int scans = 0; // based on scans

	/**
	 * This method is a loop controlling the scanning work if isInterrupted set
	 * to true, the thread will be killed if SCANNING set to false, thread will
	 * be pause.
	 */
	@Override
	public void run() {

		while (true) {
			try {
				if (isInterrupted)
					break;
				if (WORKING) {
					Log.e(TAG, "new iteration");
					if (strategyLists.size() == 0)
						break;
					/*
					 * Read from strategy list
					 */
					SparseIntArray array = strategyLists.get(cycleCounter);

					direction = array.get(TAG_NETWORK_DIRECTION, -1);

					boolean basedOnTime = (time = array.get(
							TAG_NETWORK_TIME_BASED, -1)) != -1 ? true : false;

					boolean basedOnScans = (scans = array.get(
							TAG_NETWORK_SCANS_BASED, -1)) != -1 ? true : false;

					/*
					 * Operation
					 */

					// check if there is an operation
					if (!basedOnScans && !basedOnTime) {
						return;
					}

					long sleepTime = WAITFORWIFI;
					String displayString = "";
					// check If WiFi is enabled
					boolean enabled = networkHelper.getWifiStatus();

					if (!enabled) {
						networkHelper.setWiFi(true);
						networkHelper.connectToWifi();
						makeToast(R.string.connecting_wifi);
						sleep(WAITFORWIFI);
						continue;
					}

					if (basedOnTime) {
						httpHelper = new HttpHelper(context, this);
						if (direction == SharedBox.WIFI_DOWNLOAD) {
							httpHelper.download();
							makeToast("Starting donwloading");
						} else {
							httpHelper.upload();
							makeToast("Starting uploading");
						}
						synchronized (this) {
							Log.e(TAG, "locked");
							wait();
							Log.e(TAG, "wake up");
						}
						sleepTime = time * 1000 * 60;
//						 sleepTime = time*1000; //for test
						displayString = "sleep for " + time + " mins";

					} else if (basedOnScans) {
						makeToast("Starting donwloading every " + scans
								+ " scans");
						sleep(1000);
					}

					// load preference to see if wifi should be turn off
					boolean wifiAutoSwitch = PrefHelper
							.getWifiAutoSwitchPref(context);
					if (wifiAutoSwitch) {
						networkHelper.setWiFi(false);
					}
					Log.e(TAG, "sleep = " + sleepTime);
					makeToast(displayString);
					sleep(sleepTime);
					Log.e(TAG, "sleep over");
					// check if it's reach the end of this iteration
					if (cycleCounter == strategyLists.size() - 1) {

						// reset iteration control counter
						cycleCounter = 0;
						makeToast(NEW_ITERATION);
						// prepare new iteration
						sleep(PUBLIC_INTERVAL);
					} else {
						// iteration not finished yet
						cycleCounter++;
					}
				}
			} catch (InterruptedException e) {
				e.getStackTrace();
				continue;
			}
		}

	}

	/**
	 * Toast in main activity
	 * 
	 * @param toastString
	 *            toast message
	 */
	private void makeToast(final String toastString) {
		((MainActivity) context).runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(context, toastString, Toast.LENGTH_LONG).show();
			}
		});
	}

	/**
	 * Toast in main activity
	 * 
	 * @param res
	 *            resource ID
	 */
	private void makeToast(final int res) {
		((MainActivity) context).runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(context, res, Toast.LENGTH_LONG).show();
			}
		});
	}

}
