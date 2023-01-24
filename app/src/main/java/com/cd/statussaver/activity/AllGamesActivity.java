package com.cd.statussaver.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cd.statussaver.R;
import com.cd.statussaver.databinding.ActivityAllGamesBinding;

public class AllGamesActivity extends AppCompatActivity {
    ActivityAllGamesBinding binding;
    AllGamesActivity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_all_games);
        activity = this;
        initViews();
    }

    private void initViews() {
        binding.imBack.setOnClickListener(v -> onBackPressed());
        binding.RL2048.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, GamesPlayActivity.class);
                i.putExtra("url","2048");
                startActivity(i);
            }
        });
        binding.RLHelix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, GamesPlayActivity.class);
                i.putExtra("url","Helix");
                startActivity(i);
            }
        });
    }

}