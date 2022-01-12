package com.example.eee3545_e3_1105;


import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ResultActivity_2 extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "ResultActivity_2";
    String RESULT_URL = "http://192.168.0.17:5050/result";
    String save_name = "Resultimg";
    String mResultImagePath;
    private ProgressDialog pBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result2);

        Button btn_run = (Button) findViewById(R.id.button_run2);
        Button btn_next = (Button) findViewById(R.id.button_next2);
        Button btn_show_res = (Button) findViewById(R.id.button_show_res);
        btn_run.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        btn_show_res.setOnClickListener(this);

        try {
            File dir = getCacheDir();
            File tempfile = File.createTempFile(save_name, ".jpg", dir); //getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            mResultImagePath = tempfile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Result mResult = new Result();

        pBar = new ProgressDialog(ResultActivity_2.this);
        pBar.setMessage("Loading..");
        pBar.setTitle("Get Data");
        pBar.setIndeterminate(true);
        pBar.setCancelable(false);
        pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pBar.show();

        mResult.start();
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_run2:
                finish();
                break;
            case R.id.button_next2:
                finish();
                Intent intent_cam2 = new Intent(this, CamActivity_2.class);
                startActivity(intent_cam2);
                break;
            case R.id.button_show_res:
                ImageView imageView = (ImageView)findViewById(R.id.imageView_res); // 결과 출력 위한 imageView
                File bitmapFile = new File(mResultImagePath);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                            Uri.fromFile(bitmapFile));
                }
                catch(IOException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Exception", Toast.LENGTH_SHORT).show();
                }
                imageView.setImageBitmap(bitmap);
        }
    }

    public class Result extends Thread{  // 다운로드 작업을 위한 스레드입니다.
        @Override
        public void run() {
            try {
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                @SuppressWarnings("PointlessArithmeticExpression")
                int maxBufferSize = 1 * 1024 * 1024;

                java.net.URL url = new URL(RESULT_URL);
                Log.d(TAG, "url " + url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // Allow Inputs &amp; Outputs.
                connection.setDoInput(true);
                connection.setUseCaches(false);
                connection.setChunkedStreamingMode(0);
                connection.setConnectTimeout(10000);

                // Set HTTP method to POST.
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "image/jpg");
                connection.connect();
                Log.d(TAG, "111111111");
//                int serverResponseCode = connection.getResponseCode();
//                String serverResponseMessage = connection.getResponseMessage();
//                Log.d("serverResponseCode", "" + serverResponseCode);
//                Log.d("serverResponseMessage", "" + serverResponseMessage);

                InputStream is = connection.getInputStream();
                Log.d(TAG, "222222222");
                FileOutputStream outputStream = new FileOutputStream(mResultImagePath);
                bufferSize = 8192;
                buffer = new byte[bufferSize];
                while ((bytesRead = is.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                is.close();

                outputStream.flush();
                outputStream.close();
                connection.disconnect();

                ResultActivity_2.this.runOnUiThread(new Runnable() {
                    public void run() {
                      Toast.makeText(ResultActivity_2.this, "received result", Toast.LENGTH_SHORT).show();
                    }
                });
//                if (serverResponseCode == 200) {
//                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
//                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Something went wrong.");
            }
            pBar.dismiss();
        }
    }
}