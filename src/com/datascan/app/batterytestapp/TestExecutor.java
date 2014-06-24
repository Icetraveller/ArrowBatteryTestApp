package com.datascan.app.batterytestapp;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.TextView;
import android.widget.Toast;

import com.datascan.app.batterytestapp.util.BroadcastCenter;
import com.datascan.app.batterytestapp.util.ScanHelper;
import com.datascan.app.batterytestapp.util.SharedBox;

/**
 * THis class is used to execute the text in an loop The loop is controlled by
 * two param And it will read sharedbox info before every time states changed
 * 
 * @author yue
 * 
 */
public class TestExecutor extends Thread {

	private static final String TAG = "TestExecutor";
	private ScanHelper scanHelper;
	private ArrayList<SparseIntArray> strategyLists;

	private boolean isInterrupted;
	private static boolean SCANNING = true;

	private static final String NEW_ITERATION = "Start new iteration";
	private static final String PARAM_SET = "Param set";
	private static final String DISABLE_ALL = "Disabled All";
	private static final String ENABLE_ALL = "Enabled All";
	private static final String RESET_ALL = "Reset All";

	private boolean flag_scan = false;
	private boolean flag_sound = false;
	private boolean flag_vibrate = false;

	// the following fields are important to control the scan loop
	// cycleCounter is used to count the current position in an iteration
	private int cycleCounter = 0;

	// timesCounter is used to store how many times of scanning in current scan
	// action
	private int timesCounter = -1;

	// current position in one scan action
	private int currentTimes = 0;

	// interval between each scan in current scan action
	private int interval = 0;

	// param of scanner
	private int param = 0;

	// value is used to set to scanner related to the param
	private int value = 0;

	// flag of enable all / disable all function
	private int enableAll = 0;

	// wait to be ready for further scanning
	private static final long PUBLIC_INTERVAL = 500;

	private Context context;
	private TextView tv;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            caller's Activity
	 */
	public TestExecutor(Context context) {
		this.context = context;

		// create instance of ScanHelper, this object will do the specific scan
		// work
		if (scanHelper == null)
			scanHelper = new ScanHelper(context);

		// locate statusbar textview in main activity for display use later
		tv = (TextView) ((MainActivity) context).findViewById(R.id.statusbar);
		readOptions();
	}

	public void pause() {
		// pause decode from now on
		scanHelper.stopDecode();
		SCANNING = false;
//		try{
//			context.unregisterReceiver(r);
//		}catch(Exception e){
////			ignore
//		}
	}

	/**
	 * Read info and flags from sharedbox and reset to initial state
	 */
	private void readOptions() {
		SharedBox sharedBox = SharedBox.getSharedBox();
		flag_scan = sharedBox.isFlag_scan();
		flag_sound = sharedBox.isFlag_sound();
		flag_vibrate = sharedBox.isFlag_vibrate();
		strategyLists = sharedBox.getScanStrategyLists();

		// set param to scanHelper
		scanHelper.setScanFlag(flag_scan);
		scanHelper.setSoundFlag(flag_sound);
		scanHelper.setVibrateFlag(flag_vibrate);

		// reset
		timesCounter = -1;
		currentTimes = 0;
		interval = -1;
		scanHelper.doDefaultParams();
		cycleCounter = 0;
	}

