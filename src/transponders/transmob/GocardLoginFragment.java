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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
	
	/*private enum State {
		BALANCE_TABLE, 
	};*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.gocard_login, container, false);
		
		gcnumText = (EditText) view.findViewById(R.id.gcnum_input);
		passwordText = (EditText) view.findViewById(R.id.password_input);

		gcnumText.setText("0160041933728206");
		
		
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
				//ht.execute(gcNumber, password);
				String url = "https://gocard.translink.com.au/webtix/welcome/welcome.do";
				ht.execute(url);
				break;
		}
	}
	
	private DefaultHttpClient createHttpClient()
	{
	    /*HttpParams params = new BasicHttpParams();
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
	    HttpProtocolParams.setUseExpectContinue(params, true);

	    SchemeRegistry schReg = new SchemeRegistry();
	    schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
	    ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

	    return new DefaultHttpClient(conMgr, params);*/
		
		DefaultHttpClient ret =null;
		
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
	
	public String postData(String url) {
	    // Create a new HttpClient and Post Header
	    HttpClient httpclient = createHttpClient();
	    HttpPost httppost = new HttpPost(url);
	    
	    Log.d("postData()", "inside postData()");
	    result = "not yet...";

	    try 
	    {
	        // Add your data
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        Log.d( "GoCard", "Number Entered="+gcnumText.getText().toString() );
	        /*nameValuePairs.add(new BasicNameValuePair("CardNumber", gcnumText.getText().toString()));
	        nameValuePairs.add(new BasicNameValuePair("Password", passwordText.getText().toString()));*/
	        nameValuePairs.add(new BasicNameValuePair("cardNum", gcnumText.getText().toString()));
	        nameValuePairs.add(new BasicNameValuePair("cardOps", "Display"));
	        nameValuePairs.add(new BasicNameValuePair("pass", passwordText.getText().toString()));
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
			String res = postData(params[0]);
			return res;
		}
		
		@Override
	    protected void onPostExecute(String result) {
	    	Log.d("JSONRequest", "postexecute");
	    	//Log.d("TEST HTTPPOST", result);
	    	
	    	super.onPostExecute(result);
	    	
	    	parseResult(result);
	    }	
	}
	
	public void parseResult(String result) {
		Log.d("GoCard", result);
		Log.d("GoCard", "resultEndOfFile=" + result.substring(result.length()-20));
		
		if (result.contains("<table id=\"balance-table\"")) {
			parseResultAsBalance(result);
		} else if (result.contains("<table id=\"travel-history\"")) {
			parseResultAsHistory(result);
		} else {
			TableLayout table = (TableLayout) getView().findViewById(R.id.history_table);
	        Context tableContext = table.getContext();
			TableRow newRow = new TableRow(tableContext);
			Context rowContext = newRow.getContext();
			TextView text = new TextView(rowContext);
        	text.setText(result);
        	newRow.addView(text);
        	table.addView(newRow);
        	
        	
		}
	}
	
	public void parseResultAsBalance(String result) {
		int indexOfCardBalance = result.indexOf("<table id=\"balance-table\"");
		int indexOfEndOfCardBalance = result.indexOf("</table>", indexOfCardBalance);
		String tableSubstr = result.substring(indexOfCardBalance, indexOfEndOfCardBalance);
		String[] tableRowStrings = tableSubstr.split("<tr>"); 
		for (int i = 2; i < tableRowStrings.length; i++) {
			String rowStr = tableRowStrings[i];
			TableLayout table = (TableLayout) getView().findViewById(R.id.balance_table);
	        Context tableContext = table.getContext();
			TableRow newRow = new TableRow(tableContext);
			Context rowContext = newRow.getContext();
			DisplayMetrics scale = getActivity().getResources().getDisplayMetrics();
        	int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, scale);
        	newRow.setMinimumHeight(height);
        	TextView text = new TextView(rowContext);
        	int endOfFirstColumn = rowStr.indexOf("</td>");
        	String dateText = rowStr.substring(rowStr.indexOf("<td>")+3, endOfFirstColumn);
        	text.setText(dateText);
        	//text.setText(result);
        	 TableRow.LayoutParams param0 = new TableRow.LayoutParams();
             param0.column = 0;
             param0.span = 30;
             param0.weight = 1;
             text.setLayoutParams(param0);
        	newRow.addView(text);
        	
        	TextView text1 = new TextView(rowContext);
        	String costText = rowStr.substring(rowStr.indexOf("<td>", endOfFirstColumn)+3, rowStr.indexOf("</td>", endOfFirstColumn+3));
        	text1.setText(costText);
        	TableRow.LayoutParams param1 = new TableRow.LayoutParams();
            param1.column = 1;
            param1.span = 30;
            param1.weight = 1;
            text1.setLayoutParams(param1);
            
        	table.addView(newRow);
		}
		HttpThread ht = new HttpThread();
		//ht.execute(gcNumber, password);
		String url = "https://gocard.translink.com.au/webtix/tickets-and-fares/go-card/online/history";
		ht.execute(url);
	}
	
	public void parseResultAsHistory(String result) {
		
		int indexOfHistory = result.indexOf("<table id=\"travel-history\"");
		int indexOfEndOfHistory = result.indexOf("</table>", indexOfHistory);
		String tableSubstr = result.substring(indexOfHistory, indexOfEndOfHistory);
		String[] tableRowStrings = tableSubstr.split("<tr>"); 
		for (int i = 2; i < tableRowStrings.length; i++) {
			String rowStr = tableRowStrings[i];
			TableLayout table = (TableLayout) getView().findViewById(R.id.history_table);
	        Context tableContext = table.getContext();
			TableRow newRow = new TableRow(tableContext);
			Context rowContext = newRow.getContext();
			DisplayMetrics scale = getActivity().getResources().getDisplayMetrics();
        	int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, scale);
        	newRow.setMinimumHeight(height);
        	TextView text = new TextView(rowContext);
        	int endOfFirstColumn = rowStr.indexOf("</td>");
        	String dateText = rowStr.substring(rowStr.indexOf("<td>")+3, endOfFirstColumn);
        	text.setText(dateText);
        	//text.setText(result);
        	 TableRow.LayoutParams param0 = new TableRow.LayoutParams();
             param0.column = 0;
             param0.span = 30;
             param0.weight = 1;
             text.setLayoutParams(param0);
        	newRow.addView(text);
        	
        	TextView text1 = new TextView(rowContext);
        	String costText = rowStr.substring(rowStr.indexOf("<td>", endOfFirstColumn)+3, rowStr.indexOf("</td>", endOfFirstColumn+3));
        	text1.setText(costText);
        	TableRow.LayoutParams param1 = new TableRow.LayoutParams();
            param1.column = 1;
            param1.span = 30;
            param1.weight = 1;
            text1.setLayoutParams(param1);
            
        	table.addView(newRow);
		}
		
	}
}