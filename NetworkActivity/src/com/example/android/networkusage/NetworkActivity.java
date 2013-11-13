package com.example.android.networkusage;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.networkusage.StackOverflowXmlParser.Entry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;



public class NetworkActivity extends FragmentActivity implements LocationListener,GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener,OnItemSelectedListener,OnClickListener {
    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    private String URL =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/xml?location=17.441148,78.391069&radius=1000&types=food&sensor=false&key=AIzaSyBeNRBzRlJQy0-L8OlYFTnMcJ9LRb6h_-s";

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;

    // The user's current network preference setting.
    public static String sPref = null;

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkReceiver receiver = new NetworkReceiver();
    
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean mUpdatesRequested = true;
    
    private Spinner spinner1;
    private Button button;
    private String searchText="Shopping Mall";
    private String searchType="shopping_mall";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Register BroadcastReceiver to track connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);
        
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        
        mLocationClient = new LocationClient(this, this, this);
        
        
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.search_options, android.R.layout.simple_spinner_item);
        System.out.println("Count = "+adapter.getCount());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        button = (Button) findViewById(R.id.search);
        spinner1.setOnItemSelectedListener(this);
        button.setOnClickListener(this);
        
    }
    
    public void addListenerOnSpinnerItemSelection() {
    	spinner1 = (Spinner) findViewById(R.id.spinner1);

   	
    	System.out.println("Find spinner");
    	if (spinner1 == null) {
    		System.out.println("Spinner is null");
    	}
    	spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				System.out.println("onItemSelected");
				searchText = spinner1.getSelectedItem().toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				System.out.println("onNothingSelected");
				searchText = "Restaurants";
			}
		});
    }
    
    public void addListenerOnButton() {
    	button = (Button) findViewById(R.id.search);
    	System.out.println("Find button");
    	//searchText = spinner1.getSelectedItem().toString();
    	button.setOnClickListener(
    			new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						System.out.println("onClick");
						if (searchText.equals("Restaurants")){
							searchType="restaurant";	
						}
						else if (searchText.equals("Shopping Mall")){
							searchType="shopping_mall";
						}
						else if (searchText.equals("ATM")){
							searchType="atm";
						}
						else if (searchText.equals("Bank")){
							searchType="bank";
						}
						else if (searchText.equals("Hospital")){
							searchType="hospital";
						}
						loadPage();
						}
				});
    	}

    // Refreshes the display if the network connection and the
    // pref settings allow it.
    @Override
    public void onStart() {
        super.onStart();

        // Gets the user's network preference settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieves a string value for the preferences. The second parameter
        // is the default value to use if a preference value is not found.
        sPref = sharedPrefs.getString("listPref", "Wi-Fi");

        updateConnectedFlags();
        
        mLocationClient.connect();
                
        //addListenerOnSpinnerItemSelection();
        System.out.println("Added listener for spinner");
        
        System.out.println("Added listener for button");

        // Only loads the page if refreshDisplay is true. Otherwise, keeps previous
        // display. For example, if the user has set "Wi-Fi only" in prefs and the
        // device loses its Wi-Fi connection midway through the user using the app,
        // you don't want to refresh the display--this would force the display of
        // an error page instead of stackoverflow.com content.
        if (refreshDisplay) {
            loadPage();
        }
    }
    @Override
    public void onStop() {

        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }
        

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();

        super.onStop();
    }
    
	@Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }
	private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
            LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
        }
    }

	@Override
	public void onConnected(Bundle connectionHint) {
		if (mUpdatesRequested && servicesConnected()) {
            startPeriodicUpdates();
        }
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/xml?location="+Double.toString(latitude)+","+Double.toString(longitude)+"&radius=1000&types="+searchType+"&sensor=false&key=AIzaSyBeNRBzRlJQy0-L8OlYFTnMcJ9LRb6h_-s";
		loadPage();
		System.out.println("latitude = "+latitude);
		System.out.println("longitude = "+longitude);
		
	}
	
	private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        // Google Play services was not available for some reason
        } else {
            return false;
        }
    }

	private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);        
    }
	private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    private void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
        wifiConnected = true;
        mobileConnected = true;
        System.out.println("Change 3");
    }

    // Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
    // This avoids UI lock up. To prevent network operations from
    // causing a delay that results in a poor user experience, always perform
    // network operations on a separate thread from the UI.
    private void loadPage() {
    	
        if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
                || ((sPref.equals(WIFI)) && (wifiConnected))) {
            // AsyncTask subclass
        	
            new DownloadXmlTask().execute(URL);
        } else {
            showErrorPage();
        }
    }

    // Displays an error if the app is unable to load content.
    private void showErrorPage() {
        setContentView(R.layout.main);

        // The specified network connection is not available. Displays error message.
        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.loadData(getResources().getString(R.string.connection_error),
                "text/html", null);
    }

    // Populates the activity's options menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    // Handles the user's menu selection.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.settings:
                Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(settingsActivity);
                return true;
        case R.id.refresh:
                loadPage();
                return true;
        default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    

    // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            setContentView(R.layout.main);
            // Displays the HTML string in the UI via a WebView
            WebView myWebView = (WebView) findViewById(R.id.webview);
            myWebView.loadData(result, "text/html", null);
        }
    }

    // Uploads XML from stackoverflow.com, parses it, and combines it with
    // HTML markup. Returns HTML string.
    private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        StackOverflowXmlParser stackOverflowXmlParser = new StackOverflowXmlParser();
        List<Entry> entries = null;
        String title = null;
        String url = null;
        String summary = null;
        Calendar rightNow = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");
        int count = 1;

        // Checks whether the user set the preference to include summary text
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean pref = sharedPrefs.getBoolean("summaryPref", false);

        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
        htmlString.append("<em>" + getResources().getString(R.string.updated) + " " +
                formatter.format(rightNow.getTime()) + "</em>");

        try {
            stream = downloadUrl(urlString);
            entries = stackOverflowXmlParser.parse(stream);
        // Makes sure that the InputStream is closed after the app is
        // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
        // Each Entry object represents a single post in the XML feed.
        // This section processes the entries list to combine each entry with HTML markup.
        // Each entry is displayed in the UI as a link that optionally includes
        // a text summary.
        for (Entry entry : entries) {
        	
        	String photo = "";
        	String video = "video";
            htmlString.append("<p><a href='");
            photo = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+entry.link+"&sensor=true&key=AIzaSyBeNRBzRlJQy0-L8OlYFTnMcJ9LRb6h_-s";
                        
            
            htmlString.append(photo);
            htmlString.append("'>" + entry.title + "</a></p>");
            if(count == 1 ||count == 3 ||count == 6 ||count == 8 ||count == 9) {
            	video ="http://192.168.0.16:8010/cloud/my_videos/"+Integer.toString(count);
            	htmlString.append("<a href='"+video+"'> - Video</a>");
            }
            count++;
            
            // If the user set the preference to include summary text,
            // adds it to the display.
            if (pref) {
                htmlString.append(entry.summary);
            }
        }
//        URL url2 = new URL("http://192.168.0.16:8000/cloud/images/");
//        HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
//		
//        //conn2.setDoOutput(true);
//        conn2.setConnectTimeout(10000 /* milliseconds */);
//        conn2.setRequestMethod("GET");
//        conn2.setDoInput(true);
//        conn2.connect();
//        //OutputStream out1 = new BufferedOutputStream(conn2.getOutputStream());
//        
//        
//        InputStream in = new BufferedInputStream(conn2.getInputStream());
//        BufferedReader br = new BufferedReader(new InputStreamReader(in));
//        String test = br.readLine();
//        String output="";
//        while(test != null) {
//        	output = output +test;
//        	test = br.readLine();
//        }
//        output="<html><body><video width=\"320\" height=\"240\" controls><source src=\"movie.mp4\" type=\"video/mp4\"><source src=\"movie.ogg\" type=\"video/ogg\">Your browser does not support the video tag.</video></body></html>";
    
        return htmlString.toString();
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
    	System.out.println("Inside downloadURL");
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }

    /**
     *
     * This BroadcastReceiver intercepts the android.net.ConnectivityManager.CONNECTIVITY_ACTION,
     * which indicates a connection change. It checks whether the type is TYPE_WIFI.
     * If it is, it checks whether Wi-Fi is connected and sets the wifiConnected flag in the
     * main activity accordingly.
     *
     */
    public class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            // Checks the user prefs and the network connection. Based on the result, decides
            // whether
            // to refresh the display or keep the current display.
            // If the userpref is Wi-Fi only, checks to see if the device has a Wi-Fi connection.
            if (WIFI.equals(sPref) && networkInfo != null
                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // If device has its Wi-Fi connection, sets refreshDisplay
                // to true. This causes the display to be refreshed when the user
                // returns to the app.
                refreshDisplay = true;
                Toast.makeText(context, R.string.wifi_connected, Toast.LENGTH_SHORT).show();

                // If the setting is ANY network and there is a network connection
                // (which by process of elimination would be mobile), sets refreshDisplay to true.
            } else if (ANY.equals(sPref) && networkInfo != null) {
                refreshDisplay = true;

                // Otherwise, the app can't download content--either because there is no network
                // connection (mobile or Wi-Fi), or because the pref setting is WIFI, and there
                // is no Wi-Fi connection.
                // Sets refreshDisplay to false.
            } else {
                refreshDisplay = false;
                Toast.makeText(context, R.string.lost_connection, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		searchText = spinner1.getSelectedItem().toString();
		System.out.println("Selected option = "+searchText);
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		System.out.println("Nothing selected");
		searchText ="Restaurants";
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (searchText.equals("Restaurants")){
			searchType="restaurant";	
		}
		else if (searchText.equals("Shopping Mall")){
			searchType="shopping_mall";
		}
		else if (searchText.equals("ATM")){
			searchType="atm";
		}
		else if (searchText.equals("Bank")){
			searchType="bank";
		}
		else if (searchText.equals("Hospital")){
			searchType="hospital";
		}
		loadPage();
		
	}
}
