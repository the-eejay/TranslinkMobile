package transponders.translinkmobile;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
		JSONRequest.NetworkListener, OnClickListener {

	public static final String ARGS_USER_LOC = "USER_LOCATION";
	
	// UI elements
	private EditText fromText;
	private EditText destText;
	private Spinner spinner;
	static Button dateButton;
	static Button timeButton;
	private final String TITLE = "Journey Planner";
	// params/options
	private List<String> paramList = new ArrayList<String>();
	int leaveOption = 0;
	int requests = 0;

	// Date/Time Settings
	static String date;
	static String time;
	final static Calendar c = Calendar.getInstance();
	static int year;
	static int month;
	static int day;
	static int hour;
	static int minute;
	static Calendar currentDate = Calendar.getInstance();
	static Calendar selectedDate = Calendar.getInstance();
	
	double[] userLoc;
	
	// For testing purposes
	static ShowJourneyPage showJPFragment = null;
	private JSONRequest request;

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.journey_planner, container, false);
		userLoc = getArguments().getDoubleArray(ARGS_USER_LOC);
		
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		
		currentDate.clear();
		currentDate.set(year, month, day);
		
		selectedDate.clear();
		selectedDate.set(year, month, day);
		
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
		dateButton.setOnClickListener(this);
		timeButton.setOnClickListener(this);
		
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
				paramList.clear();
				if (isNetworkAvailable()) {
					Log.d("BUTTON ONCLICK", "this function is called");
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
	
	@Override
	public void onResume()
	{
		super.onResume();
		getActivity().getActionBar().setTitle(TITLE);
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void getLocIds() {
		Log.d("BUTTON ONCLICK", "getLocIDs is called");
		String origin = fromText.getText().toString();
		String destination = destText.getText().toString();
		
		if (destination == null || destination.equalsIgnoreCase(""))
		{
			// User did not enter the destination
			Toast.makeText(getActivity().getApplicationContext(), "Please enter the destination!", Toast.LENGTH_SHORT).show();
		    return;
		}
		
		if(selectedDate.before(currentDate))
		{
			// The entered date has already passed
			Toast.makeText(getActivity().getApplicationContext(), "The entered date has already passed!", Toast.LENGTH_SHORT).show();
		    return;
		}
		
		if (origin == null || origin.equalsIgnoreCase(""))
		{
			// User did not input the from location, so use current location
			origin = userLoc[0] + ", " + userLoc[1];
		}

		getLocationId(origin);
		getLocationId(destination);                                                                                     
	}

	private void getLocationId(String loc) 
	{
		Log.d("BUTTON ONCLICK", "getLocationId is called");
		String url = "http://deco3801-010.uqcloud.net/resolve.php?input=" + Uri.encode(loc);
		request = new JSONRequest();
		request.setListener(this);
		request.execute(url);
	}

	@Override
	public void networkRequestCompleted(String result) {
		Log.d("BUTTON ONCLICK", "networkRequestCompleted is called");
		paramList.add(result);
		if (paramList.size() == 2) {
			// We need to make two calls to resolve.php before we can continue
			
			paramList.add(date + " " + time);
			paramList.add("" + leaveOption);
			Log.d("LeaveOption: ", "" + leaveOption);
			Object[] paramArray = paramList.toArray();
			String[] paramStrArray = Arrays.copyOf(paramArray, paramArray.length,
					String[].class);
			
			
			showJPFragment = new ShowJourneyPage();
			
			Log.d("BUTTON ONCLICK", "ShowJourneyPage() is called");
			Fragment fragment2 = showJPFragment;
    		Bundle args = new Bundle();
            args.putStringArray(ShowJourneyPage.ARGS_JOURNEY, paramStrArray);
            fragment2.setArguments(args);
    		
    	    FragmentManager fragmentManager = getFragmentManager();
    	    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	    fragmentTransaction.replace(R.id.content_frame, fragment2);
    	    fragmentTransaction.addToBackStack(null);
    	    fragmentTransaction.commitAllowingStateLoss();
		}
	}
	
	@Override
    public void onClick(View v) 
	{
		DialogFragment newFragment;
		
        switch (v.getId()) 
        {
	        case R.id.dateSpinner:
	        	newFragment = new DatePickerFragment();
	    	    newFragment.show(getFragmentManager(), "datePicker");
	            break;
	        case R.id.timeSpinner:
	        	newFragment = new TimePickerFragment();
	    	    newFragment.show(getFragmentManager(), "timePicker");
	    	    break;	
        }
    }
	
	public static class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) 
		{
			// Use the current date as the default date in the picker
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) 
		{
			selectedDate.clear();
			selectedDate.set(year, month, day);
			
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
	
	/*Testing functions */
	public static Calendar getCurrentDate()
	{
		return c;
	}
	
	public void setDate(int year, int month, int day)
	{
		selectedDate.clear();
		selectedDate.set(year, month, day);
		
		date = (month+1) + "/" + day + "/" + year;
	}
	
	public void setTime(int hourOfDay, int minute)
	{
		String temp;
		if(minute < 10)
			temp = "0" + minute;
		else
			temp = "" + minute;

		time = hourOfDay + ":" + temp;
	}
	
	public JSONRequest getJSONRequest()
	{
		return request;
	}
	
	public List<String> getResolvedParameters()
	{
		return paramList;
	}
	
	public ShowJourneyPage getShowJourneyPageFragment()
	{
		return showJPFragment;
	}
	/*End of Testing functions */
}
