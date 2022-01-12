package com.example.eee3545_e3_1105;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity_1 extends AppCompatActivity implements View.OnClickListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result1);

        Button btn_run = (Button)findViewById(R.id.button_run1);
        Button btn_next = (Button)findViewById(R.id.button_next1);
        btn_run.setOnClickListener(this);
        btn_next.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_run1:
                finish();
                break;
            case R.id.button_next1:
                Intent intent_cam1 = new Intent(this, CamActivity_1.class);
                startActivity(intent_cam1);
                break;
        }
    }
}