package com.inuker.bluetooth.library.scan;

import android.os.Parcel;
import android.os.Parcelable;

import com.inuker.bluetooth.library.utils.BluetoothUtils;

import java.util.ArrayList;
import java.util.List;

import static com.inuker.bluetooth.library.Constants.SEARCH_TYPE_BLE;
import static com.inuker.bluetooth.library.Constants.SEARCH_TYPE_CLASSIC;

/**
 * Created by dingjikerbo on 2016/8/28.
 */
public class ScanRequest implements Parcelable {

    private List<ScanTask> tasks;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(tasks);
    }

    public ScanRequest() {
    }

    protected ScanRequest(Parcel in) {
        this.tasks = new ArrayList<ScanTask>();
        in.readTypedList(this.tasks, ScanTask.CREATOR);
    }

    public static final Creator<ScanRequest> CREATOR = new Creator<ScanRequest>() {
        public ScanRequest createFromParcel(Parcel source) {
            return new ScanRequest(source);
        }

        public ScanRequest[] newArray(int size) {
            return new ScanRequest[size];
        }
    };

    public List<ScanTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<ScanTask> tasks) {
        this.tasks = tasks;
    }

    public static class Builder {
        private List<ScanTask> tasks;

        public Builder() {
            tasks = new ArrayList<ScanTask>();
        }

        public Builder searchBluetoothLeDevice(int duration) {
            if (BluetoothUtils.isBleSupported()) {
                ScanTask search = new ScanTask();
                search.setSearchType(SEARCH_TYPE_BLE);
                search.setSearchDuration(duration);
                tasks.add(search);
            }
            return this;
        }

        public Builder searchBluetoothLeDevice(int duration, int times) {
            for (int i = 0; i < times; i++) {
                searchBluetoothLeDevice(duration);
            }
            return this;
        }

        public Builder searchBluetoothClassicDevice(int duration) {
            ScanTask search = new ScanTask();
            search.setSearchType(SEARCH_TYPE_CLASSIC);
            search.setSearchDuration(duration);
            tasks.add(search);
            return this;
        }

        public Builder searchBluetoothClassicDevice(int duration, int times) {
            for (int i = 0; i < times; i++) {
                searchBluetoothClassicDevice(duration);
            }
            return this;
        }

        public ScanRequest build() {
            ScanRequest group = new ScanRequest();
            group.setTasks(tasks);
            return group;
        }
    }
}
