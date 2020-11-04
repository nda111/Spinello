package com.project.spinelloapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

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

        pref = getSharedPreferences("IP", 0);
        edit = pref.edit();

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (address.getText().toString().length() != 0) {
                    edit.clear();
                    edit.putString("IP", address.getText().toString());
                    edit.apply();
                    Toast.makeText(AddressActivity.this, pref.getString("IP", String.valueOf(0)).toString() + "을 입력받았습니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(AddressActivity.this, MainActivity.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(AddressActivity.this, "IP 주소를 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                }



            }


        });

    }

}
