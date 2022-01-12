package com.example.eee3545_e3_1105;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CamActivity_1 extends AppCompatActivity implements View.OnClickListener{
    static int Image_Capture_Code = 1;
    String mCurrentPhotoPath;
    String save_name = "Playerimg" + "1"; // Player number. 최소 3글자 이상으로 해야함
    Uri uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_2);

        Button btn_run = (Button)findViewById(R.id.button_run);
        Button btn_cam = (Button)findViewById(R.id.button_camera);
        Button btn_send = (Button)findViewById(R.id.button_send);
        Button btn_res1 = (Button)findViewById(R.id.button_gores1);
        ImageView imageView = (ImageView)findViewById(R.id.image_camera);

        btn_run.setOnClickListener(this);
        btn_cam.setOnClickListener(v -> captureCamera());
        btn_send.setOnClickListener(this);
        btn_res1.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_run:   // 도망가기; 첫 화면으로
                finish();
                break;

            case R.id.button_send:   // 사진을 서버 통해 전송
                /********************************
                 *  서버를 통해 사진을 전송하는 코드 *
                 ********************************/
                // 여기서 uri를 쓸 수 있음
                // 두썸딩









                /********************************/
                break;
            case R.id.button_gores1:
                // result 화면으로 넘어가는 코드
                finish();
                Intent intent_result1 = new Intent (this, ResultActivity_1.class);
                startActivity(intent_result1);
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

}

