package edu.uw.aca.scout;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.io.StringReader;


public class MainFragment extends Fragment {

    public static final String EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION";

    private WebView mWebView;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get reference of WebView from layout/activity_main.xml
        mWebView = (WebView) rootView.findViewById(R.id.fragment_main_webview);

        mWebView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("debug", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });

        // Add Javascript Interface, this will expose "window.NotificationBind"
        // in Javascript
        mWebView.addJavascriptInterface(
                new JsInterface(mWebView, getActivity().getApplicationContext()), "Android");

        setUpWebViewDefaults(mWebView);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore the previous URL and history stack
            mWebView.restoreState(savedInstanceState);
        }

        // Prepare the WebView
        String url = prepareWebView(mWebView.getUrl());

        // Load the home page
        if(mWebView.getUrl() == null) {
            mWebView.loadUrl(getString(R.string.home_url));
        }

        return rootView;
    }

    /**
     * Convenience method to set some generic defaults for a
     * given WebView
     *
     * @param webView
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setUpWebViewDefaults(WebView webView) {
        WebSettings settings = webView.getSettings();

        // Enable Javascript
        settings.setJavaScriptEnabled(true);

        // Use WideViewport and Zoom out if there is no viewport defined
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // Enable pinch to zoom without the zoom buttons
        settings.setBuiltInZoomControls(true);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            settings.setDisplayZoomControls(false);
        }

        // Enable remote debugging via chrome://inspect
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    /**
     * This is method where specific logic for this application is going to live
     * @return url to load
     */
    private String prepareWebView(String currentUrl) {
//        String hash = "";
//        int bgColor;
//
//        if(currentUrl != null) {
//            String[] hashSplit = currentUrl.split("#");
//            if(hashSplit.length == 2) {
//                hash = hashSplit[1];
//            }
//        } else {
//            Intent intent = getActivity().getIntent();
//            if(intent != null && intent.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false)) {
//                hash = "notification-launch";
//            }
//        }
//
//        if(hash.equals("notification-launch")) {
//            bgColor = Color.parseColor("#1abc9c");
//        } else if(hash.equals("notification-shown")) {
//            bgColor = Color.parseColor("#3498db");
//        } else if(hash.equals("secret")) {
//            bgColor = Color.parseColor("#34495e");
//        } else {
//            bgColor = Color.parseColor("#ffffff");
//        }
//
//        preventBGColorFlicker(bgColor);

        // We set the WebViewClient to ensure links are consumed by the WebView rather
        // than passed to a browser if it can
        mWebView.setWebViewClient(new WebViewClient());

        return "";
    }

    /**
     * This loads a url in the web view, does nothing if url is not different from currently loaded one
     *
     * @param Url
     */
    public void loadUrl(String Url) {
        if(Url != null && Url != mWebView.getUrl()){
            mWebView.loadUrl(Url);
        }
    }
    /**
     * This is a little bit of trickery to make the background color of the UI
     * the same as the anticipated UI background color of the web-app.
     *
     * @param bgColor
     */
    private void preventBGColorFlicker(int bgColor) {
        ((ViewGroup) getActivity().findViewById(R.id.content_main)).setBackgroundColor(bgColor);
        mWebView.setBackgroundColor(bgColor);
    }

    /**
     * This method is designed to hide how Javascript is injected into
     * the WebView.
     *
     * In KitKat the new evaluateJavascript method has the ability to
     * give you access to any return values via the ValueCallback object.
     *
     * The String passed into onReceiveValue() is a JSON string, so if you
     * execute a javascript method which return a javascript object, you can
     * parse it as valid JSON. If the method returns a primitive value, it
     * will be a valid JSON object, but you should use the setLenient method
     * to true and then you can use peek() to test what kind of object it is,
     *
     * @param javascript
     */
    public void loadJavascript(String javascript) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // In KitKat+ you should use the evaluateJavascript method
            mWebView.evaluateJavascript(javascript, new ValueCallback<String>() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onReceiveValue(String s) {
                    JsonReader reader = new JsonReader(new StringReader(s));

                    // Must set lenient to parse single values
                    reader.setLenient(true);

                    try {
                        if(reader.peek() != JsonToken.NULL) {
                            if(reader.peek() == JsonToken.STRING) {
                                String msg = reader.nextString();
                                if(msg != null) {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            msg, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.e("TAG", "MainActivity: IOException", e);
                    } finally {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                }
            });
        } else {
            /**
             * For pre-KitKat+ you should use loadUrl("javascript:<JS Code Here>");
             * To then call back to Java you would need to use addJavascriptInterface()
             * and have your JS call the interface
             **/
            mWebView.loadUrl("javascript:"+javascript);
        }
    }

    public boolean goBack() {
        if(!mWebView.canGoBack()) {
            return false;
        }

        mWebView.goBack();
        return true;
    }
}
