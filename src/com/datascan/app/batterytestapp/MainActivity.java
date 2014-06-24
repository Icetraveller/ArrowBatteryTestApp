package com.datascan.app.batterytestapp;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.datascan.app.batterytestapp.util.BroadcastCenter;
import com.datascan.app.batterytestapp.util.Constants;
import com.datascan.app.batterytestapp.util.DisplayHelper;
import com.datascan.app.batterytestapp.util.PrefHelper;
import com.datascan.app.batterytestapp.util.SaveHelper;
import com.datascan.app.batterytestapp.util.ScanHelper;
import com.datascan.app.batterytestapp.util.SharedBox;
import com.motorolasolutions.adc.decoder.BarCodeReader;
import com.motorolasolutions.adc.decoder.BarCodeReader.DecodeCallback;

/**
 * This is entry activity of the app. Load saves from internal storage when
 * resume. Most of UI happens in this activity
 * 
 * @author yue
 * 
 */
public class MainActivity extends Activity {

	static {
		System.loadLibrary("IAL");
		System.loadLibrary("SDL");
		System.loadLibrary("barcodereader");
	}

	private final static String TAG = "MainActivity";

	private ListView optionListView;
	private Button controlButton;
	private DSTestListener myListener = new DSTestListener();
	private OptionListAdapter adapter;
	private TestExecutor testExecutor;
	private NetworkExecutor networkExecutor;

	private static final int IDLE = 0;
	private static final int WORK = 1;
	private static final int PAUSE = 2;

	// Key code
	public static final int KEY_BOTTOM_SCAN = 190;
	public static final int KEY_TOP_SCAN = 191;

	// for status
	int status = 0;

	private PowerManager.WakeLock wakeLock;
	private PowerManager.WakeLock sleepLock;

	private SharedBox sharedBox;

	BroadcastReceiver r = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			adapter.notifyDataSetInvalidated();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// enable directly control media volume from hardware key
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		sharedBox = SharedBox.getSharedBox();
		sharedBox.setContext(getApplicationContext());

		// where are my UI? Go find them
		findViews();

