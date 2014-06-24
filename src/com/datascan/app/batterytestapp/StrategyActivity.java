package com.datascan.app.batterytestapp;

import java.util.ArrayList;
import java.util.HashMap;

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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is for adding and deleting strategies
 * 
 * @author yue
 * 
 */
public class StrategyActivity extends Activity {

	private static final String TAG = "StrategyActivity";

	private TableRow scanStrategyRow, paramRow;
	private EditText intervalEditText, timeEditText, paramEditText,
			valueEditText;
	private Button addScanStrategyButton, addParamButton, enableAllButton,
			disableAllButton;
	private LinearLayout fillList;
	private MyClickListener myListener;
	private ArrayList<SparseIntArray> strategyLists;
	private boolean initView = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.scan_strategy);

		// enable directly control media volume from hardware key
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		findUI();

	}

	protected void onResume() {
		super.onResume();
		// SharedBox.clean();
		if (initView) {
			refillUI();
			initView = false;
		}
	}

	private void findUI() {

		myListener = new MyClickListener();

		fillList = (LinearLayout) findViewById(R.id.layout_strategy_list);
		scanStrategyRow = (TableRow) findViewById(R.id.scan_strategy_row);
		paramRow = (TableRow) findViewById(R.id.param_row);

		addScanStrategyButton = (Button) findViewById(R.id.button1);
		addScanStrategyButton.setOnClickListener(myListener);

		addParamButton = (Button) findViewById(R.id.button2);
		addParamButton.setOnClickListener(myListener);

		intervalEditText = (EditText) scanStrategyRow
				.findViewById(R.id.interval);
		intervalEditText.setNextFocusDownId(R.id.time);
		timeEditText = (EditText) scanStrategyRow.findViewById(R.id.time);

		paramEditText = (EditText) paramRow.findViewById(R.id.param);
		paramEditText.setNextFocusDownId(R.id.value);
		valueEditText = (EditText) paramRow.findViewById(R.id.value);

		enableAllButton = (Button) findViewById(R.id.button_enable_all);
		enableAllButton.setOnClickListener(myListener);
		disableAllButton = (Button) findViewById(R.id.button_disable_all);
		disableAllButton.setOnClickListener(myListener);

	}

	/**
	 * This method is used to load saved strategy and populate into views It
	 * will load saves. If no saves, then return else, populate them into views
	 */
	private void refillUI() {
		// load saves
		SaveHelper.load();
		strategyLists = SharedBox.getSharedBox().getScanStrategyLists();

		// check whether saves are empty
		int size = strategyLists.size();
		if (size == 0) {
			return;
		}

		// start to populate data
		for (SparseIntArray intArray : strategyLists) {
			int interval = intArray.get(SharedBox.TAG_SCAN_INTERVAL, -1);
			int timesCounter = intArray.get(SharedBox.TAG_SCAN_TIMES, -1);
			int param = intArray.get(SharedBox.TAG_SCAN_PARAM, -1);
			int value = intArray.get(SharedBox.TAG_SCAN_VALUE, -1);
			int enableAll = intArray.get(SharedBox.TAG_SCAN_ENABLE_ALL, -1);

			// scan strategy
			if (interval != -1) {
				addScan("" + interval, "" + timesCounter, intArray);
				continue;
			}

			// set param strategy
			if (param != -1) {
				addParam("" + param, "" + value, intArray);
				continue;
			}

			// enable, disable, reset strategy
			if (enableAll != -1) {
				if (enableAll == SharedBox.DISABLE_ALL) {
					enableAll(false, intArray);
				}
				if (enableAll == SharedBox.ENABLE_ALL) {
					enableAll(true, intArray);
				}
				continue;
			}
		}
	}

	/**
	 * This method add scan strategy and shown to user
	 * 
	 * @param interval
	 *            the scan interval
	 * @param times
	 *            count in this scan strategy
	 * @param array
	 *            array stored the saved data previously. Should be null if it's
	 *            a new insertion
	 */
	private void addScan(String interval, String times, SparseIntArray array) {
		// check if these strings are illegal
		if (!checkEditText(interval, times))
			return;

		// save to array list in sharedbox
		if (array == null) {
			array = new SparseIntArray();
			array.append(SharedBox.TAG_SCAN_INTERVAL,
					Integer.parseInt(interval));
			array.append(SharedBox.TAG_SCAN_TIMES, Integer.parseInt(times));
			strategyLists.add(array);
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
				strategyLists.remove(index);
				fillList.removeView(itemRow);
			}
		});

		// show to user
		String outText = getString(R.string.strategy_text, times, interval);
		TextView text = (TextView) itemRow.findViewById(R.id.textView1);
		text.setText(outText);

		// add to views
		fillList.addView(itemRow);
	}


	/**
	 * This method add set param strategy and shown to user
	 * 
	 * @param param
	 *            The param number that is wanted to be set
	 * @param value
	 *            The value that is to be set
	 * @param array
	 *            array stored the saved data previously. Should be null if it's
	 *            a new insertion
	 */
	private void addParam(String param, String value, SparseIntArray array) {
		// check if these strings are illegal
		if (!checkEditText(param, value))
			return;

		// save to array list in sharedbox
		if (array == null) {
			array = new SparseIntArray();
			array.append(SharedBox.TAG_SCAN_PARAM, Integer.parseInt(param));
			array.append(SharedBox.TAG_SCAN_VALUE, Integer.parseInt(value));
			strategyLists.add(array);
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
				SparseIntArray index = (SparseIntArray) v.getTag();
				strategyLists.remove(index);
				fillList.removeView(itemRow);
			}
		});

		// add to views
		String outText = getString(R.string.set_param, param, value);
		TextView text = (TextView) itemRow.findViewById(R.id.textView1);
		text.setText(outText);

		fillList.addView(itemRow);
	}

	/**
	 * In this method, save data has to be done before finish activity
	 */
	@Override
	public void finish() {
		// save strategy to sharedbox
		SharedBox.getSharedBox().setScanStrategyLists(strategyLists);

		// save to storage
		SaveHelper.save();

		// end this activity
		super.finish();
	}

	/**
	 * This method add enable all/ disable all strategy and shown to user
	 * 
	 * @param flag
	 * @param array
	 */
	private void enableAll(boolean flag, SparseIntArray array) {
		SharedBox.getSharedBox().setFlag_scan_enable_all(flag);

		// store
		if (array == null) {
			array = new SparseIntArray();
			array.append(SharedBox.TAG_SCAN_ENABLE_ALL,
					flag ? SharedBox.ENABLE_ALL : SharedBox.DISABLE_ALL);
			strategyLists.add(array);
		}

		// add to view
		final LinearLayout itemRow = (LinearLayout) getLayoutInflater()
				.inflate(R.layout.strategy_item, null);
		Button delButton = (Button) itemRow.findViewById(R.id.delete);

		// save array as tag for convenience of deletion
		delButton.setTag(array);
		delButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SparseIntArray index = (SparseIntArray) v.getTag();
				strategyLists.remove(index);
				fillList.removeView(itemRow);
			}
		});

		String outText = getString(flag ? R.string.enable_all
				: R.string.disable_all);
		TextView text = (TextView) itemRow.findViewById(R.id.textView1);
		text.setText(outText);

		fillList.addView(itemRow);
	}

	/**
	 * This method helps to check the given strings are legal to show to user
	 * It's illegal if string is empty, null or contains non-digits
	 * 
	 * @param t1
	 *            first string in strategy
	 * @param t2
	 *            second string in strategy
	 * @return true if both strings are legal, otherwise return false
	 */
	private boolean checkEditText(String t1, String t2) {
		boolean flag = true;
		if (TextUtils.isEmpty(t1) || TextUtils.isEmpty(t2)) {
			Toast.makeText(this, R.string.edittext_empty, Toast.LENGTH_SHORT)
					.show();
			flag = false;
		}
		if (!TextUtils.isDigitsOnly(t1) || !TextUtils.isDigitsOnly(t2)) {
			Toast.makeText(this, R.string.edittext_not_digit,
					Toast.LENGTH_SHORT).show();
			flag = false;
		}
		return flag;
	}

	/**
	 * This inner class is a handler listening to "add button" in each line of
	 * strategy Click on the button will insert a new line to strategy
	 * 
	 * @author yue
	 * 
	 */
	private class MyClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.button1:
				String interval = intervalEditText.getText().toString();
				String count = timeEditText.getText().toString();
				addScan(interval, count, null);
				break;
			case R.id.button2:
				String param = paramEditText.getText().toString();
				String value = valueEditText.getText().toString();
				addParam(param, value, null);
				break;
			case R.id.button_enable_all:
				enableAll(true, null);
				break;
			case R.id.button_disable_all:
				enableAll(false, null);
				break;
			}
		}
	}
}
