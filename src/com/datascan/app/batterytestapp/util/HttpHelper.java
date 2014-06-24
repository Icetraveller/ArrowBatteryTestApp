package com.datascan.app.batterytestapp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.coppermobile.framework.http.AndroidHttpClient;
import com.coppermobile.framework.http.HttpParameters;
import com.coppermobile.framework.model.IModel;
import com.coppermobile.framework.model.Request;
import com.coppermobile.framework.model.Response;
import com.coppermobile.framework.utils.AppConstants;
import com.coppermobile.framework.utils.AppUtil;
import com.google.gson.Gson;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class HttpHelper {

	private static final String DOWNLOAD_PATH = "https://testapi.datascan.com/api/scannerConfig?storeNumber=70003&scannerConfigVersion=1.2.0.1&scannerTime=2014-03-04T16%3A09%3A56.0000153-06%3A00&scannerSkuVersion=1.2.0.1&countTypeConfigurationId=1&scannerLanguageVersion=1&scannerValidationJSVersion=1&scannerAppVersion=1.7.2.1.2_prod&employeeNumber=1212121212121212&scannerId=0024e00a0f70&scannerSSIDVersion=1";
	private static final String UPLOAD_PATH = "https://github.com/Icetraveller/Arrow-Battery-Test-App/blob/master/project.properties";

	private static final String TAG = "HttpHelper";

	private Context context;

	private Thread caller;

	public HttpHelper(Context context, Thread caller) {
		this.context = context;
		this.caller = caller;
	}

	private boolean WORKING = true;
	private boolean INTERRUPT = false;
	private long sleepTime = 0;

	public void download() {
		try {
			new DownloadTask().execute(new URL(DOWNLOAD_PATH));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void upload() {
		try {
			new UploadTask().execute(new URL(UPLOAD_PATH));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void pause() {
		WORKING = false;
	}

	public void stop() {
		INTERRUPT = true;
	}

	public void setSleepTime(long time) {
		sleepTime = time;
	}

	private class DownloadTask extends AsyncTask<URL, Integer, String> {
		protected String doInBackground(URL... urls) {
			int count = urls.length;
			for (int i = 0; i < count; i++) {
				download(urls[i]);
				publishProgress((int) ((i / (float) count) * 100));
				// Escape early if cancel() is called
				if (isCancelled())
					break;
			}
			return null;
		}

		protected void onPostExecute(String result) {

			if (result != null) {
				Toast.makeText(context, "Download error: " + result,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT)
						.show();
			}
			notiftCaller();
		}
	}

	private class UploadTask extends AsyncTask<URL, Integer, String> {
		@Override
		protected String doInBackground(URL... urls) {
			int count = urls.length;
			for (int i = 0; i < count; i++) {
				upload(urls[i]);
				publishProgress((int) ((i / (float) count) * 100));
				// Escape early if cancel() is called
				if (isCancelled())
					break;
			}
			return null;
		}

		protected void onPostExecute(String result) {

			if (result != null) {
				Toast.makeText(context, "Upload error: " + result,
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, "File Uploaded", Toast.LENGTH_SHORT)
						.show();
			}
			notiftCaller();
		}

	}

	public void notiftCaller() {
		synchronized (caller) {
			caller.notify();
		}
	}

	private boolean isCancelled() {
		return false;
	}

	private String upload(URL urls) {
		Request req = new Request(AppConstants.BASE_URL + "/api/fixture");
		HttpParameters hp = new HttpParameters();
		AppUtil.setHeaderInRequest(hp);
		req.setParams(hp);
		
		AssetManager assetManager = context.getAssets();
		InputStream ims = null;
		StringBuilder sb=new StringBuilder();
	    InputStream in = null;
		try {
			in = context.getAssets().open("JSON.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    BufferedReader br=
	        new BufferedReader(new InputStreamReader(in));
	    String str;
	    try {
			while ((str=br.readLine()) != null) {
			  sb.append(str);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		 String body=getJSONViaGSON(sb.toString());
		 req.setPostBody(body);
		 req.setPost(true);

		AndroidHttpClient client = new AndroidHttpClient();
		client.sendPostRequest(req.getUrl() ,sb.toString(), req.getParams());
		return null;
	}

	public String getJSONViaGSON(String jsonbody) {
		Gson gson = new Gson();
		String jsonRepresentation = gson.toJson(jsonbody);
		return jsonRepresentation;
	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

	public static String getStringFromFile(String filePath) throws Exception {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		// Make sure you close all streams.
		fin.close();
		return ret;
	}

	private String download(URL urls) {
		Request req = new Request(AppConstants.BASE_URL + "/api/fixture");
		HttpParameters hp = new HttpParameters();
		AppUtil.setHeaderInRequest(hp);
		req.setParams(hp);
		
		AndroidHttpClient client = new AndroidHttpClient();
		client.sendGetRequest(urls.toString(), req.getParams());
		return null;
	}

}
