package com.example.eee3545_e3_1105;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.provider.MediaStore;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.charset.Charset;

public class CamActivity_2 extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "CamActivity_1";
    String UPLOAD_URL = "http://192.168.0.17:5050/upload";
    String PROCEED_URL = "http://192.168.0.17:5050/proceed";
    String mCurrentPhotoPath;
    String save_name = "Playerimg"; // Player number. 최소 3글자 이상으로 해야함
    static int Image_Capture_Code = 1;
    private ProgressDialog pBar;
    Uri uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_2);

        Button btn_run = (Button)findViewById(R.id.button_run);
        Button btn_cam = (Button)findViewById(R.id.button_camera);
        Button btn_send = (Button)findViewById(R.id.button_send);
        Button btn_res2 = (Button)findViewById(R.id.button_gores1);
        ImageView imageView = (ImageView)findViewById(R.id.image_camera);

        pBar = new ProgressDialog(CamActivity_2.this);
        pBar.setMessage("Loading..");
        pBar.setTitle("Get Data");
        pBar.setIndeterminate(true);
        pBar.setCancelable(false);
        pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        btn_run.setOnClickListener(this);
        btn_cam.setOnClickListener(v -> captureCamera());
        btn_send.setOnClickListener(this);
        btn_res2.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_run:   // 도망가기; 첫 화면으로
                finish();
                break;

            case R.id.button_send:   // 사진을 서버 통해 전송
                UploadImage mUploadImage = new UploadImage();
                pBar.show();
                mUploadImage.start();
//                Toast.makeText(this, "upload", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_gores1:
                // result 화면으로 넘어가는 코드
//                finish();
//                Intent intent_result2 = new Intent (this, ResultActivity_2.class);
//                startActivity(intent_result2);
                Proceed mProceed = new Proceed();
                mProceed.start();
                break;
            default:
                break;
        }
    }
    private void captureCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String time = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss").format(new Date());
        File file = null; // 촬영한 사진을 저장할 파일
        File dir = getCacheDir();   //getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(!dir.exists()) if(!dir.mkdirs()){ Toast.makeText(this, "Cannot make dir", Toast.LENGTH_SHORT).show(); return; }


        try {
            File tempfile = File.createTempFile(save_name, ".jpg", dir); //getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            mCurrentPhotoPath = tempfile.getAbsolutePath();
            file = tempfile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);   ////////////// 저장될 경로 지정
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);  // putExtra: camera가 촬영한 사진을 저장하라는 명령. uri: 저장될 경로 지정

        startActivityForResult(intent, Image_Capture_Code);
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imageView = (ImageView)findViewById(R.id.image_camera);
        if (requestCode == Image_Capture_Code) {
            if (resultCode == RESULT_OK) {
                File bitmapFile = new File(mCurrentPhotoPath);
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
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class UploadImage extends Thread{  // 다운로드 작업을 위한 스레드입니다.

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

                java.net.URL url = new URL(UPLOAD_URL);
                Log.d(TAG, "url " + url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.connect();
                // Allow Inputs &amp; Outputs.
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setChunkedStreamingMode(0);
                connection.setConnectTimeout(10000);

                // Set HTTP method to POST.
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                FileInputStream fileInputStream;
                DataOutputStream outputStream;
                outputStream = new DataOutputStream(connection.getOutputStream());

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"filename\";filename=\"" + mCurrentPhotoPath + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);
                Log.d(TAG, "filename " + mCurrentPhotoPath);
                File file = new File(mCurrentPhotoPath);
                fileInputStream = new FileInputStream(file);
                Log.d(TAG, "Create FileInputStream");
                bytesAvailable = fileInputStream.available();
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bufferSize = 8192;
                Log.d(TAG, "bytes available: " + bytesAvailable);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                Log.d(TAG, "Read from FileInputStream");
                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
//                    bytesAvailable = fileInputStream.available();
//                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    Log.d(TAG, "Write buffer");
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                Log.d(TAG, "Write end lines");
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

                CamActivity_2.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(CamActivity_2.this, response, Toast.LENGTH_SHORT).show();
                    }
                });

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
                connection.disconnect();
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

    public class Proceed extends Thread{  // 다운로드 작업을 위한 스레드입니다.
        @Override
        public void run() {
            try {
                @SuppressWarnings("PointlessArithmeticExpression")
                java.net.URL url = new URL(PROCEED_URL);
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


                CamActivity_2.this.runOnUiThread(new Runnable() {
                    public void run() {
//                        TextView txt_textView = (TextView)findViewById(R.id.textView);
//                        txt_textView.setText(response);
                        if (!response.equals("proceed"))
                            Toast.makeText(CamActivity_2.this, response, Toast.LENGTH_SHORT).show();
                        else {
                            finish();
                            Intent intent_result2 = new Intent(CamActivity_2.this, ResultActivity_2.class);
                            startActivity(intent_result2);
                        }
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


