package com.ramotion.circlemenu.example.simple;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ramotion.circlemenu.CircleMenuView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidliu on 2017/12/21.
 */

public class DynamicActivity extends AppCompatActivity {
    private static final String TAG = "DynamicActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup rootView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_dynamic, null);
        setContentView(rootView);
        List<Integer> icons = new ArrayList<>();
        icons.add(R.drawable.ic_home_white_24dp);
        icons.add(R.drawable.ic_notifications_white_24dp);
        icons.add(R.drawable.ic_place_white_24dp);
        icons.add(R.drawable.ic_search_white_24dp);
        icons.add(R.drawable.ic_settings_white_24dp);

        List<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.colorPrimary));
        colors.add(getResources().getColor(R.color.colorPrimary));
        colors.add(getResources().getColor(R.color.colorPrimary));
        colors.add(getResources().getColor(R.color.colorPrimary));
        colors.add(getResources().getColor(R.color.colorAccent));


        CircleMenuView menuView = new CircleMenuView(this, icons, colors);
        RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        rootView.addView(menuView,layoutParams);
        menuView.setEventListener(new CircleMenuView.EventListener() {
            @Override
            public void onButtonClickAnimationEnd(@NonNull CircleMenuView view, int index) {
                Log.d(TAG, "onButtonClickAnimationEnd| index: " + index);
                Intent intent = new Intent(view.getContext(),MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
