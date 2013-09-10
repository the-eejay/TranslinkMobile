package com.example.translinkmobile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MaintenanceNewsFragment extends Fragment {

	private final String FILENAME = "MaintenanceNews.xml";
	TableLayout newsTable;
	Context tableContext;
	private final String TITLE = "Maintenance News";
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.maintenance_news, container, false);
		newsTable = (TableLayout) view.findViewById(R.id.newsTable);
		tableContext = newsTable.getContext();
		checkConnection();
		
        return view;
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
		getActivity().getActionBar().setTitle(TITLE);
	}
	
	public void checkConnection() 
    {
		FragmentActivity parent = getActivity();
		
        ConnectivityManager connMgr = (ConnectivityManager) parent.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        String url = "http://jp.translink.com.au/travel-information/service-updates/rss";
        
        if (networkInfo != null && networkInfo.isConnected())
        {
        	new DownloadWebpageTask().execute(url);
        }
        else 
        {
        	Context context = parent.getApplicationContext();
        	try 
        	{
				readXMLFile(getActivity().openFileInput(FILENAME));
				Toast toast = Toast.makeText(context, "No network connection, showing previous news...", Toast.LENGTH_LONG);
    			toast.show();
			} 
        	catch (FileNotFoundException e) 
        	{
        		Toast toast = Toast.makeText(context, "No connection and no previous news found...", Toast.LENGTH_LONG);
    			toast.show();
			}
        }
    }
	
	private class DownloadWebpageTask extends AsyncTask<String, Void, FileInputStream> 
    {
        @Override
        protected FileInputStream doInBackground(String... urls) 
        {       
            // params comes from the execute() call: params[0] is the url.
            try {
            	
                return downloadUrl(urls[0]);
            } catch (IOException e) {
            	Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Invalid URL.", Toast.LENGTH_LONG);
    			toast.show();
    			return null;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(FileInputStream result) 
        {
        	readXMLFile(result);
        }
        
        private FileInputStream downloadUrl(String myurl) throws IOException 
        {
            InputStream is = null;
                
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
                Log.d("The response is: ", "" + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                FileInputStream fis = readIt(is);
                return fis;
                
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
        
        public FileInputStream readIt(InputStream stream) throws IOException, UnsupportedEncodingException 
        {
        	Reader reader = new InputStreamReader(stream, "UTF-8"); 
        	BufferedReader buf = new BufferedReader(reader);
            String line;            
           
           
        	FileOutputStream fos = getActivity().openFileOutput(FILENAME, Context.MODE_PRIVATE);
    		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
    		
    		while((line  = buf.readLine()) != null)
            {
    			writer.write(line);
    			writer.newLine();
            }
    		
    		reader.close();
    		writer.close();
    		
    		return getActivity().openFileInput(FILENAME);
            
        }
    }
	
	public void readXMLFile(FileInputStream fis)
	{
		try
    	{
        	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fis);
			
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("item");
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				
				Node nNode = nList.item(temp);
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
		 
					String link = eElement.getElementsByTagName("link").item(0).getTextContent();
					String title = eElement.getElementsByTagName("title").item(0).getTextContent();
					
					TableRow newRow = new TableRow(tableContext);
	            	Context rowContext = newRow.getContext();
	            	TextView text1 = new TextView(rowContext);
	            	
	            	TableRow.LayoutParams param1 = new TableRow.LayoutParams();
	                param1.column = 0;
	                param1.span = 5;
	                text1.setLayoutParams(param1);
	                
	                text1.setText(title);
	                text1.setTextSize(14);
	                text1.setOnClickListener(new NewsListener(link));
            		newRow.setPadding(20, 15, 5, 15);
            		newRow.addView(text1);
            		newsTable.addView(newRow);
				}
			}
    	}
    	catch(Exception e)
    	{
    		Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Exception occured.", Toast.LENGTH_LONG);
			toast.show();
    	}
	}
	
	private class NewsListener implements OnClickListener
    {
		String linkURL;
		
		public NewsListener(String link)
		{
			linkURL = link;
		}
		
    	public void onClick(View v) 
		{
    		Fragment fragment2 = new NewsFragment();
    		Bundle args = new Bundle();
            args.putString(NewsFragment.ARG_NEWS_URL, linkURL);
            fragment2.setArguments(args);
    		
    	    FragmentManager fragmentManager = getFragmentManager();
    	    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	    fragmentTransaction.replace(R.id.content_frame, fragment2);
    	    fragmentTransaction.addToBackStack(null);
    	    fragmentTransaction.commit();
		}
    }
}