package com.cd.statussaver.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.cd.statussaver.R;
import com.cd.statussaver.adapter.FBStoriesListAdapter;
import com.cd.statussaver.adapter.FbStoryUserListAdapter;
import com.cd.statussaver.api.CommonClassForAPI;
import com.cd.statussaver.databinding.ActivityInstagramBinding;
import com.cd.statussaver.interfaces.UserListInterface;
import com.cd.statussaver.model.FBStoryModel.EdgesModel;
import com.cd.statussaver.model.FBStoryModel.MediaModel;
import com.cd.statussaver.model.FBStoryModel.NodeModel;
import com.cd.statussaver.model.story.TrayModel;
import com.cd.statussaver.util.AdsUtils;
import com.cd.statussaver.util.SharePrefs;
import com.cd.statussaver.util.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static android.content.ContentValues.TAG;
import static com.cd.statussaver.util.Utils.RootDirectoryFacebook;
import static com.cd.statussaver.util.Utils.createFileFolder;
import static com.cd.statussaver.util.Utils.startDownload;


public class FacebookActivity extends AppCompatActivity implements UserListInterface {
    ActivityInstagramBinding binding;
    FacebookActivity activity;
    CommonClassForAPI commonClassForAPI;
    private String videoUrl;
    private ClipboardManager clipBoard;
    ArrayList<NodeModel> edgeModelList;


