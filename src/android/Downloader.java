package inet.plugins;

import java.util.Iterator;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.webkit.CookieManager;

public class Downloader extends CordovaPlugin {
	String TAG = "Downloader";
	final String DOWNLOAD = "download";
//	long enqueue = 0;
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (DOWNLOAD.equals(action)) { 
               JSONObject argObject = args.getJSONObject(0);
               if(argObject.getString("url").isEmpty()){
            	   callbackContext.error("Url is null");
            	   return false;
               }
               download(argObject, callbackContext);
               return true;
            }
            callbackContext.error("Invalid action");
            return false;
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
            return false;
        } 
    }
	
	@SuppressLint("NewApi")
	public void download(final JSONObject argObject, final CallbackContext callbackContext) throws JSONException {
		final DownloadManager dm = (DownloadManager) super.cordova.getActivity().getSystemService("download");
        Request request = new Request(Uri.parse(argObject.getString("url")));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        
        // Set cookie for request
        String cookie = CookieManager.getInstance().getCookie(argObject.getString("url"));
        if(cookie != null){
        	request.addRequestHeader("Cookie", cookie);
        }
        // Add custom request headers
        if(argObject.has("headers")){
        	JSONObject json = argObject.getJSONObject("headers");
        	Iterator<String> iter = json.keys();
        	while (iter.hasNext()) {
        	    String key = iter.next();
        	    try {
        	    	request.addRequestHeader(key, (String)json.get(key));
        	    } catch (JSONException e) {
        	    	 System.err.println("Exception add header: " + e.getMessage());
        	        // Something went wrong!
        	    }
        	}
        }
        
        if(argObject.has("title")){
        	request.setTitle(argObject.getString("title"));
        }
        if(argObject.has("description")){
        	request.setTitle(argObject.getString("description"));
        }
        
        final long enqueue = dm.enqueue(request);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    Query query = new Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = c.getInt(columnIndex);
                        int columnReason = c.getColumnIndex(DownloadManager.COLUMN_REASON);
                        int reason = c.getInt(columnReason);
                    	JSONObject json = new JSONObject();
                        try {
							json.put("reason", reason);
							json.put("status", status);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            callbackContext.success(json);
                        }else{
                        	callbackContext.error(json);
                        }
                    }
                }
            }
        };

        super.cordova.getActivity().registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
