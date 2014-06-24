package com.datascan.app.batterytestapp;

import java.util.ArrayList;

import com.datascan.app.batterytestapp.util.PrefHelper;
import com.datascan.app.batterytestapp.util.SaveHelper;
import com.datascan.app.batterytestapp.util.SharedBox;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

public class NetworkStrategyActivity extends Activity {

	private static final String TAG = "NetworkStrategyActivity";

	private TableRow uploadDownloadRow, timeRow, scansRow;

	private ToggleButton uploadDownloadToggleButton;

	private Button addTimeButton, addScansButton;

	private RadioButton autoRadioButton, keepAwakeRadioButton;
	
	private RadioGroup radioGroup;

	private EditText timeEditText, scansEditText;

	private LinearLayout fillList;

	private ArrayList<SparseIntArray> networkStrategyLists;

	private boolean toggleWifiFlag = true; 
	
	private boolean toggleAutoSwitch = false;
	
	private MyClickListener buttonListner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.network_strategy);

		// enable directly control media volume from hardware key
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		//init listener
		buttonListner = new MyClickListener();

		findUI();
	}

	protected void onResume() {
		super.onResume();
		refillUI();
	}

	private void findUI() {
		uploadDownloadRow = (TableRow) findViewById(R.id.upload_download_row);
		uploadDownloadToggleButton = (ToggleButton) uploadDownloadRow
				.findViewById(R.id.wifi_toggleButton);
		toggleWifiFlag = uploadDownloadToggleButton.isChecked();

		timeRow = (TableRow) findViewById(R.id.auto_wifi_row1);
		timeEditText = (EditText) timeRow.findViewById(R.id.time);
		addTimeButton = (Button) timeRow.findViewById(R.id.button1);
		addTimeButton.setOnClickListener(buttonListner);

		scansRow = (TableRow) findViewById(R.id.auto_wifi_row2);
		scansEditText = (EditText) scansRow.findViewById(R.id.scan);
		addScansButton = (Button) scansRow.findViewById(R.id.button2);
		addScansButton.setOnClickListener(buttonListner);
		
		radioGroup = (RadioGroup) findViewById(R.id.wifi_radioGroup1);
		fillList = (LinearLayout) findViewById(R.id.layout_network_strategy_list);
	}

	/**
	 * This method is used to load saved strategy and populate into views It
	 * will load saves. If no saves, then return else, populate them into views
	 */
	private void refillUI() {
		// load saves
				SaveHelper.load();
				networkStrategyLists = SharedBox.getSharedBox().getNetworkStrategyLists();

				// check whether saves are empty
				int size = networkStrategyLists.size();
				if (size == 0) {
					return;
				}

				// start to populate data
				for (SparseIntArray intArray : networkStrategyLists) {
					int direction = intArray.get(SharedBox.TAG_NETWORK_DIRECTION, -1);
					int time = intArray.get(SharedBox.TAG_NETWORK_TIME_BASED, -1);
					int scans = intArray.get(SharedBox.TAG_NETWORK_SCANS_BASED, -1);
					

					if (time != -1) {
						addTime(""+time, direction,intArray);
					} else if (scans != -1) {
						addScans(""+scans, direction,intArray);
					}
				}
	}

	/**
	 * Handler of toggle button event
	 * 
	 * @param view
	 *            should be uploadDownloadToggleButton.
	 */
	public void onToggleClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();
		if (on) {
			// upload enabled
			toggleWifiFlag = true;
		} else {
			// download enabled
			toggleWifiFlag = false;
		}
	}

	private void addTime(String intervalStr,int direction, SparseIntArray array) {
		// check if these strings are illegal
		if (!checkEditText(intervalStr))
			return;

		int interval = Integer.parseInt(intervalStr);
		String outDirection = getDirectionString(direction);

		// save to array list in sharedbox
		if (array == null) {
			array = new SparseIntArray();
			array.append(SharedBox.TAG_NETWORK_DIRECTION, getDirectionInt());
			array.append(SharedBox.TAG_NETWORK_TIME_BASED, interval);
			networkStrategyLists.add(array);
		}

		// add to view dynamically
		final LinearLayout itemRow = (LinearLayout) getLayoutInflater()
				.inflate(R.layout.strategy_item, null);
		Button delButton = (Button) itemRow.findViewById(R.id.delete);

		// save array as tag for convenience of deletion
		delButton.setTag(array);
		delButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// delete this strategy
				SparseIntArray index = (SparseIntArray) v.getTag();
				networkStrategyLists.remove(index);
				fillList.removeView(itemRow);
			}
		});

		// show to user
		if(TextUtils.isEmpty(outDirection)){
			outDirection = getDirectionString();
		}
		String outText = getString(R.string.based_on_time_text, outDirection, interval);
		TextView text = (TextView) itemRow.findViewById(R.id.textView1);
		text.setText(outText);

		// add to views
		fillList.addView(itemRow);
	}

	private void addScans(String scansStr,int direction, SparseIntArray array) {
		// check if these strings are illegal
		if (!checkEditText(scansStr))
			return;

		int scans = Integer.parseInt(scansStr);
		String outDirection = getDirectionString(direction);

		// save to array list in sharedbox
		if (array == null) {
			array = new SparseIntArray();
			array.append(SharedBox.TAG_NETWORK_DIRECTION, getDirectionInt());
			array.append(SharedBox.TAG_NETWORK_SCANS_BASED, scans);
			networkStrategyLists.add(array);
		}

		// add to view dynamically
		final LinearLayout itemRow = (LinearLayout) getLayoutInflater()
				.inflate(R.layout.strategy_item, null);
		Button delButton = (Button) itemRow.findViewById(R.id.delete);

		// save array as tag for convenience of deletion
		delButton.setTag(array);
		delButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// delete this strategy
				SparseIntArray index = (SparseIntArray) v.getTag();
				networkStrategyLists.remove(index);
				fillList.removeView(itemRow);
			}
		});

		// show to user
		if(TextUtils.isEmpty(outDirection)){
			outDirection = getDirectionString();
		}
		String outText = getString(R.string.based_on_scans_text, outDirection, scans);
		TextView text = (TextView) itemRow.findViewById(R.id.textView1);
		text.setText(outText);

		// add to views
		fillList.addView(itemRow);
	}
	
	/**
	 * In this method, save data has to be done before finish activity
	 */
	@Override
	public void finish() {
		// save strategy to sharedbox
		SharedBox.getSharedBox().setNetworkStrategyLists(networkStrategyLists);

		// save to storage
		SaveHelper.save();
		
		//save pref
		PrefHelper.setWifiAutoSwitchPref(this, toggleAutoSwitch);

		// end this activity
		super.finish();
	}
	
	/**
	 * This inner class is a handler listening to "add button" in each line of strategy
	 * Click on the button will insert a new line to strategy
	 * @author yue
	 *
	 */
	private class MyClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.button1:
				String time = timeEditText.getText().toString();
				addTime(time, -1, null);
				break;
			case R.id.button2:
				String scans = scansEditText.getText().toString();
				addScans(scans, -1, null);
				break;
			}
		}
	}

	/**
	 * check the value is legal and ready for parse to integer
	 * 
	 * @param value
	 *            String value that want to be checked
	 * @return true: the value is good and ready false: value is illegl
	 */
	private boolean checkEditText(String value) {
		if (TextUtils.isEmpty(value) || !TextUtils.isDigitsOnly(value)) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * @return the upload or download string based on toggle button status
	 */
	private String getDirectionString(){
		return toggleWifiFlag?getString(R.string.upload):getString(R.string.download);
	}
	
	private String getDirectionString(int direction){
		if(direction == SharedBox.WIFI_UPLOAD){
			return getString(R.string.upload);
		}else if(direction == SharedBox.WIFI_DOWNLOAD){
			return getString(R.string.download);
		}
		return "";
	}
	
	/**
	 * @return the upload or download int based on toggle button status
	 */
	private int getDirectionInt(){
		return toggleWifiFlag?SharedBox.WIFI_UPLOAD:SharedBox.WIFI_DOWNLOAD;
	}
	
	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.radio0:
	            if (checked)
	                toggleAutoSwitch = true;
	            break;
	        case R.id.radio1:
	            if (checked)
	            	toggleAutoSwitch = false;
	            break;
	    }
	}

}