	/**
	 * This method is a loop controlling the scanning work if isInterrupted set
	 * to true, the thread will be killed if SCANNING set to false, thread will
	 * be pause.
	 */
	@Override
	public void run() {
		Log.d(TAG, "inside doScans");
		try {
			while (true) {
				if (isInterrupted)
					break;
				if (SCANNING) {
					// Check if it's current in a scan action
					if (timesCounter == -1) {
						// not in a scan action, read strategy
						if (strategyLists.size() == 0)
							break;
						SparseIntArray array = strategyLists.get(cycleCounter);
						interval = array.get(SharedBox.TAG_SCAN_INTERVAL, -1);
						timesCounter = array.get(SharedBox.TAG_SCAN_TIMES, -1);
						param = array.get(SharedBox.TAG_SCAN_PARAM, -1);
						value = array.get(SharedBox.TAG_SCAN_VALUE, -1);
						enableAll = array
								.get(SharedBox.TAG_SCAN_ENABLE_ALL, -1);
					}

					// go into scan action
					if (interval != -1) {
						takeScanStrategy();
					}

					// set param
					else if (param != -1) {
						takeDoParamStrategy();
						sleep(PUBLIC_INTERVAL);
					}

					// special settings
					else if (enableAll != -1) {
						takeEnableAllStrategy();
						sleep(PUBLIC_INTERVAL);
					}

					// control scan action
					if (timesCounter > 0) {
						// in scan action
						continue;
					}

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
			}
		} catch (Exception e) {// TODO log more
			Log.e(TAG, "here " + e.getMessage());
		} finally {
			// if exception encountered, close scan object
			scanHelper.close();
		}
	}

	/**
	 * This method simply call scan helper to do scan work
	 * 
	 * @throws Exception
	 */
	private void takeScanStrategy() throws Exception {
		// do scan
		doScan();
		final int intervalOut = interval;
		final int currentTimesOut = currentTimes;
		showToStatus("Scan per " + intervalOut + "ms" + " at "
				+ currentTimesOut + " times ");
		// interval
		sleep(interval);
		currentTimes++;
		// if timesCounter is reached, exit this scan action
		if (currentTimes == timesCounter) {
			timesCounter = -1;
			currentTimes = 0;
			interval = -1;
		}

	}

	/**
	 * This method simply call scan helper to set param
	 * 
	 * @throws Exception
	 */
	private void takeDoParamStrategy() throws Exception {
		final boolean flag = scanHelper.setParam(param, value);
		if (flag) {
			makeToast(R.string.param_set_successfully);
		} else {
			makeToast(R.string.param_set_failed);
		}

		makeToast(PARAM_SET);
		param = 0;
		value = 0;
	}

	/**
	 * This method simply call scan helper to do special operation
	 * 
	 * @throws Exception
	 */
	private void takeEnableAllStrategy() throws Exception {
		String toastString = "";
		switch (enableAll) {
		case SharedBox.DISABLE_ALL:
			scanHelper.disableAll();
			toastString = DISABLE_ALL;
			break;
		case SharedBox.ENABLE_ALL:
			scanHelper.enableAll();
			toastString = ENABLE_ALL;
			break;
		case SharedBox.RESET_ALL:
			scanHelper.doDefaultParams();
			toastString = RESET_ALL;
			break;
		}

		makeToast(toastString);
		enableAll = 0;
	}

	/**
	 * This method will do the basic scanning work, if it detects the previous
	 * bcr is not released, it will automatically close.
	 * 
	 * @throws Exception
	 */
	private void doScan() throws Exception {
		try {
			scanHelper.doDecode();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * will stop decode when user press pause button
	 */
	public void pauseScans() {
//		try{
//			context.unregisterReceiver(r);
//		}catch(Exception e){
////			ignore
//		}
		SCANNING = false;
		scanHelper.stopDecode();
	}

	/**
	 * Called to start or restart scanning
	 */
	public void resumeScans() {
		try {
			if (!this.isAlive()) {
				this.start();
			}
		} catch (IllegalThreadStateException e) {
			e.printStackTrace();
		}
		SCANNING = true;
//		context.registerReceiver(r, new IntentFilter(BroadcastCenter.BROADCAST_SLEEP));
	}

	/**
	 * will kill the thread
	 */
	public void interupt() {
		isInterrupted = true;
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
				Toast.makeText(context, toastString, Toast.LENGTH_SHORT).show();
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
				Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Display status in main activity
	 * 
	 * @param statusString
	 */
	private void showToStatus(final String statusString) {
		((MainActivity) context).runOnUiThread(new Runnable() {
			public void run() {
				tv.setText(statusString);
			}
		});
	}

//	private final BroadcastReceiver r = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			Log.d(TAG, "received");
//			pauseScans();
//		}
//	};

}
