package transponders.translinkmobile;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MenuAdapter extends ArrayAdapter<String> {
	private final Context context;
	private final String[] values;
 
	public MenuAdapter(Context context, String[] values) {
		super(context, R.layout.drawer_list_item, values);
		this.context = context;
		this.values = values;
	}
 
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	 
			View rowView = inflater.inflate(R.layout.drawer_list_item, parent, false);
			TextView text = (TextView) rowView.findViewById(R.id.drawer_item);
			text.setText(values[position]);
			
			Drawable icon = null;
			switch(position)
			{
				case 0:
					icon = context.getResources().getDrawable(R.drawable.nearby_icon2);
					break;	
				case 1:
					icon = context.getResources().getDrawable(R.drawable.planner_icon);
					break;	
				case 2:
					icon = context.getResources().getDrawable(R.drawable.news_icon);
					break;	
				case 3:
					icon = context.getResources().getDrawable(R.drawable.go_card_icon);
					break;	
			}
			
			text.setCompoundDrawablePadding(20);
			text.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
			return rowView;
	}
}