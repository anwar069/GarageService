package com.garageservice.app.receiver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.garageservice.app.R;
import com.garageservice.app.search.SearchModel;

public class CallReceiver extends PhonecallReceiver {
    private WindowManager wm;
    private static LinearLayout ly1;
    private LayoutParams params1;
    Boolean blockCall;
    SharedPreferences getPrefs;
    private String file = "locdata";
    SharedPreferences preferences;
    Context acContext;
    TextView tvName;

    // CONNECTION_TIMEOUT and READ_TIMEOUT are in milliseconds
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;

    @Override
    protected void onIncomingCallStarted(final Context ctx, String number, Date start) {
        getPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        acContext = ctx;

        blockCall = getPrefs.getBoolean("blockCallPref", false);
        if (blockCall) {
            if (!contactExists(ctx, number)) {
                disconnectCall();
                Toast.makeText(ctx, "Call Blocked", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Toast.makeText(ctx, "Incoming Call Started", Toast.LENGTH_LONG).show();
        Log.i("Call Receiver", "Incoming Call Started");
        showCallerInfo(ctx, number);
        // Execute some code after 2 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                WindowManager wm = (WindowManager) ctx
                        .getSystemService(Context.WINDOW_SERVICE);
                Toast.makeText(ctx, "Removing Info", Toast.LENGTH_LONG).show();

                if (ly1 != null) {
                    wm.removeView(ly1);
                    ly1 = null;
                }
            }
        }, 25000);
    }