    private UnifiedNativeAd nativeAd;
    private String strName = "facebook";
    private String strNameSecond = "fb";
    FbStoryUserListAdapter fbStoryUserListAdapter;
    FBStoriesListAdapter fbStoriesListAdapter;
    private RewardedAd rewardedAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_instagram);
        activity = this;




        commonClassForAPI = CommonClassForAPI.getInstance(activity);
        createFileFolder();
        initViews();

        AdsUtils.showGoogleBannerAd(activity,binding.adView);
        rewardedVideoAdsINIT();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity = this;
        assert activity != null;
        clipBoard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
        pasteText();
    }



    private void initViews() {
        clipBoard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
        binding.tvAppName.setText(activity.getResources().getString(R.string.facebook_app_name));

        binding.TVTitle.setImageResource(R.drawable.fb);
        binding.tvLoginInstagram.setText(getResources().getString(R.string.download_stories));
        binding.imBack.setOnClickListener(view -> onBackPressed());

        binding.tvPaste.setOnClickListener(view -> {
            pasteText();
        });

        binding.tvAppName.setText(activity.getResources().getString(R.string.facebook_app_name));

        binding.imInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);




            }
        });


        Glide.with(activity)
                .load(R.drawable.fb1)
                .into(binding.layoutHowTo.imHowto1);

        Glide.with(activity)
                .load(R.drawable.innn)
                .into(binding.layoutHowTo.imHowto2);

        Glide.with(activity)
                .load(R.drawable.fb3)
                .into(binding.layoutHowTo.imHowto3);

        Glide.with(activity)
                .load(R.drawable.fb4)
                .into(binding.layoutHowTo.imHowto4);


        binding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.opn_fb));
        binding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.opn_fb));
        if (!SharePrefs.getInstance(activity).getBoolean(SharePrefs.ISSHOWHOWTOFB)) {
            SharePrefs.getInstance(activity).putBoolean(SharePrefs.ISSHOWHOWTOFB,true);
            binding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        }else {
            binding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }

        binding.loginBtn1.setOnClickListener(v -> {
            String ll = binding.etText.getText().toString();
            if (ll.equals("")) {
                Utils.setToast(activity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(ll).matches()) {
                Utils.setToast(activity, getResources().getString(R.string.enter_valid_url));
            } else {
                getFacebookData();
                showRewardedVideoAds();
            }

            showRewardedVideoAds();
        });
        binding.TVTitle.setOnClickListener(v -> Utils.OpenApp(activity, "com.facebook.katana"));

        GridLayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        binding.RVUserList.setLayoutManager(mLayoutManager);
        binding.RVUserList.setNestedScrollingEnabled(false);
        mLayoutManager.setOrientation(RecyclerView.HORIZONTAL);


        edgeModelList = new ArrayList<>();
        fbStoryUserListAdapter = new FbStoryUserListAdapter(activity, edgeModelList, FacebookActivity.this);
        binding.RVUserList.setAdapter(fbStoryUserListAdapter);


        if (SharePrefs.getInstance(activity).getBoolean(SharePrefs.ISFBLOGIN)) {
            layoutCondition();
            getFacebookUserData();
            binding.SwitchLogin.setChecked(true);
        }else {
            binding.SwitchLogin.setChecked(false);
        }

        binding.RLLoginInstagram.setOnClickListener(v -> binding.tvLogin.performClick());

        binding.tvLogin.setOnClickListener(v -> {
            if (!SharePrefs.getInstance(activity).getBoolean(SharePrefs.ISFBLOGIN)) {
                Intent intent = new Intent(activity,
                        FBLoginActivity.class);
                startActivityForResult(intent, 100);
            }else {
                AlertDialog.Builder ab = new AlertDialog.Builder(activity);
                ab.setPositiveButton(getResources().getString(R.string.yes), (dialog, id) -> {
                    SharePrefs.getInstance(activity).putBoolean(SharePrefs.ISFBLOGIN, false);
                    SharePrefs.getInstance(activity).putString(SharePrefs.FBKEY, "");
                    SharePrefs.getInstance(activity).putString(SharePrefs.FBCOOKIES, "");
                    if (SharePrefs.getInstance(activity).getBoolean(SharePrefs.ISFBLOGIN)){
                        binding.SwitchLogin.setChecked(true);
                    }else {
                        binding.SwitchLogin.setChecked(false);
                        binding.RVUserList.setVisibility(View.GONE);
                        binding.RVStories.setVisibility(View.GONE);
                        binding.tvLogin.setVisibility(View.VISIBLE);
                        binding.tvViewStories.setText(activity.getResources().getText(R.string.view_stories));
                    }
                    dialog.cancel();

                });
                ab.setNegativeButton(getResources().getString(R.string.cancel), (dialog, id) -> dialog.cancel());
                AlertDialog alert = ab.create();
                alert.setTitle(getResources().getString(R.string.do_u_want_to_download_media_from_pvt));
                alert.show();
            }

        });
        GridLayoutManager mLayoutManager1 = new GridLayoutManager(getApplicationContext(), 2);
        binding.RVStories.setLayoutManager(mLayoutManager1);
        binding.RVStories.setNestedScrollingEnabled(false);
        mLayoutManager1.setOrientation(RecyclerView.VERTICAL);
    }
    public void layoutCondition(){
        binding.tvViewStories.setText(activity.getResources().getString(R.string.stories));
        binding.tvLogin.setVisibility(View.GONE);

    }


    private void getFacebookData() {
        try {
            createFileFolder();
            URL url = new URL(binding.etText.getText().toString());
            String host = url.getHost();
            if (host.contains(strName)||host.contains(strNameSecond)) {
                Utils.showProgressDialog(activity);
                new CallGetFacebookData().execute(binding.etText.getText().toString());
                showRewardedVideoAds();
            } else {
                Utils.setToast(activity, getResources().getString(R.string.enter_valid_url));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pasteText() {
        try {
            binding.etText.setText("");
            String copyIntent = getIntent().getStringExtra("CopyIntent");
            copyIntent= MainActivity.extractLinks(copyIntent);
            if (copyIntent== null || copyIntent.equals("")) {
                if (!(clipBoard.hasPrimaryClip())) {
                    Log.d(TAG, "PasteText");
                } else if (!(clipBoard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                    if (clipBoard.getPrimaryClip().getItemAt(0).getText().toString().contains(strName)||clipBoard.getPrimaryClip().getItemAt(0).getText().toString().contains(strNameSecond)) {
                        binding.etText.setText(clipBoard.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains(strName)||item.getText().toString().contains(strNameSecond)) {
                        binding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (copyIntent.contains(strName)||copyIntent.contains(strNameSecond)) {
                    binding.etText.setText(copyIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void userListClick(int position, TrayModel trayModel) {

    }


    @Override
    public void fbUserListClick(int position, NodeModel trayModel) {
        getFacebookUserStories(trayModel.getNodeDataModel().getId());
    }

    class CallGetFacebookData extends AsyncTask<String, Void, Document> {
        Document facebookDoc;

        @Override
        protected Document doInBackground(String... urls) {
            try {
                facebookDoc = Jsoup.connect(urls[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: Error");
            }
            return facebookDoc;
        }

        @Override
        protected void onPostExecute(Document result) {
            Utils.hideProgressDialog(activity);
            try {
                videoUrl = result.select("meta[property=\"og:video\"]").last().attr("content");
                if (!videoUrl.equals("")) {
                    startDownload(videoUrl, RootDirectoryFacebook, activity, "facebook_"+ System.currentTimeMillis()+".mp4");
                    videoUrl = "";
                    showRewardedVideoAds();
                    binding.etText.setText("");
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 100 && resultCode == RESULT_OK) {
                if (SharePrefs.getInstance(activity).getBoolean(SharePrefs.ISFBLOGIN)){
                    binding.SwitchLogin.setChecked(true);
                    layoutCondition();
                    getFacebookUserData();
                } else {
                    binding.SwitchLogin.setChecked(false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void getFacebookUserData(){
        binding.prLoadingBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post("https://www.facebook.com/api/graphql/")
                .addHeaders("accept-language", "en,en-US;q=0.9,fr;q=0.8,ar;q=0.7")
                .addHeaders("cookie", SharePrefs.getInstance(activity).getString(SharePrefs.FBCOOKIES))
                .addHeaders(
                        "user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36"
                )
                .addHeaders("Content-Type", "application/json")
                .addBodyParameter("fb_dtsg", SharePrefs.getInstance(activity).getString(SharePrefs.FBKEY))
                .addBodyParameter(
                        "variables",
                        "{\"bucketsCount\":200,\"initialBucketID\":null,\"pinnedIDs\":[\"\"],\"scale\":3}"
                )
                .addBodyParameter("doc_id", "2893638314007950")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("JsonResp- "+response);

                        if(response!=null){
                            try {
                                JSONObject unified_stories_buckets = response.getJSONObject("data").getJSONObject("me").getJSONObject("unified_stories_buckets");

                                Gson gson = new Gson();
                                Type listType = new TypeToken<EdgesModel>() {
                                }.getType();
                                EdgesModel edgesModelNew = gson.fromJson(String.valueOf(unified_stories_buckets), listType);

                                if (edgesModelNew.getEdgeModel().size()>0){
                                    edgeModelList.clear();
                                    edgeModelList.addAll(edgesModelNew.getEdgeModel());
                                    fbStoryUserListAdapter.notifyDataSetChanged();
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        binding.RVUserList.setVisibility(View.VISIBLE);
                        binding.prLoadingBar.setVisibility(View.GONE);

                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.prLoadingBar.setVisibility(View.GONE);

                    }
                });
    }
    public void getFacebookUserStories(String userId){
        binding.prLoadingBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post("https://www.facebook.com/api/graphql/")
                .addHeaders("accept-language", "en,en-US;q=0.9,fr;q=0.8,ar;q=0.7")
                .addHeaders("cookie", SharePrefs.getInstance(activity).getString(SharePrefs.FBCOOKIES))
                .addHeaders(
                        "user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36"
                )
                .addHeaders("Content-Type", "application/json")
                .addBodyParameter("fb_dtsg", SharePrefs.getInstance(activity).getString(SharePrefs.FBKEY))
                .addBodyParameter(
                        "variables",
                        "{\"bucketID\":\""+userId+"\",\"initialBucketID\":\""+userId+"\",\"initialLoad\":false,\"scale\":5}"
                )
                .addBodyParameter("doc_id", "2558148157622405")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println("JsonResp- "+response);
                        binding.prLoadingBar.setVisibility(View.GONE);
                        if(response!=null){
                            try {
                                JSONObject edges = response.getJSONObject("data").getJSONObject("bucket").getJSONObject("unified_stories");
                                Gson gson = new Gson();
                                Type listType = new TypeToken<EdgesModel>() {
                                }.getType();
                                EdgesModel edgesModelNew = gson.fromJson(String.valueOf(edges), listType);
                                ArrayList<MediaModel> attachmentsList = edgesModelNew.getEdgeModel().get(0).getNodeDataModel().getAttachmentsList();
                                fbStoriesListAdapter = new FBStoriesListAdapter(activity, edgesModelNew.getEdgeModel());
                                binding.RVStories.setAdapter(fbStoriesListAdapter);
                                fbStoriesListAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        binding.prLoadingBar.setVisibility(View.GONE);
                    }
                });
    }

    //RewardedAds : Start
    public void rewardedVideoAdsINIT() {
        rewardedAd = new RewardedAd(this,
                getResources().getString(R.string.admob_rewareded_ad));

        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.

                Log.d(TAG, "onRewardedAdLoaded: ");

            }

            @Override
            public void onRewardedAdFailedToLoad(int i) {
                Log.d(TAG, "onRewardedAdFailedToLoad: "+i);
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
    }

    private void showRewardedVideoAds() {
        if (rewardedAd.isLoaded()) {

            RewardedAdCallback adCallback = new RewardedAdCallback() {
                @Override
                public void onRewardedAdOpened() {
                    // Ad opened.
                }

                @Override
                public void onRewardedAdClosed() {
                    // Ad closed.
                }

                @Override
                public void onUserEarnedReward(@NonNull RewardItem reward) {
                    // User earned reward.
                }

            };
            rewardedAd.show(activity, adCallback);
        } else {
            Log.d("TAG", "The rewarded ad wasn't loaded yet.");
        }
    }

    //RewardedAds : End

}