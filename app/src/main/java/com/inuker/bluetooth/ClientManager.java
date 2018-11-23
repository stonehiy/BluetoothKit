package com.inuker.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.ConstantsClassic;
import com.inuker.bluetooth.library.beacon.BluetoothDataParserImpl;
import com.inuker.bluetooth.library.beacon.CommandResult;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.response.ClassicResponse;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ByteUtils;

import java.util.concurrent.ConcurrentLinkedDeque;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

/**
 * Created by dingjikerbo on 2016/8/27.
 */
public class ClientManager implements SearchResponse, ClassicResponse {

    private final static String TAG = ClientManager.class.getName();

    private String mBtName;

    private boolean mConnected;

    private static BluetoothClient mClient;

    private static ClientManager mClientManager;

    private Context mContext;

    private AlertDialog mConAlertDialog;
    private AlertDialog mScannerDialog;

    private BluetoothDevice mDevice;
    private BluetoothDataParserImpl mBluetoothDataParserImpl;

    private ClientManager(Context context) {
        this.mContext = context;
        mBluetoothDataParserImpl = new BluetoothDataParserImpl(null);

    }

    public static BluetoothClient getClient() {
        if (mClient == null) {
            synchronized (ClientManager.class) {
                if (mClient == null) {
                    mClient = new BluetoothClient(MyApplication.getInstance());
                }
            }
        }
        return mClient;
    }

    public static ClientManager getInstance(Context context) {
        if (null == mClientManager) {
            mClientManager = new ClientManager(context);
        }
        return mClientManager;
    }

    public void onScanner(String btName) {
        this.mBtName = btName;
        showScannerDialog();
        searchDevice();
        ClientManager.getClient().registerBluetoothStateListener(new BluetoothStateListener() {
            @Override
            public void onBluetoothStateChanged(boolean openOrClosed) {
                BluetoothLog.v(String.format("onBluetoothStateChanged %b", openOrClosed));

            }
        });

    }

    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
//                .searchBluetoothLeDevice(5000, 2)// // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000, 2) // 再扫经典蓝牙5s
                .build();

