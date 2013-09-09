package com.example.translinkmobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * The Fragment class that displays the input form for the journey planner.
 * This class gets the location IDs from translink's resolve API and sends them to
 * ShowJourneyPage class, a view that displays translink's journey plan page.
 * 
 * @author Transponders
 * @version 1.0
 */
public class JourneyPlanner extends Fragment implements
		JSONRequest.NetworkListener {

	// UI elements
	private EditText fromText;
	private EditText destText;
	private Spinner spinner;
	static Button dateButton;
	static Button timeButton;
	
	// params/options
	private List<String> paramList = new ArrayList<String>();
	int leaveOption = 0;
	int requests = 0;

	// Date/Time Settings
	static String date;
	static String time;
	final Calendar c = Calendar.getInstance();
	static int year;
	static int month;
	static int day;
	static int hour;
	static int minute;

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.journey_planner, container, false);
		
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
		
		dateButton = (Button) view.findViewById(R.id.dateSpinner);
		timeButton = (Button) view.findViewById(R.id.timeSpinner);
		dateButton.setText(date);
		timeButton.setText(time);
		
		spinner = (Spinner) view.findViewById(R.id.leave_options_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.leave_options_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {           
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            	leaveOption = position;
            	// We need to disable date/time in the right circumstance
    	        if (position > 1) {
    	        	// First or last services
    	        	timeButton.setEnabled(false);
    	        	dateButton.setEnabled(false);
    	        } else {
    	        	timeButton.setEnabled(true);
    	        	dateButton.setEnabled(true);
    	        }
            }

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

		fromText = (EditText) view.findViewById(R.id.fromLocation);
		destText = (EditText) view.findViewById(R.id.toLocation);

		Button button = (Button) view.findViewById(R.id.sendDestButton);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isNetworkAvailable()) {
					getLocIds();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity().getApplicationContext());
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
		
		return view;
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void getLocIds() {
		getLocationId(fromText.getText().toString());
		getLocationId(destText.getText().toString());                                                                                     
	}

	private void getLocationId(String loc) {
		
		if (loc == null || loc.equalsIgnoreCase("")) {
			// User did not enter a location
			Toast.makeText(getActivity().getApplicationContext(), "Please enter the From & To location!", Toast.LENGTH_SHORT).show();
		    return;
		}
			
		String url = "http://deco3801-010.uqcloud.net/resolve.php?input="
				+ Uri.encode(loc);
		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(url);
	}

	@Override
	public void networkRequestCompleted(String result) {
		paramList.add(result);
		if (paramList.size() == 2) {
			// We need to make two calls to resolve.php before we can continue
			Intent intent = new Intent(getActivity().getApplicationContext(),
					ShowJourneyPage.class);
			
			paramList.add(date + " " + time);
			paramList.add("" + leaveOption);
			Log.d("LeaveOption: ", "" + leaveOption);
			Object[] paramArray = paramList.toArray();
			String[] paramStrArray = Arrays.copyOf(paramArray, paramArray.length,
					String[].class);
			
			intent.putExtra("locs", paramStrArray);
			startActivity(intent);
		}
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
