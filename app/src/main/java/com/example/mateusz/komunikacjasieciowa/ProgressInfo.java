package com.example.mateusz.komunikacjasieciowa;

import android.os.Parcel;
import android.os.Parcelable;

public class ProgressInfo implements Parcelable {

    private long downloadedByte;
    private long size;
    private static StatusType result;

    public static StatusType getResult() {
        return result;
    }

    public static void setResult(StatusType result) {
        ProgressInfo.result = result;
    }


    public enum StatusType {
        IN_PROGRESS,
        FINISH,
        ERROR
    }

    public ProgressInfo(long downloadedByte, long size, StatusType statusType) {
        this.downloadedByte = downloadedByte;
        this.size = size;
        this.result = statusType;
    }

    public ProgressInfo(Parcel in) {
        this.size = in.readLong();
        this.downloadedByte = in.readLong();
    }

    public static final Creator<ProgressInfo> CREATOR = new Creator<ProgressInfo>() {
        @Override
        public ProgressInfo createFromParcel(Parcel in) {
            return new ProgressInfo(in);
        }

        @Override
        public ProgressInfo[] newArray(int size) {
            return new ProgressInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(downloadedByte);
        dest.writeLong(size);
    }

    public long getDownloadedByte() {
        return downloadedByte;
    }

    public void setDownloadedByte(long downloadedByte) {
        this.downloadedByte = downloadedByte;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }


    @Override
    public String toString() {
        return "ProgressInfo{" +
                "downloadedByte=" + downloadedByte +
                ", size=" + size +
                ", result=" + result +
                '}';
    }
}
