package com.datascan.app.batterytestapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.datascan.app.batterytestapp.util.Constants.Options;
import com.datascan.app.batterytestapp.util.GPSHelper;
import com.datascan.app.batterytestapp.util.HttpHelper;
import com.datascan.app.batterytestapp.util.NetworkHelper;
import com.datascan.app.batterytestapp.util.SharedBox;

/**
 * This class is an adapter that will provide customized array adapter to
 * listview
 * 
 * @author yue
 * 
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class OptionListAdapter extends ArrayAdapter<String> {

	private final static String TAG = "OptionListAdapter";

	public String[] names;
	public Context context;
	public int resource;
	private MyCheckListener myCheckListener = new MyCheckListener();
	private MyClickListener myClickListener = new MyClickListener();
	private NetworkHelper networkHelper;
	private GPSHelper gpsHelper;
//	private DisplayHelper displayHelper;


	/**
	 * Contructor
	 * 
	 * @param context
	 *            context of caller activity
	 * @param resource
	 *            layout resource
	 * @param names
	 *            options strings
	 */
	public OptionListAdapter(Context context, int resource, String[] names) {
		super(context, resource, names);
		this.names = names;
		this.context = context;
		this.resource = resource;

		networkHelper = new NetworkHelper(context);
		gpsHelper = new GPSHelper(context);
//		displayHelper = new DisplayHelper(context);
	}

	/**
	 * This inner class holding the items of one line view in the list. It will
	 * help increase performance of listview
	 * 
	 * @author yue
	 * 
	 */
	class ViewHolder {
		ImageView imgIcon;
		TextView title;
		Switch switchWidget;
	}

	/**
	 * Some of them are directly from the preference, some of them should from
	 * preference then set device.
	 * 
	 * @param position
	 * @return
	 */
	private boolean getInitialStats(int position) {
		Options options = Options.valueOf(names[position]);
		switch (options) {
		case BT:
			return networkHelper.getBlueToothStatus();
		case Display:// this is set to false at default
			return false;
		case GPS:
			return gpsHelper.getGPSStatus();
		case IO:
			break;
		case Network:
			return networkHelper.getWifiStatus();
		case Scan:
			return true;
		case Sound:
			return true;
		case Vibrate:
			return false;
		default:
			return false;
		}
		return false;
	}

	// following for disable views when scanning
	private boolean ignoreDisabled = true;

	public void setIgnoreDisabled(boolean ignoreDisabled) {
		this.ignoreDisabled = ignoreDisabled;
	}

	public boolean areAllItemsEnabled() {
		return ignoreDisabled;
	}

	/**
	 * this method will be called when create views, so check if it's scanning
	 */
	public boolean isEnabled(int position) {
		if (areAllItemsEnabled()) {
			return true;
		}
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View row = convertView;
		ViewHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(resource, parent, false);
			row.setId(position);
			row.setOnClickListener(myClickListener);

			holder = new ViewHolder();
			holder.imgIcon = (ImageView) row.findViewById(R.id.icon);
			holder.title = (TextView) row.findViewById(R.id.title);

			holder.switchWidget = (Switch) row.findViewById(R.id.switchWidget);
			holder.switchWidget.setTag(Options.valueOf(names[position]));
			holder.switchWidget.setOnCheckedChangeListener(myCheckListener);
			row.setTag(holder);

			holder.title.setText(names[position]);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		// TODO has to register on wifi changed listener
		boolean initialStats = getInitialStats(position);
		holder.switchWidget.setEnabled(ignoreDisabled);
		holder.switchWidget.setChecked(initialStats);

		return row;

	}

	private class MyClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			if (!ignoreDisabled) {
				return;
			}

			if (Options.values()[view.getId()] == Options.Scan) {
				SharedBox sharedBox = SharedBox.getSharedBox();
				if (!sharedBox.isFlag_scan()) {
					return;
				}
				Intent intent = new Intent(context, StrategyActivity.class);
				context.startActivity(intent);

			} else if (Options.values()[view.getId()] == Options.Network) {
				SharedBox sharedBox = SharedBox.getSharedBox();
				if (!sharedBox.isFlag_network()) {
					return;
				}
				Intent intent = new Intent(context,
						NetworkStrategyActivity.class);
				context.startActivity(intent);
			}
		}

	}

	private class MyCheckListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton view, boolean enabled) {
			Options options = (Options) view.getTag();
			SharedBox sharedBox = SharedBox.getSharedBox();
			switch (options) {
			case Scan:
				sharedBox.setFlag_scan(enabled);
				break;
			case BT:
				networkHelper.setBlueTooth(enabled);
				sharedBox.setFlag_bt(enabled);
				break;
			case Display:
//				displayHelper.setBrightness(enabled);
//				sharedBox.setFlag_brightness(enabled);
				break;
			case GPS:
				gpsHelper.setGPS(enabled);
				sharedBox.setFlag_gps(enabled);
				break;
			case IO:
				break;
			case Network:
				networkHelper.setWiFi(enabled);
				sharedBox.setFlag_network(enabled);
				if (enabled)
					networkHelper.connectToWifi();
				break;
			case Sound:
				sharedBox.setFlag_sound(enabled);
				break;
			case Vibrate:
				sharedBox.setFlag_vibrate(enabled);
				break;
			default:
				break;
			}
		}

	}

}