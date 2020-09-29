package com.project.spinelloapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
    private int ival;
    public static int glob=1;
    RadioGroup transmission;
    RadioButton mid,hour;
    static SharedPreferences sPref;
    private SharedPreferences.Editor sE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        transmission = (RadioGroup) findViewById(R.id.transmission);
        mid = (RadioButton)findViewById(R.id.midnight);
        hour = (RadioButton)findViewById(R.id.hours);

        sPref = getSharedPreferences("Pref",0);
        sE = sPref.edit();
        ival = sPref.getInt("Pref", 0);

        if(ival == R.id.midnight){
            mid.setChecked(true);
            glob=1;
        }else if(ival == R.id.hours){
            hour.setChecked(true);
            glob=2;
        }

        transmission.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            int state = 0;
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.midnight){
                    sE.clear();
                    sE.putInt("Pref",checkedId);
                    sE.apply();
                }else if(checkedId == R.id.hours){
                    sE.clear();
                    sE.putInt("Pref",checkedId);
                    sE.apply();
                }
            }
        });

    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        sPref = getSharedPreferences("Pref",0);
        sE = sPref.edit();
        ival = sPref.getInt("Pref", 0);

        if(ival == R.id.midnight){
            mid.setChecked(true);
            glob=1;
        }else if(ival == R.id.hours){
            hour.setChecked(true);
            glob=2;
        }
    }

}