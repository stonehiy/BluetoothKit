package com.inuker.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.scan.ScanResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dingjikerbo on 2016/9/1.
 */
public class DeviceListAdapter extends BaseAdapter implements Comparator<ScanResult> {

    private Context mContext;

    private List<ScanResult> mDataList;

    public DeviceListAdapter(Context context) {
        mContext = context;
        mDataList = new ArrayList<ScanResult>();
    }

    public void setDataList(List<ScanResult> datas) {
        mDataList.clear();
        mDataList.addAll(datas);
        Collections.sort(mDataList, this);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int compare(ScanResult lhs, ScanResult rhs) {
        return rhs.rssi - lhs.rssi;
    }

    private static class ViewHolder {
        TextView name;
        TextView mac;
        TextView rssi;
        TextView adv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.device_list_item, null, false);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.mac = (TextView) convertView.findViewById(R.id.mac);
            holder.rssi = (TextView) convertView.findViewById(R.id.rssi);
            holder.adv = (TextView) convertView.findViewById(R.id.adv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ScanResult result = (ScanResult) getItem(position);

        holder.name.setText(result.getName());
        holder.mac.setText(result.getAddress());
        holder.rssi.setText(String.format("Rssi: %d", result.rssi));

        Beacon beacon = new Beacon(result.scanRecord);
        holder.adv.setText(beacon.toString());

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(mContext, DeviceDetailActivity.class);
                intent.putExtra("mac", result.getAddress());
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }
}
