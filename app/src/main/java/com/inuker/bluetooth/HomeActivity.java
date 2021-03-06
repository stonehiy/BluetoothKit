package com.inuker.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.inuker.bluetooth.library.beacon.BluetoothDataParserImpl;
import com.inuker.bluetooth.library.beacon.CommandResult;
import com.inuker.bluetooth.library.utils.ByteUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.functions.Consumer;

public class HomeActivity extends AppCompatActivity {

    private boolean hasLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getRxPermissions();
//        test();
//        test1();
    }

    public void test() {
        String str = "640C11A1010755000000005555EF";
        BluetoothDataParserImpl bluetoothDataParser = new BluetoothDataParserImpl(null);
        CommandResult cr = bluetoothDataParser.parseFromBytes(ByteUtils.hexToByteArray(str));
        System.out.println("cr = " + cr);

    }

    public void test1() {
        String str = "1B58";
        String s = ByteUtils.byteToString(ByteUtils.hexToByteArray(str));
        System.out.println("test1 s = " + s);
    }

    public void clickApi(View view) {
        startActivity(new Intent(this, ApiActivity.class));
    }

    public void clickBluetooth(View view) {
        if (!hasLocation) {
            Toast.makeText(HomeActivity.this, "获取定位权限失败，请设置打开该权限", Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(new Intent(this, MainActivity.class));
    }


    @SuppressLint("CheckResult")
    private void getRxPermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        //同时请求多个权限
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)
                //多个权限用","隔开
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        hasLocation = granted;
                        if (granted) {

                        } else {
                            Toast.makeText(HomeActivity.this, "获取定位权限失败，请设置打开该权限", Toast.LENGTH_LONG).show();

                        }
                    }
                });


    }


}
