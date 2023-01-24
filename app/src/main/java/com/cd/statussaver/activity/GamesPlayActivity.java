package com.cd.statussaver.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cd.statussaver.R;
import com.cd.statussaver.databinding.ActivityGamesPlayBinding;


public class GamesPlayActivity extends AppCompatActivity {
    private ActivityGamesPlayBinding mBinding;
    private String intentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_games_play);
        try {
            intentUrl = getIntent().getStringExtra("url");
        } catch (Exception e) {
            e.printStackTrace();
        }

        WebView webView = mBinding.webView;
        webView.clearCache(true);
        webView.setWebViewClient(new MyBrowser());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        WebSettings settingWebView = webView.getSettings();
        settingWebView.setJavaScriptEnabled(true);
        settingWebView.setJavaScriptCanOpenWindowsAutomatically(true);
        settingWebView.setAllowFileAccess(true);
        settingWebView.setDomStorageEnabled(true);

        settingWebView.setCacheMode(WebSettings.LOAD_DEFAULT);
//        webView.loadUrl(intentUrl);
        if (intentUrl.equals("Helix")) {
            webView.loadUrl("file:///android_asset/helix/index.html");
        } else if (intentUrl.equals("2048")) {
            webView.loadUrl("file:///android_asset/twozero/index.html");
        }
        webView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
    }

    @Override
    public void onBackPressed() {
        mBinding.webView.destroy();
        finish();
    }

    public void checkScore(String message) {
        int score = Integer.parseInt(message);
        showGameOverDialog("Nice try! Play Again.\n Your Score- "+score);
    }

    public void ShowToast(String message) {
        Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
    }

    public void showGameOverDialog(String Message) {
        View mView = getLayoutInflater().inflate(R.layout.dialog_game_over, null);
        Dialog customDialog = new Dialog(this, R.style.CustomDialog);
        customDialog.setContentView(mView);
        customDialog.setCancelable(true);
        TextView pd_title = mView.findViewById(R.id.pd_title);
        TextView okBtn = mView.findViewById(R.id.ok_btn);
        TextView tvGameOverHead = mView.findViewById(R.id.tvGameOverHead);
        tvGameOverHead.setText("Game Over");
        okBtn.setText("Play Again");
        pd_title.setText(Message);
        okBtn.setOnClickListener(v -> customDialog.dismiss());
        customDialog.show();
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    private class JavaScriptInterface {
        private final Context context;

        JavaScriptInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void gameOver(String message) {
            checkScore(message);
        }
    }
}