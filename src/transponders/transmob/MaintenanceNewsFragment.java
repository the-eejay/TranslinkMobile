package transponders.transmob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import java.util.Calendar;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MaintenanceNewsFragment extends Fragment {

	//private final String url = "http://jp.translink.com.au/travel-information/service-updates/rss";
	private final String url = "http://translink.com.au/travel-information/service-notices/rss";
	private final String FILENAME = "MaintenanceNews.xml";
	private final String TITLE = "Maintenance News";
	final Calendar calendar = Calendar.getInstance();
	
	TableLayout newsTable;
	Context tableContext;
	TextView newsDate;
	FragmentActivity parent;
	DisplayMetrics scale;
	
	ActionBarActivity activity;
	
	// For testing purposes
	private StringBuilder allTitles = new StringBuilder();
	private StringBuilder allURLs = new StringBuilder();
	private DownloadWebpageTask downloadTask = null;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
		
		activity = (ActionBarActivity) getActivity();
		
        // Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.maintenance_news, container, false);
		newsTable = (TableLayout) view.findViewById(R.id.newsTable);
		tableContext = newsTable.getContext();
		newsDate = (TextView) view.findViewById(R.id.newsDate);
		scale = activity.getResources().getDisplayMetrics();
		
		newsDate.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}	
		});
		
		activity.setProgressBarIndeterminateVisibility(true);
		
		checkConnection();
        return view;
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
		activity.getSupportActionBar().setTitle(TITLE);
	}
	
	public void checkConnection() 
    {
		parent = activity;
		
        ConnectivityManager connMgr = (ConnectivityManager) parent.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        Date date;
        
        if (networkInfo != null && networkInfo.isConnected())
        {
        	downloadTask = new DownloadWebpageTask();
        	downloadTask.execute(url);
        	date = calendar.getTime();
        	newsDate.setText("Last updated: " + date.toString().substring(0, 20));
        	newsDate.setTextSize(18);
        }
        else 
        {
        	Context context = parent.getApplicationContext();
        	try 
        	{
        		File file = new File(context.getFilesDir().getAbsolutePath() + "/" + FILENAME);
        		date = new Date(file.lastModified());
        		newsDate.setText("Last updated: " + date.toString().substring(0, 20));
        		
				readXMLFile(new FileInputStream(file));
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
	
	public class DownloadWebpageTask extends AsyncTask<String, Void, FileInputStream> 
    {
        @Override
        protected FileInputStream doInBackground(String... urls) 
        {       
            // params comes from the execute() call: params[0] is the url.
            try {
            	
                return downloadUrl(urls[0]);
            } catch (IOException e) {
            	
            	e.printStackTrace();
            	//Toast toast = Toast.makeText(parent.getApplicationContext(), "Invalid URL.", Toast.LENGTH_LONG);
    			//toast.show();
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
                Log.d("The URL is: ", "" + url);
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
           
           
        	FileOutputStream fos = parent.openFileOutput(FILENAME, Context.MODE_PRIVATE);
    		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
    		
    		while((line  = buf.readLine()) != null)
            {
    			writer.write(line);
    			writer.newLine();
            }
    		
    		reader.close();
    		writer.close();
    		
    		return parent.openFileInput(FILENAME);
            
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
			Drawable arrow = getResources().getDrawable(R.drawable.mini_arrow_small);
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				
				Node nNode = nList.item(temp);
		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
					
					String category = eElement.getElementsByTagName("category").item(0).getTextContent();
					
					if(category.equalsIgnoreCase("current"))
					{
						String link = eElement.getElementsByTagName("link").item(0).getTextContent();
						String title = eElement.getElementsByTagName("title").item(0).getTextContent();
						
						if(title.length() > 50)
							title = title.substring(0, 47) + "...";
						
						allTitles.append(title);
						allURLs.append(link);
						
						TableRow newRow = new TableRow(tableContext);
		            	Context rowContext = newRow.getContext();
		            	TextView text1 = new TextView(rowContext);
		            	
		            	int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, scale);
		            	
		            	newRow.setMinimumHeight(height);
		            	
		            	TableRow.LayoutParams param1 = new TableRow.LayoutParams();
		                param1.column = 0;
		                text1.setLayoutParams(param1);
		                
		                text1.setText(title);
		                text1.setTextSize(15);
		                text1.setOnClickListener(new NewsListener(link));
		                text1.setPadding(20, 0, 10, 0);
		                text1.setGravity(Gravity.CENTER_VERTICAL);
		                text1.setCompoundDrawablePadding(0);
		    			text1.setCompoundDrawablesWithIntrinsicBounds(null, null, arrow, null);
		    			text1.setMinimumHeight(height);
		    			text1.setBackgroundResource(R.drawable.selector);
		    			
		    			newRow.addView(text1);
	            		newsTable.addView(newRow);
	            		
	            		View separatorLine = new View(tableContext);
	                    separatorLine.setBackgroundColor(getResources().getColor(R.color.separator_line));
	                    separatorLine.setPadding(0, 0, 0, 0);
	                    TableLayout.LayoutParams lineParam = new TableLayout.LayoutParams();
	                    lineParam.height = 2;
	                    separatorLine.setLayoutParams(lineParam);
	                    newsTable.addView(separatorLine);
					}
				}
			}
    	}
    	catch(Exception e)
    	{
    		Toast toast = Toast.makeText(parent.getApplicationContext(), "Exception occured.", Toast.LENGTH_LONG);
			toast.show();
			e.printStackTrace();
    	}
		
		activity.setProgressBarIndeterminateVisibility(false);
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
    		activity.setProgressBarIndeterminateVisibility(true);
    		
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
	
	/*Testing functions */
	public String getAllTitles()
	{
		return allTitles.toString();
	}
	
	public String getAllURLs()
	{
		return allURLs.toString();
	}
	
	public DownloadWebpageTask getDownloadWebpageTask()
	{
		return downloadTask;
	}
	/*End of Testing functions */
}