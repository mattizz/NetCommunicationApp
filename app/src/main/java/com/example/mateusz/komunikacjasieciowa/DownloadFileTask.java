package com.example.mateusz.komunikacjasieciowa;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFileTask extends IntentService {

    private static final String ACTION_TASK = "action";
    private static final String PARAM = "addressURL";
    private static final int BLOCK_SIZE = 1024;
    private long downloadByte = 0;
    private long lengthFile = 0;
    private ProgressInfo.StatusType statusType;
    public static final String INFO = "progressInfo";
    public final static String NOTIFICATION = "com.example.mateusz.komunikacjasieciowa.MainActivity";

    public static void start(Context context, String address) {
        Intent intent = new Intent(context, DownloadFileTask.class);
        intent.setAction(ACTION_TASK);
        intent.putExtra(PARAM, address);
        context.startService(intent);
    }

    public DownloadFileTask() {
        super("DownloadFileTask");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TASK.equals(action)) {
                final String param = intent.getStringExtra(PARAM);
                doExercise(param);
            } else {
                Log.e("Intent_service", "nieznana akcja");
            }
        }
        Log.d("Intent_service", "usługa wykonała zadanie");
    }

    private void doExercise(String param) {
        Log.d("Connection ", "start");

        HttpURLConnection connection = null;
        FileOutputStream fileStream = null;
        InputStream input = null;

        try {
            URL url = new URL(param);
            connection = (HttpURLConnection) url.openConnection();
            int statusCode;
            try {
                statusCode = connection.getResponseCode();
            } catch (Exception ex) {
                statusCode = 404;
            }
            if (statusCode == 200) {
                statusType = ProgressInfo.StatusType.IN_PROGRESS;
                File file = new File(url.getFile());
                File outputFile = new File(
                        Environment.getExternalStorageDirectory() +
                                File.separator + file.getName());
                if (outputFile.exists()) outputFile.delete();
                DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
                fileStream = new FileOutputStream(outputFile.getPath());
                byte bufor[] = new byte[BLOCK_SIZE];
                int downloaded = dataInputStream.read(bufor, 0, BLOCK_SIZE);
                lengthFile = connection.getContentLength();
                while (downloaded != -1) {
                    fileStream.write(bufor, 0, downloaded);
                    downloadByte += downloaded;
                    downloaded = dataInputStream.read(bufor, 0, BLOCK_SIZE);
                    if (downloadByte == lengthFile) {
                        statusType = ProgressInfo.StatusType.FINISH;
                    }
                    sendMessage();
                }
            } else {
                statusType = ProgressInfo.StatusType.ERROR;
                sendMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    fileStream.close();
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) connection.disconnect();
        }

        Log.d("Connection ", "close");
    }

    public void sendMessage() {
        ProgressInfo value = new ProgressInfo(downloadByte, lengthFile, statusType);
        value.toString();
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(INFO, value);
        sendBroadcast(intent);
    }
}
