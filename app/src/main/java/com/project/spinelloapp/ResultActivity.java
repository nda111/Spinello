package com.project.spinelloapp;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_result);


        String text = "전달 받은 값은";
        int id = 0;

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            text = "값을 전달 받는데 문제 발생";
        }
        else
            id = extras.getInt("notificationId");

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(text + " " + id);

        NotificationManager notificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //노티피케이션 제거
        notificationManager.cancel(id);
    }
}