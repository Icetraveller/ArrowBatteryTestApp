package com.datascan.app.batterytestapp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import com.datascan.app.batterytestapp.R;

import android.content.Context;
import android.util.Log;
import android.util.SparseIntArray;
import android.widget.EditText;

import static com.datascan.app.batterytestapp.util.SharedBox.*;

/**
 * This class provide methods to save and load previous settings in internal
 * storage/batteryTestApp folder
 * 
 * @author yue
 * 
 */
public class SaveHelper {
	private static final String TAG = "SaveHelper";
	private static final String SAVEFILES_PATH = "DSBatteryTest";
	private static final String SAVE_SCAN_STRATEGY_FILES_NAME = "Scan_Strategy";
	private static final String SAVE_NETWORK_STRATEGY_FILES_NAME = "Network_Strategy";
	private static final String SAVE_DOWNLOAD_FILES_NAME = "download_file";
	private static final String SAVE_UPLOAD_FILES_NAME = "upload_file";


	/**
	 * Constructor
	 * 
	 * @param contex
	 *            caller's activity
	 */
	public SaveHelper(Context contex) {
	}

	/**
	 * This method mainly to save strategy files to internal storage. It reads
	 * strategy data from sharedbox and save it to files in storage.
	 */
	public static void save() {
		String scanOutString = "";
		// scan
		if (SharedBox.getSharedBox().getScanStrategySize() <= 0) {
			return;
		} else {
			scanOutString = ScanStrategyListsToString();
			write(SharedBox.STRATEGY_SCAN, scanOutString);
		}

		String networkOutString = "";
		if (SharedBox.getSharedBox().getNetworkStrategySize() <= 0) {
			return;
		} else {
			networkOutString = NetworkStrategyListsToString();
			write(SharedBox.STRATEGY_NETWORK, networkOutString);
		}
	}

