package com.example.mateusz.komunikacjasieciowa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mateusz.komunikacjasieciowa.ProgressInfo.StatusType;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText editTextAddress;
    private ProgressBar progressBar;
    private Button buttonDownloadInformation;
    private Button buttonDownloadFile;
    public TextView textViewFileDownloadValue;
    public TextView textViewFileSizeValue;
    public TextView textViewFileTypeValue;
    private String address;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            ProgressInfo obj = bundle.getParcelable(DownloadFileTask.INFO);
            ProgressInfo.StatusType statusType = obj.getResult();
            if (statusType == StatusType.ERROR) {
                progressBar.setProgress(0);
                textViewFileSizeValue.setText(" ");
                textViewFileTypeValue.setText(" ");
                textViewFileDownloadValue.setText(" ");
                Toast.makeText(MainActivity.this, "Niepoprawny URL!", Toast.LENGTH_SHORT).show();
            } else if (statusType == StatusType.FINISH) {
                Toast.makeText(MainActivity.this, "Ukończono pobieranie", Toast.LENGTH_SHORT).show();
            } else {
                double x = (double) obj.getDownloadedByte();
                double y = (double) obj.getSize();
                double result = (y / x) * 100;
                int progress = (int) result;
                String text = String.valueOf((int) y);
                progressBar.setProgress(progress);
                textViewFileDownloadValue.setText(text);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(
                DownloadFileTask.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextAddress = (EditText) findViewById(R.id.editTextAddress);
        textViewFileSizeValue = (TextView) findViewById(R.id.textViewFileSizeValue);
        textViewFileTypeValue = (TextView) findViewById(R.id.textViewFileTypeValue);
        textViewFileDownloadValue = (TextView) findViewById(R.id.textViewFileDownloadValue);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        buttonDownloadInformation = (Button) findViewById(R.id.buttonDownloadInformation);
        buttonDownloadFile = (Button) findViewById(R.id.buttonDownloadFile);

        buttonDownloadInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                address = editTextAddress.getText().toString();

                if (address.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Empty field!", Toast.LENGTH_SHORT).show();
                    address = null;
                } else {
                    DownloadInformationTask task = new DownloadInformationTask();
                    task.execute(address);
                }
            }
        });

        buttonDownloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                address = editTextAddress.getText().toString();

                if (address.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Empty field!", Toast.LENGTH_SHORT).show();
                    address = null;
                } else {
                    DownloadFileTask.start(MainActivity.this, address);
                }
            }
        });
    }

    private class DownloadInformationTask extends AsyncTask<String, Void, InfoFile> {

        private InfoFile infoFile = new InfoFile();
        private boolean canConnect;

        @Override
        protected InfoFile doInBackground(String... params) {
            Log.d("Connection ", "start");
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[0]);
                Log.d("Connection ", "URL " + url.toString());
                connection = (HttpURLConnection) url.openConnection();
                int statusCode = connection.getResponseCode();
                if (statusCode == 200) {
                    canConnect = true;
                    //connection.setDoOutput(true);
                    //connection.setRequestMethod("GET");
                    Log.d("Connection ", "connection: " + connection.toString());
                    Log.d("Connection ", "size: " + connection.getContentLength());
                    Log.d("Connection ", "type: " + connection.getContentType());
                    long sizeFile = connection.getContentLength();
                    infoFile.setType(connection.getContentType());
                    infoFile.setSize(sizeFile);
                } else {
                    canConnect = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
            }

            Log.d("Connection ", "close");

            return infoFile;
        }

        protected void onPostExecute(InfoFile infoFile) {
            if (canConnect == true) {
                textViewFileSizeValue.setText(infoFile.getSize().toString());
                textViewFileTypeValue.setText(infoFile.getType());
            } else {
                Toast.makeText(MainActivity.this, "Niepoprawny URL!", Toast.LENGTH_SHORT).show();
                textViewFileSizeValue.setText(" ");
                textViewFileTypeValue.setText(" ");
                progressBar.setProgress(0);
            }
        }
    }
}