		// keep screen on
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| LayoutParams.FLAG_DISMISS_KEYGUARD);
		registerReceiver(sleepReceiver, new IntentFilter(
				BroadcastCenter.BROADCAST_SLEEP));
		registerReceiver(wakeReceiver, new IntentFilter(
				BroadcastCenter.BROADCAST_WAKE));

		final IntentFilter filters = new IntentFilter();
		filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
		registerReceiver(r, filters);

		IntentFilter dimFilters = new IntentFilter();
		dimFilters.addAction(BroadcastCenter.BROADCAST_MAX_BRIGHTNESS);
		dimFilters.addAction(BroadcastCenter.BROADCAST_MIN_BRIGHTNESS);
		registerReceiver(dimReceiver, dimFilters);

		// initialize views, give names and take care of all UI stuff
		initViews();
		// load saves from storage
		SaveHelper.load();
		
	}

	protected void onResume() {
		super.onResume();
		Log.e(TAG, "resume");

		// start scan thread
		if (testExecutor == null) {
			testExecutor = new TestExecutor(MainActivity.this);
		}
		if (networkExecutor == null) {
			networkExecutor = new NetworkExecutor(MainActivity.this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * find views defined in layout files
	 */
	private void findViews() {
		optionListView = (ListView) findViewById(R.id.list_options);
		controlButton = (Button) findViewById(R.id.dummy_button);

	}

	/**
	 * init views setup adapter for listview
	 */
	private void initViews() {
		// listview
		adapter = new OptionListAdapter(this, R.layout.list_item,
				Constants.optionNames());
		optionListView.setAdapter(adapter);

		controlButton.setText(R.string.start);
		controlButton.setOnClickListener(myListener);
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onDestroy() {
		if (sharedBox.isFlag_scan())
			testExecutor.pause();

		if (sharedBox.isFlag_network())
			networkExecutor.pause();
		
		unregisterReceiver(sleepReceiver);
		unregisterReceiver(r);
		unregisterReceiver(dimReceiver);
		pause();
		super.onDestroy();
	}

	/**
	 * This is inner class as handler listen to bottom button. It should have
	 * three status: idle: init status work: scanning status pause: scan paused
	 * 
	 * @author yue
	 * 
	 */
	private class DSTestListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.dummy_button) {
				switch (status) { // original status
				case IDLE:
					work();
					break;
				case WORK:
					pause();
					break;
				case PAUSE:
					work();
					break;
				}
			}
		}
	}

	/**
	 * call this when press the bottom button from status of idle, pause doing
	 * work like change status, text, and update listview, restart thread of
	 * test
	 */
	private void work() {

		// change to new status
		status = WORK;
		controlButton.setText(R.string.pause);

		// disable views in the listview and update data shown
		adapter.setIgnoreDisabled(false);
		adapter.notifyDataSetInvalidated();
		optionListView.setClickable(false);
		startService(new Intent(this, LocalService.class));

		// restart scan thread
		sharedBox = SharedBox.getSharedBox();
		if (sharedBox.isFlag_scan())
			testExecutor.resumeScans();

		if (sharedBox.isFlag_network())
			networkExecutor.resumeExecute();
	}

	/**
	 * call this when press the bottom button from status of work doing work
	 * like change status, text, and update listview, pause thread of test
	 */
	private void pause() {
		// change to new status
		status = PAUSE;
		controlButton.setText(R.string.start);

		// enable views in the listview and update data shown
		adapter.setIgnoreDisabled(true);
		adapter.notifyDataSetInvalidated();
		optionListView.setClickable(true);

		stopService(new Intent(this, LocalService.class));
		// pause scan thread
		if (sharedBox.isFlag_scan())
			testExecutor.pauseScans();

		if (sharedBox.isFlag_network())
			networkExecutor.pause();
	}

	private void sleep() {
		// change to new status
		status = WORK;
		controlButton.setText(R.string.pause);

		// disable views in the listview and update data shown
		adapter.setIgnoreDisabled(false);
		adapter.notifyDataSetInvalidated();
		optionListView.setClickable(false);

		// pause scan thread
		if (sharedBox.isFlag_scan())
			testExecutor.pauseScans();

		if (sharedBox.isFlag_network())
			networkExecutor.pause();
	}

	private final BroadcastReceiver wakeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakelock");
			wakeLock.acquire();
			// wakeLock.release();
			if (status == WORK) {
				work();
			}

		}
	};
	private final BroadcastReceiver sleepReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			int duration = PrefHelper
					.getSleepDurationPref(getApplicationContext());
			if (duration < 0) {
				duration = 0;
			}
			Log.e(TAG, "received, will sleep for" + duration + "ms");
			sleep();
			sleepFor(duration, context);
		}
	};

	private final BroadcastReceiver dimReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(TAG, "dimReceiver");
			String action = intent.getAction();
			final DisplayHelper dh = new DisplayHelper(MainActivity.this);
			if (action.equals(BroadcastCenter.BROADCAST_MAX_BRIGHTNESS)) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
			 			dh.setToMax();
					}
				});

				Log.e(TAG, "set to max");
			} else if (action.equals(BroadcastCenter.BROADCAST_MIN_BRIGHTNESS)) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							dh.setToMin();
						}
					});
					Log.e(TAG, "set to min");
				}
		}

	};

	public void sleepFor(long time, Context context) {
		int CODE_WAKE_UP_DEVICE = 1;
		Intent intent = new Intent(BroadcastCenter.BROADCAST_WAKE);

		PendingIntent wakeupIntent = PendingIntent.getBroadcast(context,
				CODE_WAKE_UP_DEVICE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime() + time, wakeupIntent);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		pm.goToSleep(SystemClock.uptimeMillis() + 1);
	}
}
