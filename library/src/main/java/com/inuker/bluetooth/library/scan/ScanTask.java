package com.inuker.bluetooth.library.scan;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dingjikerbo on 2016/8/28.
 */
public class ScanTask implements Parcelable {

    private int searchType;
    private int searchDuration;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.searchType);
        dest.writeInt(this.searchDuration);
    }

    public ScanTask() {
    }

    public int getSearchType() {
        return searchType;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    public int getSearchDuration() {
        return searchDuration;
    }

    public void setSearchDuration(int searchDuration) {
        this.searchDuration = searchDuration;
    }

    protected ScanTask(Parcel in) {
        this.searchType = in.readInt();
        this.searchDuration = in.readInt();
    }

    public static final Creator<ScanTask> CREATOR = new Creator<ScanTask>() {
        public ScanTask createFromParcel(Parcel source) {
            return new ScanTask(source);
        }

        public ScanTask[] newArray(int size) {
            return new ScanTask[size];
        }
    };
}
