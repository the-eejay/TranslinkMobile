package com.example.translinkmobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import com.example.support.SlideHolder;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class JourneyPlanner extends Activity implements
		JSONRequest.NetworkListener {
	/**
	 * Gets the location IDs from translink's resolve API and sends them to
	 * JourneyMap class
	 */

	private EditText fromText;
	private EditText destText;
	private SlideHolder mSlideHolder;
	private ActionBar bar;

	private LocationManager locManager;
	private Location loc;
	private List<String> idList = new ArrayList<String>();
	int requests = 0;

	static String date;
	static String time;
	final Calendar c = Calendar.getInstance();
	static int year;
	static int month;
	static int day;
	static int hour;
	static int minute;
	static Button dateButton;
	static Button timeButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.journey_planner);

		mSlideHolder = (SlideHolder) findViewById(R.id.slideHolder);
		bar = getActionBar();

		bar.setDisplayHomeAsUpEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// bar.setHomeButtonEnabled(true);
		}
		
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		date = (month+1) + "/" + day + "/" + year;
		
		String temp;
		if(minute < 10)
			temp = "0" + minute;
		else
			temp = "" + minute;
		
		time = hour + ":" + temp;
		
		dateButton = (Button) findViewById(R.id.dateSpinner);
		timeButton = (Button) findViewById(R.id.timeSpinner);
		dateButton.setText(date);
		timeButton.setText(time);

		fromText = (EditText) findViewById(R.id.fromLocation);
		destText = (EditText) findViewById(R.id.toLocation);

		Button button = (Button) findViewById(R.id.sendDestButton);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isNetworkAvailable()) {
					getLocIds();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getApplicationContext());
					builder.setTitle("No network connection");
					builder.setMessage("No network connection!");
					builder.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				}
			}
		});
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void getLocIds() {
		getLocationId(fromText.getText().toString());
		getLocationId(destText.getText().toString());
	}

	private void getLocationId(String loc) {

		String url = "http://deco3801-010.uqcloud.net/resolve.php?input="
				+ Uri.encode(loc);
		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(url);
	}

	@Override
	public void networkRequestCompleted(String result) {
		idList.add(result);
		if (idList.size() == 2) {
			Intent intent = new Intent(getApplicationContext(),
					ShowJourneyPage.class);
			
			idList.add(date + " " + time);
			Object[] objArray = idList.toArray();
			String[] strArray = Arrays.copyOf(objArray, objArray.length,
					String[].class);
			
			intent.putExtra("locs", strArray);
			startActivity(intent);
		}
	}

	public void nsClick(View view) {
		startActivity(new Intent(getApplicationContext(), NearbyStops.class));
	}

	public void jpClick(View view) {
		startActivity(new Intent(getApplicationContext(), JourneyPlanner.class));
	}
	
	public void showTimePickerDialog(View v) {
	    DialogFragment newFragment = new TimePickerFragment();
	    newFragment.show(getFragmentManager(), "timePicker");
	}
	
	public void showDatePickerDialog(View v) {
	    DialogFragment newFragment = new DatePickerFragment();
	    newFragment.show(getFragmentManager(), "datePicker");
	}

	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			date = (month+1) + "/" + day + "/" + year;
			dateButton.setText(date);
		}
	}

	public static class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// Do something with the time chosen by the user
			String temp;
			if(minute < 10)
				temp = "0" + minute;
			else
				temp = "" + minute;
			
			Log.d("Selected time: ", time);
			time = hourOfDay + ":" + temp;
			timeButton.setText(time);
		}
	}
}
