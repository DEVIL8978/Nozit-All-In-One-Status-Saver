package com.cd.statussaver.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cd.statussaver.R;
import com.cd.statussaver.databinding.ActivityLoginBinding;
import com.cd.statussaver.util.SharePrefs;
import com.cd.statussaver.util.Utils;


public class FBLoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    FBLoginActivity activity;
    private String cookies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        activity = this;
        loadPage();
        binding.swipeRefreshLayout.setOnRefreshListener(() -> loadPage());
    }

    public void loadPage() {
        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.clearCache(true);
        CookieSyncManager.createInstance(activity);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);

        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        binding.webView.addJavascriptInterface(activity, "Android");
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true);
            webSettings.setMixedContentMode(2);
        }
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            binding.webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress == 100) {
                    binding.swipeRefreshLayout.setRefreshing(false);
                } else {
                    binding.swipeRefreshLayout.setRefreshing(true);
                }
            }
        });

        binding.webView.setWebViewClient(new MyBrowser());

        binding.webView.loadUrl("https://www.facebook.com/");
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            if (Build.VERSION.SDK_INT >= 21) {
                webView.loadUrl(webResourceRequest.getUrl().toString());
                cookies = CookieManager.getInstance().getCookie(webResourceRequest.getUrl().toString());
                if (!Utils.isNullOrEmpty(cookies) && cookies.contains("c_user")) {
                    SharePrefs.getInstance(activity).putString(SharePrefs.FBCOOKIES, cookies);
                }
            }
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            cookies = CookieManager.getInstance().getCookie(url);
            if (!Utils.isNullOrEmpty(cookies) && cookies.contains("c_user")) {
                SharePrefs.getInstance(activity).putString(SharePrefs.FBCOOKIES, cookies);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView webView, String str) {
            super.onPageFinished(webView, str);
            cookies = CookieManager.getInstance().getCookie(str);
            webView.loadUrl("javascript:Android.resultOnFinish();");
            webView.loadUrl("javascript:var el = document.querySelectorAll('input[name=fb_dtsg]');Android.resultOnFinish(el[0].value);");
        }
    }

    @JavascriptInterface
    public void resultOnFinish(String key) {
        if (key.length() < 15) {
            return;
        }
        try {
            if (!Utils.isNullOrEmpty(cookies) && cookies.contains("c_user")) {
                SharePrefs.getInstance(activity).putString(SharePrefs.FBKEY, key);
                SharePrefs.getInstance(activity).putBoolean(SharePrefs.ISFBLOGIN, true);
                System.out.println("Key - " + key);
                Intent intent = new Intent();
                intent.putExtra("result", "result");
                setResult(RESULT_OK, intent);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}