        ClientManager.getClient().search(request, this);
    }


    public void onCreateConnect() {
        ClientManager.getClient().stopSearch();
        if (null != mDevice) {
            ClientManager.getClient().registerConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
        }
        connectDeviceIfNeeded();

    }

    public void onPause() {

    }

    public void onDestroy() {
        ClientManager.getClient().stopSearch();
        ClientManager.getClient().disconnectClassic();
        if (null != mDevice) {
            ClientManager.getClient().unregisterConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
        }
        mBtName = null;
        mClientManager = null;
        mClient = null;
        mContext = null;
        mScannerDialog = null;
        mConAlertDialog = null;
        mBluetoothDataParserImpl = null;
    }


    @Override
    public void onSearchStarted() {

    }

    @Override
    public void onDeviceFounded(SearchResult searchResult) {
        if (null == searchResult) {
            return;
        }
        BluetoothDevice device = searchResult.device;
        if (null == device) {
            return;
        }
        if (mBtName.equals(device.getName())) {
            mDevice = device;
            mScannerDialog.dismiss();
            onCreateConnect();
        }

    }

    @Override
    public void onSearchStopped() {

    }

    @Override
    public void onSearchCanceled() {

    }


    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));
            mConnected = (status == STATUS_CONNECTED);
            connectDeviceIfNeeded();
        }
    };

    private void connectDeviceIfNeeded() {
        if (!mConnected) {
            connectDevice();
        }
    }

    private void connectDevice() {
        ClientManager.getClient().readClassic(this);
        showConnectDialog();
        ClientManager.getClient().disconnectClassic();
        if (null == mDevice) {
            return;
        }
        ClientManager.getClient().connectClassic(mDevice.getAddress(), new ClassicResponse() {
            @Override
            public void onResponse(int code, Object data) {
                if (code == ConstantsClassic.CLASSIC_CON_SECCESS) {
                    sendByteData((byte) 0x31, new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,}, 0);
                } else {
                    showErrorDialog();
                }
            }
        });
    }

    private void showScannerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("发现蓝牙")
                .setCancelable(false)
                .setMessage("正在扫描蓝牙中，请稍等...");
        mScannerDialog = builder.create();
        mScannerDialog.show();
    }


    private void showConnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("连接蓝牙")
                .setCancelable(false)
                .setMessage("蓝牙连接中，请稍等...");
        mConAlertDialog = builder.create();
        mConAlertDialog.show();
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("蓝牙连接失败")
                .setCancelable(false)
                .setMessage("蓝牙连接失败，是否重新连接")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        connectDeviceIfNeeded();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        builder.create().show();
    }


    public void sendByteData(byte command, byte[] params, int serialNum) {
        byte[] bytes = mBluetoothDataParserImpl.toBytes(command, params, serialNum);
        ClientManager.getClient().writeClassic(bytes, new ClassicResponse() {
            @Override
            public void onResponse(int code, Object data) {
                if (code == ConstantsClassic.MESSAGE_WRITE) {
                    String s = ByteUtils.byteToString((byte[]) data);
                    Log.i(TAG, "writeClassic data = " + s);
                } else {
                    Log.i(TAG, "writeClassic data failed ,bt not connected ");
                }
            }
        });
    }


    @Override
    public void onResponse(int code, Object data) {
        if (code == ConstantsClassic.MESSAGE_READ) {
            byte[] bytes = (byte[]) data;
            String hexStr = ByteUtils.byteToString(bytes);
            CommandResult commandResult = mBluetoothDataParserImpl.parseFromBytes(bytes);
            if (null != commandResult) {
                if (commandResult.isResult()) {
                    if (commandResult.getType().code == CommandResult.CommandType.AUTH.code) {
                        sendByteData((byte) 0x32, commandResult.getSecondCode(), 0);
                    } else if (commandResult.getType().code == CommandResult.CommandType.SECOND_AUTH.code) {
                        if (null != mConAlertDialog && mConAlertDialog.isShowing()) {
                            mConAlertDialog.dismiss();
                        }
                        Toast.makeText(mContext, "蓝牙连接成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, commandResult.getDesc(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (commandResult.getType().code == CommandResult.CommandType.AUTH.code) {
                        showErrorDialog();
                        Toast.makeText(mContext, commandResult.getDesc(), Toast.LENGTH_SHORT).show();
                    } else if (commandResult.getType().code == CommandResult.CommandType.SECOND_AUTH.code) {
                        showErrorDialog();
                        Toast.makeText(mContext, commandResult.getDesc(), Toast.LENGTH_SHORT).show();
                    } else if (commandResult.getType().code == CommandResult.CommandType.CHARGE_START_RES.code) {
                        Toast.makeText(mContext, commandResult.getDesc(), Toast.LENGTH_SHORT).show();
                    } else if (commandResult.getType().code == CommandResult.CommandType.CHARGE_STOP_RES.code) {
                        Toast.makeText(mContext, commandResult.getDesc(), Toast.LENGTH_SHORT).show();
                    } else if (commandResult.getType().code == CommandResult.CommandType.OFF_CHARGE_START.code) {
                        Toast.makeText(mContext, commandResult.getDesc(), Toast.LENGTH_SHORT).show();
                    } else if (commandResult.getType().code == CommandResult.CommandType.UNOFF_CHARGE_START.code) {
                        Toast.makeText(mContext, commandResult.getDesc(), Toast.LENGTH_SHORT).show();
                    } else if (commandResult.getType().code == CommandResult.CommandType.SET_PILE_NO.code) {
                        Toast.makeText(mContext, commandResult.getDesc(), Toast.LENGTH_SHORT).show();
                    }
                }

            }


        }
    }
}
