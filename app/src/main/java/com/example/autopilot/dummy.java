//package com.example.autopilot;
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.media.MediaRecorder;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.ParcelFileDescriptor;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//public class MainActivity extends AppCompatActivity {
//
//    private MediaRecorder mediaRecorder;
//    private static final int REQUEST_PERMISSION_CODE = 200;
//    private static final int REQUEST_STORAGE_PERMISSION = 201;
//    private static final int REQUEST_CREATE_DOCUMENT = 1;
//    private Uri audioFileUri = null;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//
//        Button startButton = findViewById(R.id.startButton);
//        Button stopButton = findViewById(R.id.stopButton);
//        stopButton.setEnabled(false);
//    }
//
//    public void turnOnMicrophone(View view) {
//        if (checkPermissions()) {
//            if (checkStoragePermissions()) {
//                startRecording();
//            } else {
//                requestStoragePermissions();
//            }
//        } else {
//            requestPermissions();
//        }
//    }
//
//    public void turnOffMicrophone(View view) {
//        Button startButton = findViewById(R.id.startButton);
//        startButton.setEnabled(true);
//        stopRecording();
//    }
//
//    private void startRecording() {
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
//        String audioFileName = "AudioRecording_" + timeStamp + ".3gp";
//
//        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("audio/3gpp");
//        intent.putExtra(Intent.EXTRA_TITLE, audioFileName);
//
//        startActivityForResult(intent, REQUEST_CREATE_DOCUMENT);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        super.onActivityResult(requestCode, resultCode, resultData);
//        if (requestCode == REQUEST_CREATE_DOCUMENT && resultCode == Activity.RESULT_OK) {
//            if (resultData != null) {
//                audioFileUri = resultData.getData();
//                startRecordingWithUri();
//            }
//        }
//    }
//
//    private void startRecordingWithUri() {
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//        try {
//            ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(audioFileUri, "w");
//            if (pfd != null) {
//                mediaRecorder.setOutputFile(pfd.getFileDescriptor());
//                mediaRecorder.prepare();
//                mediaRecorder.start();
//
//                Button startButton = findViewById(R.id.startButton);
//                startButton.setEnabled(false);
//                Button stopButton = findViewById(R.id.stopButton);
//                stopButton.setEnabled(true);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void stopRecording(){
//        if (mediaRecorder != null) {
//            mediaRecorder.stop();
//            mediaRecorder.release();
//            mediaRecorder = null;
//
//            Button startButton = findViewById(R.id.startButton);
//            startButton.setEnabled(true);
//            Button stopButton = findViewById(R.id.stopButton);
//            stopButton.setEnabled(false);
//        }
//    }
//
//    private boolean checkPermissions() {
//        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
//        return result == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestPermissions() {
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
//    }
//
//    private boolean checkStoragePermissions() {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//            return true;
//        } else {
//            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            return result == PackageManager.PERMISSION_GRANTED;
//        }
//    }
//
//    private void requestStoragePermissions() {
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                turnOnMicrophone(null);
//            } else {
//                Log.d("myTag", "Audio Permission Denied");
//            }
//        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                turnOnMicrophone(null);
//            } else {
//                Log.d("myTag", "Storage Permission Denied");
//            }
//        }
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        stopRecording();
//    }
//}