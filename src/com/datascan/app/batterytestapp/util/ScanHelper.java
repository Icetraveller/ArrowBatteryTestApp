package com.datascan.app.batterytestapp.util;

import com.datascan.app.batterytestapp.MainActivity;
import com.datascan.app.batterytestapp.R;
import com.motorolasolutions.adc.decoder.BarCodeReader;
import com.motorolasolutions.adc.decoder.BarCodeReader.DecodeCallback;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

/**
 * This helper hold one bar code reader object for the whole application
 * 
 * @author yue
 * 
 */
public class ScanHelper implements DecodeCallback {

	private final static String TAG = "ScanHelper";

	// BarCodeReader specifics
	private static BarCodeReader bcr = null;

	// states
	static final int STATE_IDLE = 0;
	static final int STATE_DECODE = 1;
	static final int STATE_HANDSFREE = 2;
	static final int STATE_PREVIEW = 3; // snapshot preview mode
	static final int STATE_SNAPSHOT = 4;
	static final int STATE_VIDEO = 5;

	private int state = STATE_IDLE;

	private boolean isFlag_Scan = true;

	private boolean isFlag_Vibrate = false;
	private SharedBox sharedBox;

	// sound related
	private boolean isFlag_Sound = true;
	private ToneGenerator tg;
	private Vibrator vibrator;
	private static final long VIBRATE_TIME = 500;

	private Context context;
	private TextView tv;

	public ScanHelper(Context ctx) {
		context = ctx;
		sharedBox = SharedBox.getSharedBox();
		// scan
		initReader();
		// sound
		tg = new ToneGenerator(AudioManager.STREAM_MUSIC,
				ToneGenerator.MAX_VOLUME); // TODO add option to set to max or
											// min

		vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
	}

	private void initReader() {
		if (bcr == null) {
			boolean flag = openBcr();
		}
		bcr.setDecodeCallback(this);
		tv = (TextView) ((MainActivity) context).findViewById(R.id.statusbar);

		setParam(136, 15);
	}

	/**
	 * Set flag of scan to see if scanning will be performed
	 * 
	 * @param flag
	 */
	public void setScanFlag(boolean flag) {
		isFlag_Scan = flag;
	}

	/**
	 * Set flag of sound to see if sound will be performed
	 * 
	 * @param flag
	 */
	public void setSoundFlag(boolean flag) {
		isFlag_Sound = flag;
	}

	/**
	 * Set flag of vibrate to see if vibrate will be performed each scanning
	 * 
	 * @param flag
	 */
	public void setVibrateFlag(boolean flag) {
		isFlag_Vibrate = flag;
	}

	/**
	 * used when user press pause button in MainActivity
	 */
	public void stopDecode() {
		if (bcr != null && state == STATE_DECODE) {
			bcr.stopDecode();
			state = STATE_IDLE;
		}
	}

	/**
	 * Do decode
	 */
	public void doDecode() {
		if (!isFlag_Scan) {
			return;
		}

		if (bcr == null) {
			return;
		}

		if (setIdle() != STATE_IDLE)
			return;
		state = STATE_DECODE;
		bcr.startDecode();
	}

	/**
	 * 
	 * @return successful or not
	 */
	private boolean openBcr() {
		try {
			bcr = BarCodeReader.open(1);
			if (bcr == null) {
				Log.e(TAG, "failed to open");
			} else {
				Log.e(TAG, "succeed to open");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void close() {
		if (bcr != null) {
			bcr.release();
		}
	}

	public void beep() {
		if (tg != null)
			tg.startTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING);
	}

	public void vibrate() {
		if (vibrator != null)
			vibrator.vibrate(VIBRATE_TIME);
	}

	public boolean setParam(int paramNum, int paramVal) {
		if (bcr == null)
			return false;
		int returnValue = bcr.setParameter(paramNum, paramVal);
		if (returnValue == bcr.BCR_SUCCESS)
			return true;
		else
			return false;

	}

	/**
	 * This method set scanner to default state
	 */
	public void doDefaultParams() {
		bcr.setDefaultParameters();
		setParam(136, 15);
	}

	public void enableAll() {
		bcr.enableAllCodeTypes();
	}

	public void disableAll() {
		bcr.disableAllCodeTypes();
	}

	@Override
	public void onDecodeComplete(int symbology, int length, byte[] data,
			BarCodeReader reader) {

		if (state == STATE_DECODE)
			state = STATE_IDLE;

		if (length > 0) {
			tv.setText(new String(data));
			sharedBox.increaseScans();
			if (isFlag_Sound)
				beep();
			if (isFlag_Vibrate)
				vibrate();
			Log.e(TAG, "decode successfully");
		} else { // no-decode
			switch (length) {
			case BarCodeReader.DECODE_STATUS_TIMEOUT:
				Log.e(TAG, "decode timed out");
				tv.setText("decode timed out");
				sharedBox.increaseScans();
				break;

			case BarCodeReader.DECODE_STATUS_CANCELED:
				Log.e(TAG, "decode cancelled");
				tv.setText("decode cancelled");
				break;

			case BarCodeReader.DECODE_STATUS_ERROR:
				break;
			case BarCodeReader.DECODE_STATUS_MULTI_DEC_COUNT:
				break;
			default:
				Log.e(TAG, "decode failed" + length);
				tv.setText("failed");
				break;
			}
		}
	}

	@Override
	public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {
		// TODO Auto-generated method stub
	}

	private int setIdle() {
		int prevState = state;
		int ret = prevState; // for states taking time to chg/end

		state = STATE_IDLE;
		switch (prevState) {
		case STATE_DECODE:
			break;
		default:
			ret = STATE_IDLE;
		}
		return ret;
	}

}
