package transponders.transmob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
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
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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
	// UI elements
	private EditText gcnumText;
	private EditText passwordText;
	private Button loginButton;
	
	private String gcNumber, password, result;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.gocard_login, container, false);
		
		gcnumText = (EditText) view.findViewById(R.id.gcnum_input);
		passwordText = (EditText) view.findViewById(R.id.password_input);

		loginButton = (Button) view.findViewById(R.id.login_button);
		loginButton.setOnClickListener(this);
		
		Button gcnumClear = (Button) view.findViewById(R.id.gcnum_clear_button);
		Button passwordClear = (Button) view.findViewById(R.id.password_clear_button);
		
		gcnumClear.setOnClickListener(this);
		passwordClear.setOnClickListener(this);
		
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
				gcNumber = gcnumText.getText().toString();
				password = passwordText.getText().toString();
				
				HttpThread ht = new HttpThread();
				ht.execute(gcNumber, password);
				break;
		}
	}
	
	private HttpClient createHttpClient()
	{
	    HttpParams params = new BasicHttpParams();
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
	    HttpProtocolParams.setUseExpectContinue(params, true);

	    SchemeRegistry schReg = new SchemeRegistry();
	    schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
	    ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

	    return new DefaultHttpClient(conMgr, params);
	}
	
	public String postData() {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = createHttpClient();
	    HttpPost httppost = new HttpPost("https://gocard.translink.com.au/webtix/welcome/welcome.do");
	    
	    Log.d("postData()", "inside postData()");
	    result = "not yet...";

	    try 
	    {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("CardNumber", "0160029286217844"));
	        nameValuePairs.add(new BasicNameValuePair("Password", "transponders1234"));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
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
	
	private class HttpThread extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String... params) 
		{
			String res = postData();
			return res;
		}
		
		@Override
	    protected void onPostExecute(String result) {
	    	Log.d("JSONRequest", "postexecute");
	    	Log.d("TEST HTTPPOST", result);
	    	
	    	super.onPostExecute(result);
	    }	
	}
}