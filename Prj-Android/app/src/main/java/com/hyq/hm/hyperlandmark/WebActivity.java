package com.hyq.hm.hyperlandmark;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.ar.sceneform.samples.hellosceneform.R;

public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_web);

        TextView textView = findViewById(R.id.testText);
        textView.setText("Just Test");
        textView.setTextColor(Color.parseColor("#FF0000"));
        textView.setTextSize(20);
    }

}
