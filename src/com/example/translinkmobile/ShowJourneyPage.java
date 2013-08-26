package com.example.translinkmobile;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class ShowJourneyPage extends Activity implements JSONRequest.NetworkListener {

	String result;
	WebView wv;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.journey_page);
		wv = (WebView) findViewById (R.id.webView);
		Intent intent = getIntent();
		String[] ids = intent.getStringArrayExtra("locs");
		String fromId = ids[0];
		String destId = ids[1];
		requestPlan(fromId, destId);
	}

	private void requestPlan(String fromId, String destId) {
		
		/* Call our php code on web zone */
		
		String urlString = "http://deco3801-010.uqcloud.net/journeyplan.php?fromLocId=" + Uri.encode(fromId) + "&destLocId=" + Uri.encode(destId);
		Log.d("JourneyMap request: ", urlString);
		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
	}
	@Override
	public void networkRequestCompleted(String result) {
		this.result = result;
		Log.d("Journeymap got result: ", result);
		parseResult(result);
	}

	private void parseResult(String result) {
		wv.loadUrl(result);
	}
}
