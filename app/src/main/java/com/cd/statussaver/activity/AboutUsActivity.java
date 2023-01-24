package com.cd.statussaver.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.cd.statussaver.util.AdsUtils;
import com.cd.statussaver.R;
import com.cd.statussaver.databinding.ActivityAboutUsBinding;
import static com.cd.statussaver.util.Utils.PrivacyPolicyUrl;

public class AboutUsActivity extends AppCompatActivity {

    ActivityAboutUsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about_us);
        AdsUtils.showGoogleBannerAd(AboutUsActivity.this,binding.adView);
        binding.RLPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i  = new Intent(AboutUsActivity.this,WebviewAcitivity.class);
                i.putExtra("URL",PrivacyPolicyUrl);
                i.putExtra("Title","Privacy Policy");
                startActivity(i);
            }
        });
        binding.imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}
