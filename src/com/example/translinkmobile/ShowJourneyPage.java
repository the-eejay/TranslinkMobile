package com.example.translinkmobile;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * The Android activity that shows the result of the journey planner
 * in a Web View.
 * 
 * @author Transponders
 * @version 1.0
 */
public class ShowJourneyPage extends Fragment implements JSONRequest.NetworkListener {

	public final static String ARGS_JOURNEY = "JOURNEY_PARAMETERS";
	String result;
	WebView wv;
	
	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View view = inflater.inflate(R.layout.journey_page, container, false);

		wv = (WebView) view.findViewById(R.id.webView);
		wv.setWebViewClient(new WebViewClient());

		String[] ids = getArguments().getStringArray(ARGS_JOURNEY);
		String fromId = ids[0];
		String destId = ids[1];
		String date = ids[2];
		String leaveOption = ids[3];
		requestPlan(fromId, destId, date, leaveOption);
		
		return view;
	}

	private void requestPlan(String fromId, String destId, String date, String leaveOption) {

		/* Call our php code on web zone */

		String urlString = "http://deco3801-010.uqcloud.net/journeyplan.php?fromLocId="
				+ Uri.encode(fromId) + "&destLocId=" + Uri.encode(destId) + "&date=" + Uri.encode(date)
				+ "&leaveOption=" + Uri.encode(leaveOption);
		Log.d("JourneyMap request: ", urlString);
		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
	}

	@Override
	public void networkRequestCompleted(String result) {
		this.result = result;
		Log.d("ShowJourneyPage got result: ", result);
		parseResult(result);
	}

	private void parseResult(String result) {
		Object obj = JSONValue.parse(result);
		String url = (String) ((JSONObject) obj).get("JourneyPlannerUrl");
		Log.d("URL: ", url.toString());
		/* Got the URL, time to load it into the web view */
		wv.loadUrl(url);
	}
}