    @Override
    protected void onOutgoingCallStarted(final Context ctx, String number, Date start) {
        Toast.makeText(ctx, "Outgoing Call Started", Toast.LENGTH_LONG).show();
        getPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        blockCall = getPrefs.getBoolean("blockCallPref", false);


        Toast.makeText(ctx, "Incoming Call Started", Toast.LENGTH_LONG).show();
        Log.i("Call Receiver", "Incoming Call Started");
        showCallerInfo(ctx, number);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                WindowManager wm = (WindowManager) ctx
                        .getSystemService(Context.WINDOW_SERVICE);
                Toast.makeText(ctx, "Removing Info", Toast.LENGTH_LONG).show();

                if (ly1 != null) {
                    wm.removeView(ly1);
                    ly1 = null;
                }
            }
        }, 25000);
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start,
                                       Date end) {
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        Toast.makeText(ctx, "Incoming Call Ended", Toast.LENGTH_LONG).show();

        if (ly1 != null) {
            wm.removeView(ly1);
            ly1 = null;
        }
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start,
                                       Date end) {
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        Toast.makeText(ctx, "Incoming Call Ended", Toast.LENGTH_LONG).show();

        if (ly1 != null) {
            wm.removeView(ly1);
            ly1 = null;
        }
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        WindowManager wm = (WindowManager) ctx
                .getSystemService(Context.WINDOW_SERVICE);
        Toast.makeText(ctx, "Missed Call ", Toast.LENGTH_LONG).show();
        if (ly1 != null) {
            wm.removeView(ly1);
            ly1 = null;
        }
    }

    public boolean contactExists(Context context, String number) {
        // / number is the phone number
        Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {PhoneLookup._ID,
                PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri,
                mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    public void showCallerInfo(final Context ctx, String number) {
		number=number.replace("+91", "");


        wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        params1 = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.TYPE_TOAST,
                LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

		/*params1.height = 75;
        params1.width = 512;*/
        params1.x = 0;
        params1.y = 0;
        params1.format = PixelFormat.TRANSLUCENT;


        ly1 = new LinearLayout(ctx);
        ly1.setOrientation(LinearLayout.HORIZONTAL);

        LayoutInflater mInflater = LayoutInflater.from(ctx);
        View hiddenInfo = mInflater.inflate(R.layout.dialogue_layout, ly1,
                false);

        final LinearLayout infoLayout = (LinearLayout) hiddenInfo.findViewById(R.id.llInfo);


        tvName=((TextView) hiddenInfo.findViewById(R.id.tvPopName));

        tvName.setText(number);

//		String data = read(ctx);
//		try {
//			JSONObject userInfo = new JSONObject(data);
//
//			JSONObject callerInfo = userInfo.optJSONObject(number);
//
//			if (callerInfo != null) {
//				((TextView) hiddenInfo.findViewById(R.id.tvName))
//						.setText(callerInfo.optString("UNAME"));
//				((TextView) hiddenInfo.findViewById(R.id.tvAddress))
//
//				.setText(callerInfo.optString("ADDRESS"));
//			}
//
//
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}


        //		Bitmap image = BlurBuilder.blur(content);
        infoLayout.setBackgroundResource(R.drawable.face);

        ly1.addView(hiddenInfo);
//		ly1.setBackgroundColor(Color.RED);
        ly1.setOrientation(LinearLayout.VERTICAL);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        int dialogPref = Integer.parseInt(sharedPref.getString("dialog_list", "0"));

//		Toast.makeText(ctx,dialogPref+ "", Toast.LENGTH_SHORT).show();
        switch (dialogPref) {
            case 1:
                params1.gravity = Gravity.TOP;
                break;
            case 0:
                params1.gravity = Gravity.CENTER_VERTICAL;
                break;
            case -1:
                params1.gravity = Gravity.BOTTOM;
                break;

        }

        wm.addView(ly1, params1);

        new AsyncFetch(number).execute();
    }

    public void disconnectCall() {
        try {

            String serviceManagerName = "android.os.ServiceManager";
            String serviceManagerNativeName = "android.os.ServiceManagerNative";
            String telephonyName = "com.android.internal.telephony.ITelephony";
            Class<?> telephonyClass;
            Class<?> telephonyStubClass;
            Class<?> serviceManagerClass;
            Class<?> serviceManagerNativeClass;
            Method telephonyEndCall;
            Object telephonyObject;
            Object serviceManagerObject;
            telephonyClass = Class.forName(telephonyName);
            telephonyStubClass = telephonyClass.getClasses()[0];
            serviceManagerClass = Class.forName(serviceManagerName);
            serviceManagerNativeClass = Class.forName(serviceManagerNativeName);
            Method getService = // getDefaults[29];
                    serviceManagerClass.getMethod("getService", String.class);
            Method tempInterfaceMethod = serviceManagerNativeClass.getMethod(
                    "asInterface", IBinder.class);
            Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            IBinder retbinder = (IBinder) getService.invoke(
                    serviceManagerObject, "phone");
            Method serviceMethod = telephonyStubClass.getMethod("asInterface",
                    IBinder.class);
            telephonyObject = serviceMethod.invoke(null, retbinder);
            telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.invoke(telephonyObject);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Receiver",
                    "FATAL ERROR: could not connect to telephony subsystem");
            Log.e("Receiver", "Exception object: " + e.getCause());
        }
    }

    public String read(Context ctx) {
        try {
            FileInputStream fin = ctx.getApplicationContext().openFileInput(
                    file);
            int c;
            String temp = "";
            while ((c = fin.read()) != -1) {
                temp = temp + Character.toString((char) c);
            }

            return temp;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // Create class AsyncFetch
    private class AsyncFetch extends AsyncTask<String, String, String> {

        //		ProgressDialog pdLoading = new ProgressDialog();
        HttpURLConnection conn;
        //		String BaseNameUrl ="http://api.mobilenumbertracker.com/v1/person/name/";
        String BaseNumberUrl = "http://api.mobilenumbertracker.com/v1/person/mobile/";
        String searchQuery;
        String AuthToken = "A525CKA30B760953CC8018C57C49FDA8";
        URL url = null;

        public AsyncFetch(String searchQuery) {
            this.searchQuery = searchQuery;
        }

        private boolean isValidMobile(String phone) {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
//			pdLoading.setMessage("\tSearching...");
//			pdLoading.setCancelable(false);
//			pdLoading.show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your php file resides
                String searchText = this.searchQuery;

                String encodedText = searchText.replaceAll(" ", "%20").trim();
//                try {
//                    encodedText = URLEncoder.encode(searchText.trim(),"UTF-8");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }

                String UrlString = BaseNumberUrl + encodedText + "/?auth_token=" + AuthToken;

                url = new URL(UrlString);

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                // setDoInput and setDoOutput to true as we send and recieve data
                conn.setDoInput(true);
//                conn.setDoOutput(true);

                // add parameter to our above url
//                Uri.Builder builder = new Uri.Builder().appendQueryParameter("searchQuery", searchQuery);
//                String query = builder.build().getEncodedQuery();

//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//                writer.write(query);
//                writer.flush();
//                writer.close();
//                os.close();
                conn.connect();

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.toString();
            }

            try {

                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {
                    return ("Connection error");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }


        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
//			pdLoading.dismiss();
            List<SearchModel> data = new ArrayList<>();
//            Toast.makeText(SearchActivity.this,result, Toast.LENGTH_LONG).show();
//			pdLoading.dismiss();
            if (result.equals("no rows")) {
                Toast.makeText(acContext, "No Results found for entered query", Toast.LENGTH_LONG).show();
            } else {
                SearchModel searchResult = new SearchModel();
                try {

                    if (isValidMobile(this.searchQuery)) {
                        JSONObject jsonObject = new JSONObject(result);


                        searchResult.setFirstName(jsonObject.getString("FirstName"));
                        searchResult.setLastName(jsonObject.getString("LastName"));
                        searchResult.setEmail(jsonObject.getString("Email"));
                        searchResult.setMobile(jsonObject.getString("Mobile"));
                        searchResult.setPicUrl(jsonObject.getString("PictureUrl"));
                        searchResult.setAddress(jsonObject.getString("Address"));
//                        data.add(searchResult);

                    }

                   tvName.setText(searchResult.getFirstName()+" "+searchResult.getLastName());

                    // Setup and Handover data to recyclerview

                } catch (JSONException e) {
                    // You to understand what actually error is and handle it appropriately
//                    Toast.makeText(acContext, "No result found", Toast.LENGTH_LONG).show();
//                    Toast.makeText(acContext, result, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
