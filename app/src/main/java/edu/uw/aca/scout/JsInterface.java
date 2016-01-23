package edu.uw.aca.scout;

/**
 * Created by devights on 1/22/16.
 */import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class JsInterface {

    private WebView mWebView;
    private Context mContext;
    private String filterUrl;

    public JsInterface(WebView wv, Context con) {
        mWebView = wv;
        mContext = con;
    }

    /**
     * The '@JavascriptInterface is required to make the method accessible from the Javascript
     * layer
     * http://developer.android.com/training/notify-user/build-notification.html
     *
     * @param url The filter url to load
     */
    @JavascriptInterface
    public void showFilterResults(String url) {
        filterUrl = url;
        Log.d("debug", url);
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(mContext.getString(R.string.base_url) + "/" + filterUrl + "&hybrid=true");
            }
        });

    }

}
