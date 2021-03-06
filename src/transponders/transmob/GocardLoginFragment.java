package transponders.transmob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The Fragment class that displays the input form for the journey planner.
 * This class gets the location IDs from translink's resolve API and sends them to
 * ShowJourneyPage class, a view that displays translink's journey plan page.
 * 
 * @author Transponders
 * @version 1.0
 */
public class GocardLoginFragment extends Fragment implements OnClickListener 
{	
	public final String GOCARD_NUMBER = "GOCARD_NUMBER";
	public final String GOCARD_PASSWORD = "GOCARD_PASSWORD";
	
	// UI elements
	private EditText gcnumText;
	private EditText passwordText;
	private Button loginButton;
	private TextView wrongpassWarning;
	private CheckBox rememberBox;
	private FragmentActivity activity;
	
	private String gcNumber, password;
	
	private static DefaultHttpClient httpClient;
	private HttpThread httpThread;
	
	private CountDownLatch lock;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.gocard_login, container, false);
		
		activity = getActivity();
		
		gcnumText = (EditText) view.findViewById(R.id.gcnum_input);
		passwordText = (EditText) view.findViewById(R.id.password_input);
		wrongpassWarning = (TextView) view.findViewById(R.id.wrongpass_warning);
		rememberBox = (CheckBox) view.findViewById(R.id.check_remember);
		
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);;
		String savedNum = settings.getString(GOCARD_NUMBER, "");
		String savedPassword = settings.getString(GOCARD_PASSWORD, "");
	
		gcnumText.setText(savedNum);
		passwordText.setText(savedPassword);
		
		loginButton = (Button) view.findViewById(R.id.login_button);
		loginButton.setOnClickListener(this);
		
		Button gcnumClear = (Button) view.findViewById(R.id.gcnum_clear_button);
		Button passwordClear = (Button) view.findViewById(R.id.password_clear_button);

		gcnumClear.setOnClickListener(this);
		passwordClear.setOnClickListener(this);
		
		int density = getResources().getDisplayMetrics().densityDpi;
        if(density <= DisplayMetrics.DENSITY_HIGH)
        {
        	loginButton.setMinimumHeight(60);
        }
	
		return view;
	}

	@Override
	public void onClick(View v) 
	{		
		switch(v.getId())
		{
			case R.id.gcnum_clear_button:
				gcnumText.setText("");
				break;
			
			case R.id.password_clear_button:
				passwordText.setText("");
				break;
				
			case R.id.login_button:
				if(isNetworkAvailable())
				{
					gcNumber = gcnumText.getText().toString();
					password = passwordText.getText().toString();
					
					if(rememberBox.isChecked())
					{
						SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);;

						SharedPreferences.Editor editor = settings.edit();
		        	    editor.putString(GOCARD_NUMBER, gcNumber);
		        	    editor.putString(GOCARD_PASSWORD, password);
		        	  
		        	    editor.commit();
					}
					else
					{
						SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);;

						SharedPreferences.Editor editor = settings.edit();
		        	    editor.putString(GOCARD_NUMBER, "");
		        	    editor.putString(GOCARD_PASSWORD, "");
		        	    editor.commit();
					}

					activity.setProgressBarIndeterminateVisibility(true);
					httpThread = new HttpThread();
					String url = "https://gocard.translink.com.au/webtix/welcome/welcome.do";
					httpThread.execute(url);
				}
				else
					Toast.makeText(activity.getApplicationContext(), "No internet connection!", Toast.LENGTH_SHORT).show();
				
				break;
		}
	}
	
	/**
	 * The class to handle https requests
	 * 
	 *
	 */
	private class HttpThread extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String... params) 
		{
			String res = postData(params[0]);
			return res;
		}
		
		@Override
	    protected void onPostExecute(String result) {
	    	Log.d("JSONRequest", "postexecute");
	    	
	    	super.onPostExecute(result);
	    	parseResult(result);
	    	if (lock != null) {
	    		lock.countDown();
	    		
	    	}
	    }	
	}
	
	public String postData(String url) 
	{
	    // Create a new HttpClient and Post Header
	    if(httpClient == null) {
	    	httpClient = createHttpClient();
	    }
	    
	    HttpPost httppost = new HttpPost(url);
	    
	    Log.d("postData()", "inside postData()");
	    String result = "not yet...";

	    try 
	    {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        Log.d( "GoCard", "Number Entered="+ gcNumber );
	        nameValuePairs.add(new BasicNameValuePair("cardNum", gcNumber ));
	        nameValuePairs.add(new BasicNameValuePair("cardOps", "Display"));
	        nameValuePairs.add(new BasicNameValuePair("pass", password ));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpClient.execute(httppost);
	        
	        if (response != null) {
                result = EntityUtils.toString(response.getEntity());
            }
	        
	    } catch (ClientProtocolException e) {
	        result = "ClientProtocolException";
	    } catch (IOException e) {
	        result = "IOException";
	    }
	    
	    return result;
	} 
	
	/**
	 * Given an HTML page, determine whether the login was successful
	 * @param result HTML page returned from previous call
	 */
	public void parseResult(String result) {
		Log.d("GoCard", result);
		if (result.length() < 21) {
			wrongpassWarning.setVisibility(View.VISIBLE);
			return;
		}
		Log.d("GoCard", "resultEndOfFile=" + result.substring(result.length()-20));
		
		activity.setProgressBarIndeterminateVisibility(false);
		
		//If the HTML page shows the balance table then the loging was successful
		if (result.contains("<table id=\"balance-table\"")) 
		{
			wrongpassWarning.setVisibility(View.INVISIBLE);
			
			Fragment fragment = new GocardDisplayFragment();
			Bundle args = new Bundle();
			args.putString("BALANCE_RESULT", result);
			fragment.setArguments(args);
			
			// Switch to the GocardLoginDisplayFragment
			FragmentManager manager = activity.getSupportFragmentManager();
	   		FragmentTransaction transaction = manager.beginTransaction();
     		transaction.replace(R.id.content_frame, fragment);
            transaction.addToBackStack(null);
     		transaction.commit();
     		Log.d("TestCase","leaving parse result invisible");
		} 
		else 
		{	
			wrongpassWarning.setVisibility(View.VISIBLE);
			Log.d("TestCase","leaving parse result visible");
		}
		
	}
	
	private DefaultHttpClient createHttpClient()
	{	
		DefaultHttpClient ret = null;
		
		 //sets up parameters
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "utf-8");
        params.setBooleanParameter("http.protocol.expect-continue", false);

        //registers schemes for both http and https
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        registry.register(new Scheme("https", sslSocketFactory, 443));

        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
        ret = new DefaultHttpClient(manager, params);
        return ret;
	}
	
	public static DefaultHttpClient getHttpClient()
	{
		return httpClient;
	}
	
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	public void onStop() 
	{
		activity.setProgressBarIndeterminateVisibility(false);
		
		// Prevents crash when user hits back before the asynctask is finished
	    if (this.httpThread != null && this.httpThread.getStatus() == Status.RUNNING) 
	    	this.httpThread.cancel(true);
	    
	    super.onStop();
	}
	
	/* test functions*/
	public void setCountDownLatch(CountDownLatch lock) {
		this.lock = lock;
	}
	public void setGocardNumber(String num) {
		gcnumText.setText(num);
	}
	public void setPassword(String pass) {
		passwordText.setText(pass);
	}
	public TextView getWrongPassWarning() {
		return wrongpassWarning;
	}
	/*End of test functions */
}