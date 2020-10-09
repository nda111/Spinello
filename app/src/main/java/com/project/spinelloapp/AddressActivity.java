package com.project.spinelloapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddressActivity extends AppCompatActivity {

    EditText address;
    Button connect;
    SharedPreferences pref;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        address = findViewById(R.id.address);
        connect = findViewById(R.id.btn_connect);

        pref=getSharedPreferences("IP",0);
        edit = pref.edit();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(address.getText().toString().length()!=0){
                    edit.clear();
                    edit.putString("IP",address.getText().toString());
                    edit.apply();
                    Toast.makeText(AddressActivity.this, pref.getString("IP", String.valueOf(0)).toString()+"에 연결되었습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddressActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(AddressActivity.this, "IP 주소를 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                }



            }
        });
    }
}