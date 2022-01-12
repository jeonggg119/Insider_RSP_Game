package com.example.eee3545_e3_1105;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    String LOGIN_URL = "http://192.168.0.17:5050/login";
    String MATCH_URL = "http://192.168.0.17:5050/match";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TextView txt_textView = (TextView)findViewById(R.id.textView);
        Button btn_setting = (Button)findViewById(R.id.button_setting);
        Button btn_center = (Button)findViewById(R.id.button_center);
        Button btn_play2 = (Button)findViewById(R.id.button_play2);

        btn_setting.setOnClickListener(this);
        btn_center.setOnClickListener(this);
        btn_play2.setOnClickListener(this);

        Login mLogin = new Login();
        mLogin.start();
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_setting:
                MatchOpponent mMatchOpponent = new MatchOpponent();
                mMatchOpponent.start();
                /********************************************************/
                break;
            case R.id.button_center:
               // txt_textView.setText("button_center");
                Intent intent_info = new Intent(this, ExplainActivity.class);
                startActivity(intent_info);
                //finish();
                break;
            case R.id.button_play2:
                //txt_textView.setText("button_play2");
                Intent intent_cam2 = new Intent(this, CamActivity_2.class);
                startActivity(intent_cam2);
                break;

        }
    }

    public class Login extends Thread{  // 다운로드 작업을 위한 스레드입니다.
        @Override
        public void run() {
            try {
                @SuppressWarnings("PointlessArithmeticExpression")
                java.net.URL url = new URL(LOGIN_URL);
                Log.d(TAG, "url " + url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.connect();
                // Allow Inputs &amp; Outputs.
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setChunkedStreamingMode(0);
                connection.setConnectTimeout(10000);

                // Set HTTP method to POST.
                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Content-Type", "application/json" + boundary);

                int serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();
                Log.d("serverResponseCode", "" + serverResponseCode);
                Log.d("serverResponseMessage", "" + serverResponseMessage);

                Charset charset = StandardCharsets.UTF_8;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset));
                String inputLine;
                StringBuffer sb = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                br.close();
                String response = sb.toString();
                Log.d("serverResponseString", "" + response);


                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
//                        TextView txt_textView = (TextView)findViewById(R.id.textView);
//                        txt_textView.setText(response);
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                });
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Something went wrong.");
            }
        }
    }

    public class MatchOpponent extends Thread{  // 다운로드 작업을 위한 스레드입니다.
        @Override
        public void run() {

            try {
                java.net.URL url = new URL(MATCH_URL);
                Log.d(TAG, "url " + url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.connect();
                // Allow Inputs &amp; Outputs.
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setChunkedStreamingMode(0);
                connection.setConnectTimeout(10000);

                // Set HTTP method to POST.
                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Content-Type", "application/json" + boundary);

                int serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();
                Log.d("serverResponseCode", "" + serverResponseCode);
                Log.d("serverResponseMessage", "" + serverResponseMessage);

                Charset charset = StandardCharsets.UTF_8;
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset));
                String inputLine;
                StringBuffer sb = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                br.close();
                String response = sb.toString();
                Log.d("serverResponseString", "" + response);


                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        TextView txt_textView = (TextView)findViewById(R.id.textView);
                        txt_textView.setText(response);
//                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                    }
                });
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Something went wrong.");
            }
        }
    }
}