	private static void write(int category, String outString) {
		try {
			File f = getFile(category);
			FileOutputStream fOut = new FileOutputStream(f);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(outString);
			myOutWriter.close();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static File getFile(int category){
		File dir = new File(
				android.os.Environment.getExternalStorageDirectory(),
				SAVEFILES_PATH);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File f = null;
		switch (category) {
		case STRATEGY_SCAN:
			f = new File(dir + File.separator
					+ SAVE_SCAN_STRATEGY_FILES_NAME);
			break;
		case STRATEGY_NETWORK:
			f = new File(dir + File.separator
					+ SAVE_NETWORK_STRATEGY_FILES_NAME);
			break;
		case DOWNLOAD_FILE:
			f = new File(dir + File.separator
					+ SAVE_DOWNLOAD_FILES_NAME);
			break;
		case UPLOAD_FILE:
			f = new File(dir + File.separator
					+ SAVE_UPLOAD_FILES_NAME);
			break;
		}
		if (f != null) {
			return f;
		}else{
			return null;
		}
	}

	public static String ScanStrategyListsToString() {
		String nextLineStr = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		ArrayList<SparseIntArray> strategyLists = SharedBox.getSharedBox()
				.getScanStrategyLists();
		for (SparseIntArray intArray : strategyLists) {
			int interval = intArray.get(SharedBox.TAG_SCAN_INTERVAL, -1);
			int timesCounter = intArray.get(SharedBox.TAG_SCAN_TIMES, -1);
			int param = intArray.get(SharedBox.TAG_SCAN_PARAM, -1);
			int value = intArray.get(SharedBox.TAG_SCAN_VALUE, -1);
			int enableAll = intArray.get(SharedBox.TAG_SCAN_ENABLE_ALL, -1);

			if (interval != -1) {
				sb.append(SharedBox.CATEGORY_SCAN).append(" ").append(interval)
						.append(" ").append(timesCounter).append(nextLineStr);
				continue;
			}
			if (param != -1) {
				sb.append(SharedBox.CATEGORY_PARAM).append(" ").append(param)
						.append(" ").append(value).append(nextLineStr);
				continue;
			}
			if (enableAll != -1) {
				sb.append(SharedBox.CATEGORY_SETTINGS).append(" ")
						.append(enableAll).append(nextLineStr);
				continue;
			}

		}
		return sb.toString();
	}

	public static String NetworkStrategyListsToString() {
		String nextLineStr = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		ArrayList<SparseIntArray> strategyLists = SharedBox.getSharedBox()
				.getNetworkStrategyLists();
		for (SparseIntArray intArray : strategyLists) {
			int direction = intArray.get(SharedBox.TAG_NETWORK_DIRECTION, -1);
			int time = intArray.get(SharedBox.TAG_NETWORK_TIME_BASED, -1);
			int scans = intArray.get(SharedBox.TAG_NETWORK_SCANS_BASED, -1);

			if (time != -1) {
				sb.append(SharedBox.CATEGORY_NETWORK_TIME_BASED).append(" ")
						.append(time).append(" ");
			} else if (time != -1) {
				sb.append(SharedBox.CATEGORY_NETWORK_SCANS_BASED).append(" ")
						.append(scans).append(" ");
			}
			if (direction != -1) {
				sb.append(SharedBox.CATEGORY_NETWORK_DIRECTION).append(" ")
						.append(direction).append(nextLineStr);
			}
		}
		return sb.toString();
	}

	/**
	 * This method mainly to load strategy files from internal storage. It reads
	 * strategy data from files and save it to strategy data structure in
	 * sharedbox
	 */
	public static void load() {
		try {
			File dir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					SAVEFILES_PATH);
			if (!dir.exists()) {
				return;
			}
			File scanStrategyFile = new File(dir + File.separator
					+ SAVE_SCAN_STRATEGY_FILES_NAME);
			File networkStrategyFile = new File(dir + File.separator
					+ SAVE_NETWORK_STRATEGY_FILES_NAME);
			SharedBox sharedBox = SharedBox.getSharedBox();
			if (scanStrategyFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(
						scanStrategyFile));
				sharedBox.setScanStrategyLists(arrayLoader(br));
				br.close();
			}
			if (networkStrategyFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(
						networkStrategyFile));
				sharedBox.setNetworkStrategyLists(arrayLoader(br));
				br.close();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Common method to load data on sharedbox
	 * 
	 * @param br
	 * @throws Exception
	 */
	private static ArrayList<SparseIntArray> arrayLoader(BufferedReader br)
			throws IOException {
		ArrayList<SparseIntArray> strategyLists = new ArrayList<SparseIntArray>();
		String line = "";
		while ((line = br.readLine()) != null) {
			SparseIntArray array = new SparseIntArray();
			String[] option1 = line.split("\\s", 2); // option1[0] is category
			String[] option2 = option1[1].split("\\s");
			switch (Integer.parseInt(option1[0])) {
			case SharedBox.CATEGORY_SCAN:
				array.put(SharedBox.TAG_SCAN_INTERVAL,
						Integer.parseInt(option2[0]));
				array.put(SharedBox.TAG_SCAN_TIMES,
						Integer.parseInt(option2[1]));
				break;
			case SharedBox.CATEGORY_PARAM:
				array.put(SharedBox.TAG_SCAN_PARAM,
						Integer.parseInt(option2[0]));
				array.put(SharedBox.TAG_SCAN_VALUE,
						Integer.parseInt(option2[1]));
				break;
			case SharedBox.CATEGORY_SETTINGS:
				array.put(SharedBox.TAG_SCAN_ENABLE_ALL,
						Integer.parseInt(option1[1]));
				break;
			case SharedBox.CATEGORY_NETWORK_TIME_BASED:
				array.put(SharedBox.TAG_NETWORK_TIME_BASED,
						Integer.parseInt(option2[0]));
				array.put(SharedBox.TAG_NETWORK_DIRECTION,
						Integer.parseInt(option2[2]));
				break;
			case SharedBox.CATEGORY_NETWORK_SCANS_BASED:
				array.put(SharedBox.TAG_NETWORK_SCANS_BASED,
						Integer.parseInt(option2[0]));
				array.put(SharedBox.TAG_NETWORK_DIRECTION,
						Integer.parseInt(option2[2]));
				break;
			}
			strategyLists.add(array);
		}

		return strategyLists;
	}

}
