package transponders.transmob;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		getView().setBackgroundResource(R.drawable.background_others);
	}
}
