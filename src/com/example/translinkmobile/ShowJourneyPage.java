package com.example.translinkmobile;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ShowJourneyPage extends Activity implements
		JSONRequest.NetworkListener {
	/** Displays the Translink's Journey Planner results in a Web View */
	String result;
	WebView wv;
	

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.journey_page);

		wv = (WebView) findViewById(R.id.webView);
		wv.setWebViewClient(new WebViewClient());

		Intent intent = getIntent();
		String[] ids = intent.getStringArrayExtra("locs");
		String fromId = ids[0];
		String destId = ids[1];
		String date = ids[2];
		String leaveOption = ids[3];
		requestPlan(fromId, destId, date, leaveOption);
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
