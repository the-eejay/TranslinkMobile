package transponders.translinkmobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
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
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
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
		JSONRequest.NetworkListener, OnClickListener, OnItemSelectedListener {

	public static final String ARGS_USER_LOC = "USER_LOCATION";
	
	// UI elements
	private AutoCompleteTextView fromText;
	private AutoCompleteTextView destText;
	private Spinner spinner;
	private Spinner vehicleSpinner;
	private Spinner maxWalkSpinner;
	private static Button dateButton;
	private static Button timeButton;
	private static Button fromMyLocButton;
	private static Button fromClearButton;
	private static Button toMyLocButton;
	private static Button toClearButton;
	private final String TITLE = "Journey Planner";
	
	// params/options
	private List<String> resolvedLocations = new ArrayList<String>();
	private List<String> paramList = new ArrayList<String>();
	private HashMap<String, String> locationsToID = new HashMap<String, String>();
	int leaveOption = 0;
	int requests = 0;
	int vehicleType = 0;
	int maxWalkDistance = 1000;
	boolean hasChosenFromLocation = false;
	boolean hasChosenToLocation = false;

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
		
		fromText = (AutoCompleteTextView) view.findViewById(R.id.fromLocation);
		destText = (AutoCompleteTextView) view.findViewById(R.id.toLocation);
		
		resolvedLocations.add("ZERO");
		resolvedLocations.add("ONE");
		resolvedLocations.add("TWO");
		resolvedLocations.add("THREE");
		resolvedLocations.add("FOUR");
		
		LocationAdapter fromAdapter = new LocationAdapter(getActivity(),
			    android.R.layout.simple_spinner_dropdown_item, resolvedLocations, fromText);
		
		LocationAdapter destAdapter = new LocationAdapter(getActivity(),
			    android.R.layout.simple_spinner_dropdown_item, resolvedLocations, destText);
		
		fromText.setAdapter(fromAdapter);
		destText.setAdapter(destAdapter);
		fromText.setThreshold(3);
		destText.setThreshold(3);
		
		fromMyLocButton = (Button) view.findViewById(R.id.from_myloc_button);
		toMyLocButton = (Button) view.findViewById(R.id.to_myloc_button);
		fromMyLocButton.setOnClickListener(this);
		toMyLocButton.setOnClickListener(this);
		
		fromClearButton = (Button) view.findViewById(R.id.from_clear_button);
		toClearButton = (Button) view.findViewById(R.id.to_clear_button);
		fromClearButton.setOnClickListener(this);
		toClearButton.setOnClickListener(this);
		
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
		spinner.setOnItemSelectedListener(this);

		vehicleSpinner = (Spinner) view.findViewById(R.id.vehicle_type_spinner);
		ArrayAdapter<CharSequence> vehicleAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.vehicle_type_array, android.R.layout.simple_spinner_item);
		vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		vehicleSpinner.setAdapter(vehicleAdapter);
		vehicleSpinner.setOnItemSelectedListener(this);
		
		maxWalkSpinner = (Spinner) view.findViewById(R.id.max_walk_spinner);
		ArrayAdapter<CharSequence> maxWalkAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.max_walk_array, android.R.layout.simple_spinner_item);
		maxWalkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		maxWalkSpinner.setAdapter(maxWalkAdapter);
		maxWalkSpinner.setOnItemSelectedListener(this);
		maxWalkSpinner.setSelection(1);

		Button button = (Button) view.findViewById(R.id.sendDestButton);
		button.setOnClickListener(this);
		
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

	@Override
	public void networkRequestCompleted(String result) {
		
		Log.d("NETWORK REQ COMPLETED", result);
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
	    	    
	        case R.id.from_myloc_button:
	        	String fromDisplay = "" + userLoc[0] + ", " + userLoc[1];
	        	fromText.setText(fromDisplay);
	        	locationsToID.put(fromDisplay, "GP:" + fromDisplay);
	        	break;
	        	
	        case R.id.to_myloc_button:
	        	String toDisplay = "" + userLoc[0] + ", " + userLoc[1];
	        	destText.setText(toDisplay);
	        	locationsToID.put(toDisplay, "GP:" + toDisplay);
	        	break;
	        	
	        case R.id.from_clear_button:
	        	fromText.setText("");
	        	break;
	        	
	        case R.id.to_clear_button:
	        	destText.setText("");
	        	break;
	        	
	        case R.id.sendDestButton:
	        	paramList.clear();
				if (isNetworkAvailable()) {
					Log.d("BUTTON ONCLICK", "this function is called");
					//getLocIds();
					
					String origin = fromText.getText().toString();
					
					if(origin == null || origin.equalsIgnoreCase(""))
					{
						paramList.add(0, "GP:" + userLoc[0] + "," + userLoc[1]);
					}
					else
					{
						String originID = locationsToID.get(origin);
						
						if(originID == null)
						{
							Toast.makeText(getActivity().getApplicationContext(), "Please select a valid location from the dropdown!", Toast.LENGTH_SHORT).show();
							return;
						}
						else
						{
							paramList.add(0, originID);
						}
					}
					
					String destination = destText.getText().toString();
					
					if(destination == null || destination.equalsIgnoreCase(""))
					{
						Toast.makeText(getActivity().getApplicationContext(), "Please enter the destination!", Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						String destinationID = locationsToID.get(destination);
						
						if(destinationID == null)
						{
							Toast.makeText(getActivity().getApplicationContext(), "Please select a valid location from the dropdown!", Toast.LENGTH_SHORT).show();
							return;
						}
						else
						{
							paramList.add(1, destinationID);
						}
					}
					
					paramList.add(date + " " + time);
					paramList.add("" + leaveOption);
					paramList.add("" + vehicleType);
					paramList.add("" + maxWalkDistance);
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
		    	    
				} else {
					Toast.makeText(getActivity().getApplicationContext(), "No internet connection!", Toast.LENGTH_SHORT).show();
				}
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

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

		if(view != null)
		{
			switch(parent.getId())
			{
				case R.id.fromLocation:
					
					break;
					
				case R.id.toLocation:

					break;
			
				case R.id.leave_options_spinner:
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
	    	        break;
	    	        
				case R.id.vehicle_type_spinner:
					if(position > 0)
						vehicleType = (int) Math.pow(2, position);
					else
						vehicleType = 0;
					break;
					
				case R.id.max_walk_spinner:
					maxWalkDistance = (position + 1) * 500;
					break;
			}
		}		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {}
	
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
	
	private class LocationAdapter extends ArrayAdapter<String>
	{
		List<String> objects;
		AutoCompleteTextView ownerACTV;
		
		public LocationAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}
		
		public LocationAdapter(Context context, int textViewResourceId,
				List<String> objects, AutoCompleteTextView actv) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
			this.ownerACTV = actv;
		}
		
		@Override
	    public Filter getFilter() 
		{
			Filter filter = new Filter(){
				  @Override
	              protected FilterResults performFiltering(CharSequence constraint)
				  {
					  objects.clear();
					  FilterResults results = new FilterResults();
					  String typedText = ownerACTV.getText().toString();
					  
					  try
					  {
						  Log.d("performFiltering", "TYPED TEXT LENGTH: " + typedText.length());
						  if(typedText.length() >= 3)
						  {
							  String url = "http://deco3801-010.uqcloud.net/resolve.php?input=" 
								  		+ Uri.encode(typedText)
								  		+ "&maxResults=" + Uri.encode("5");
							  request = new JSONRequest();
							  request.setListener(JourneyPlanner.this);
							  request.execute(url);
							  
							  String result = request.get();
							  
							  Object obj = JSONValue.parse(result);
							  JSONArray array = (JSONArray)((JSONObject)obj).get("Locations");
									
							  for(int i = 0; i < array.size(); i++)
							  {
								  JSONObject location = (JSONObject) array.get(i);
								  String desc = (String) location.get("Description");
								  String id = (String) location.get("Id");
								
								  Log.d("filterResults", "Description: " + desc);
								  Log.d("filterResults", "ID: " + id);
								  
								  String[] splitted = desc.split(" \\(");
								  if (splitted.length > 1)
									  desc = splitted[0];
								  
								  locationsToID.put(desc, id);
								  objects.add(desc);
							  }
						  }

						  results.values = objects;
						  results.count = objects.size();
					  
					  }
					  catch (Exception e)
					  {
						  e.printStackTrace();
					  }
					  
					  Log.d("performFiltering", "return FilterResults");
					  return results;
				  }
				  
				  @Override
			      protected void publishResults(CharSequence arg0, FilterResults arg1) 
				  {
			          notifyDataSetChanged();
			      }
			};
			
			return filter;
		}	
	}
}
