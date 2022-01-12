package com.example.eee3545_e3_1105;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // TextView txt_textView = (TextView)findViewById(R.id.textView_announcer);
        Button btn_connect = (Button)findViewById(R.id.button_connect);
        Button btn_check = (Button)findViewById(R.id.button_check);

        btn_connect.setOnClickListener(this);
        btn_check.setOnClickListener(this);
    }

    public void onClick(View v) {
        TextView txt_textView = (TextView)findViewById(R.id.textView_announcer);
        switch(v.getId()){
            case R.id.button_connect:
                txt_textView.setText("button_setting pressed");
                break;
            case R.id.button_check:
                txt_textView.setText("button_setting 2pressed");
                // Intent intent = new Intent(this, MainActivity.class);
                // startActivity(intent);
                finish();
                break;
        }
    }
}