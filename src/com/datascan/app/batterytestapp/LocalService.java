/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datascan.app.batterytestapp;

import java.util.Timer;
import java.util.TimerTask;

import com.datascan.app.batterytestapp.util.BroadcastCenter;
import com.datascan.app.batterytestapp.util.PrefHelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application. The
 * {@link LocalServiceActivities.Controller} and
 * {@link LocalServiceActivities.Binding} classes show how to interact with the
 * service.
 * 
 * <p>
 * Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service. This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */

public class LocalService extends Service {
	private Context context = this;

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		LocalService getService() {
			return LocalService.this;
		}
	}

	@Override
	public void onCreate() {
		t.start();
		// Display a notification about us starting. We put an icon in the
		// status bar.
	}

	private boolean interrupt = false;
	private boolean pause = false;
	private Thread t = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				if (interrupt) {
					break;
				}
				if (pause) {
					continue;
				}
				try {
					maxBrightness();
					minBrightness();
				} catch (InterruptedException e) {
					continue;
				}

			}

		}
	});

	private void maxBrightness() throws InterruptedException{

		if (pause | interrupt)
			return;
		Log.e("service", "max");
		BroadcastCenter.broadcast(context, BroadcastCenter.MAX_BRIGHTNESS);
		long time = PrefHelper.getScreenDim80(context);
		Thread.sleep(time);
	}

	private void minBrightness()  throws InterruptedException{
		if (pause | interrupt)
			return;
		Log.e("service", "min");
		BroadcastCenter.broadcast(context, BroadcastCenter.MIN_BRIGHTNESS);
		long time = PrefHelper.getScreenDim0(context);
		Thread.sleep(time);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		pause = false;
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		interrupt = true;
		// Tell the user we stopped.
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

}
