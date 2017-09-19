package com.ramotion.circlemenu;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RippleEffectView view = (RippleEffectView) findViewById(R.id.ripple);
        final ObjectAnimator animation = ObjectAnimator.ofFloat(view, "radius", 700);
        animation.setStartDelay(1000);
        animation.setDuration(1000);
        animation.start();
    }
}
