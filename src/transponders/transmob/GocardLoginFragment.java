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

import transponders.transmob.NearbyStops.StackState;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
	
	private DefaultHttpClient httpClient;
	
	
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
				
				
					Fragment fragment = new GocardDisplayFragment();
					Bundle args = new Bundle();
					args.putString("goCardNumber", gcNumber);
					args.putString("password", password);
					fragment.setArguments(args);
					
					FragmentManager manager = getActivity().getSupportFragmentManager();
			   		FragmentTransaction transaction = manager.beginTransaction();
		     		transaction.replace(R.id.content_frame, fragment);
		            transaction.addToBackStack(null);
		     		transaction.commit();
		     		
		     		
				
				break;
		}
	}
	
	
}