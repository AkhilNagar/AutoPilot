package com.example.autopilot;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 200;
    private static final int SAMPLE_RATE = 44100; // Sample rate in Hz
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private AudioRecord audioRecord;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setEnabled(false);
    }

    public void turnOnMicrophone(View view) {
        if (checkPermissions()) {
            startRecording();
        } else {
            requestPermissions();
        }
    }

    public void turnOffMicrophone(View view) {
        if (isRecording) {
            stopRecording();
        }
    }

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);

        audioRecord.startRecording();
        isRecording = true;

        Button startButton = findViewById(R.id.startButton);
        startButton.setEnabled(false);
        Button stopButton = findViewById(R.id.stopButton);
        stopButton.setEnabled(true);

        // Start a new thread to capture audio
        new Thread(() -> {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];

            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    outputStream.write(buffer, 0, read);
                }
            }

            // Send the audio data to the server
            sendAudioToServer(outputStream.toByteArray());
        }).start();
    }

    private void stopRecording() {
        if (audioRecord != null) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;

            Button startButton = findViewById(R.id.startButton);
            startButton.setEnabled(true);
            Button stopButton = findViewById(R.id.stopButton);
            stopButton.setEnabled(false);
        }
    }

    private void sendAudioToServer(byte[] audioData) {
        new Thread(() -> {
            try {
                byte[] wavData = convertToWav(audioData);
                //Log.d("myTag", "Audioo: " + Arrays.toString(audioData));
                URL url = new URL("http://10.0.2.2:5000/upload"); // Replace with your API endpoint
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/octet-stream");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(wavData);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Handle success
                } else {
                    // Handle error
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean checkPermissions() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                turnOnMicrophone(null);
            } else {
                Log.d("myTag", "Audio Permission Denied");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRecording();
    }




    private byte[] convertToWav(byte[] pcmData) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            // WAV header
            int sampleRate = 44100; // Example sample rate
            int numChannels = 1; // Mono
            int bitsPerSample = 16;
            int byteRate = sampleRate * numChannels * bitsPerSample / 8;

            // Write WAV header
            outputStream.write("RIFF".getBytes());
            outputStream.write(intToByteArray(pcmData.length + 36));
            outputStream.write("WAVE".getBytes());
            outputStream.write("fmt ".getBytes());
            outputStream.write(intToByteArray(16)); // Subchunk1Size for PCM
            outputStream.write(shortToByteArray((short) 1)); // AudioFormat (PCM)
            outputStream.write(shortToByteArray((short) numChannels));
            outputStream.write(intToByteArray(sampleRate));
            outputStream.write(intToByteArray(byteRate));
            outputStream.write(shortToByteArray((short) (numChannels * bitsPerSample / 8))); // BlockAlign
            outputStream.write(shortToByteArray((short) bitsPerSample)); // BitsPerSample
            outputStream.write("data".getBytes());
            outputStream.write(intToByteArray(pcmData.length));

            // Write PCM data
            outputStream.write(pcmData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    private byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value & 0xff),
                (byte)((value >> 8) & 0xff),
                (byte)((value >> 16) & 0xff),
                (byte)((value >> 24) & 0xff)
        };
    }

    private byte[] shortToByteArray(short value) {
        return new byte[] {
                (byte)(value & 0xff),
                (byte)((value >> 8) & 0xff)
        };
    }
}
