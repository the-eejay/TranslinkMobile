package transponders.translinkmobile;

import android.content.Context;
import android.widget.Toast;

public class JavaScriptInterface 
{
	Context mContext;
	
	/** Instantiate the interface and set the context */
	public JavaScriptInterface(Context c) 
	{
	    mContext = c;
	}
	
	/** Show a toast from the web page */
	@android.webkit.JavascriptInterface
	public void showToast(String toast) 
	{
	    Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
	}
}
