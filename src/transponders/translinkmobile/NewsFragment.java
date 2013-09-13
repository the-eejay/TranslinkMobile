package transponders.translinkmobile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NewsFragment extends Fragment {
	
	public final static String ARG_NEWS_URL = "NEWSURL";
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.news_layout, container, false);
		String url = getArguments().getString(ARG_NEWS_URL);
		
		WebView wv = (WebView) view.findViewById(R.id.newsWebView);
		wv.setWebViewClient(new WebViewClient());
		wv.loadUrl(url);
		
        return view;
    }

}
