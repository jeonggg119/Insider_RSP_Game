package com.example.eee3545_e3_1105;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

public class ExplainActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explain);
        Button btn_back = (Button)findViewById(R.id.button_back);
        btn_back.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_back:
                finish();
                break;
        }
    }
}