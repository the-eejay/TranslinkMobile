package com.example.serverdownload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class HomeUI extends Activity {
	private static final String DEBUG_TAG = "HttpExample";
	private TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_ui);
        textView = (TextView) findViewById(R.id.textView1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home_ui, menu);
        return true;
    }
    
    public void checkConnection(View view) 
    {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        String url = "http://pataflafla.sg/transponders/dummyschedule.txt";
        
        if (networkInfo != null && networkInfo.isConnected())
        {
        	new DownloadWebpageTask().execute(url);

        }
        else 
        {
        	Context context = getApplicationContext();
			Toast toast = Toast.makeText(context, "No network connection!", Toast.LENGTH_LONG);
			toast.show();
        }
    }
    
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> 
    {
        @Override
        protected String doInBackground(String... urls) 
        {
              
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) 
        {
            textView.setText(result);
        }
        
        private String downloadUrl(String myurl) throws IOException 
        {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
                
            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;
                
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            } 
            finally 
            {
                if (is != null) {
                    is.close();
                } 
            }
        }
        
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");        
            //char[] buffer = new char[len];
            //reader.read(buffer);
            
            BufferedReader buf = new BufferedReader(reader);
            String line;            
            StringBuilder result = new StringBuilder();
            
            while((line  = buf.readLine()) != null)
            {
            	result.append(line + "\n");
            }
            
            //return new String(buffer);
            return result.toString();
        }
    }

}
