package com.inuker.bluetooth.newcode;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.inuker.bluetooth.MyApplication;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.ConstantsClassic;
import com.inuker.bluetooth.library.beacon.BluetoothDataParserImpl;
import com.inuker.bluetooth.library.beacon.CommandResult;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.response.ClassicResponse;
import com.inuker.bluetooth.library.receiver.listener.BluetoothBondListener;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ByteUtils;

import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

/**
 * Created by dingjikerbo on 2016/8/27.
 */
public class BTClientManager implements SearchResponse, ClassicResponse {

    private final static String TAG = BTClientManager.class.getName();

    private String mBtName;


    private static BluetoothClient mClient;

    private static BTClientManager mClientManager;

    private Context mContext;

    private AlertDialog mConAlertDialog;
    private AlertDialog mScannerDialog;

    private BluetoothDevice mDevice;
    private BluetoothDataParserImpl mBluetoothDataParserImpl;

    private CommandResultCallback mCommandResultCallback;

    private SendCommandCallback mSendCommandCallback;

    private ConnectStatusCallback mConnectStatusCallback;

    public void setCommandResultCallback(CommandResultCallback commandResultCallback) {
        this.mCommandResultCallback = commandResultCallback;

    }

    public void setSendCommandCallback(SendCommandCallback sendCommandCallback) {
        this.mSendCommandCallback = sendCommandCallback;

    }

    public void setConnectStatusCallback(ConnectStatusCallback connectStatusCallback) {
        this.mConnectStatusCallback = connectStatusCallback;

    }

    private BTClientManager(Context context) {
        this.mContext = context;
        mBluetoothDataParserImpl = new BluetoothDataParserImpl(null);

    }

    private BluetoothClient getClient() {
        if (mClient == null) {
            synchronized (BTClientManager.class) {
                if (mClient == null) {
                    mClient = new BluetoothClient(MyApplication.getInstance());
                }
            }
        }
        return mClient;
    }

    public static BTClientManager getInstance(Context context) {
        if (null == mClientManager) {
            mClientManager = new BTClientManager(context);
        }
        return mClientManager;
    }

    public void onScanner(String btName) {
        this.mBtName = btName;
        if (!getClient().isBluetoothOpened()) {
            getClient().openBluetooth();
            getClient().registerBluetoothStateListener(new BluetoothStateListener() {
                @Override
                public void onBluetoothStateChanged(boolean openOrClosed) {
                    if (openOrClosed) {
                        onScanner(mBtName);
                    } else {
                        showErrorDialog("蓝牙未开启", "请开启手机蓝牙重试", 3);
                    }

                }
            });

            return;
        }
        if (null != mContext) {
            showScannerDialog();
            searchDevice();
        }


    }

    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
//                .searchBluetoothLeDevice(5000, 2)// // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000, 2) // 再扫经典蓝牙5s
                .build();

        getClient().search(request, this);
    }


    public void onCreateConnect() {
        getClient().stopSearch();
        if (null != mDevice) {
            getClient().registerConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
            getClient().registerClassicConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
        }
        connectDevice();

    }

    public void onPause() {

    }

    public void onDisconnect() {
        getClient().stopSearch();
        getClient().disconnectClassic();
//        mBtName = null;
//        mContext = null;
//        mDevice = null;
//        mClientManager = null;
//        mBtName = null;
//        mClientManager = null;
//        mClient = null;
//        mContext = null;
//        mScannerDialog = null;
//        mConAlertDialog = null;
//        mBluetoothDataParserImpl = null;
//        mDevice = null;
//        mCommandResultCallback = null;
//        mSendCommandCallback = null;
//        mConnectStatusCallback = null;
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
        mScannerDialog.dismiss();
        showErrorDialog("蓝牙扫描失败", "蓝牙扫描失败，请检查设备是否通电", 2);
    }

    @Override
    public void onSearchCanceled() {
        mScannerDialog.dismiss();

    }


    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));
            boolean connected = (status == STATUS_CONNECTED);
            if (null != mConnectStatusCallback) {
                mConnectStatusCallback.onConnectStatus(mac, status);
            }


        }
    };

    private final BluetoothBondListener mBluetoothBondListener = new BluetoothBondListener() {
        @Override
        public void onBondStateChanged(String mac, int bondState) {
            // bondState = Constants.BOND_NONE, BOND_BONDING, BOND_BONDED
        }
    };


    private void connectDevice() {
        getClient().readClassic(this);
        showConnectDialog();
        getClient().disconnectClassic();
        if (null == mDevice) {
            return;
        }
        getClient().registerBluetoothBondListener(mBluetoothBondListener);
        getClient().connectClassic(mDevice.getAddress(), new ClassicResponse() {
            @Override
            public void onResponse(int code, Object data) {
                if (code == ConstantsClassic.CLASSIC_CON_SECCESS) {
                    sendByteData((byte) 0x31, new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,}, 0);
                } else {
                    if (null != mConAlertDialog && mConAlertDialog.isShowing()) {
                        mConAlertDialog.dismiss();
                    }
                    showErrorDialog("蓝牙连接失败", "蓝牙连接失败，是否重新连接", 1);
                }
            }
        });
    }

    private void showScannerDialog() {
        if (null == mScannerDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("发现蓝牙")
                    .setCancelable(true)
                    .setMessage("正在扫描蓝牙中，请稍等...");
            mScannerDialog = builder.create();
        }
        if (null != mScannerDialog && !mScannerDialog.isShowing()) {
            mScannerDialog.show();
        }
    }


    private void showConnectDialog() {
        if (null == mConAlertDialog) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("连接蓝牙")
                    .setCancelable(true)
                    .setMessage("蓝牙连接中，请稍等...");
            mConAlertDialog = builder.create();
        }
        if (!mConAlertDialog.isShowing()) {
            mConAlertDialog.show();
        }
    }

    private void showErrorDialog(String title, String msg, final int tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title)
                .setCancelable(false)
                .setMessage(msg)
                .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (tag) {
                            case 1:
                                connectDevice();
                                break;
                            case 2:
                                if (null != mBtName) {
                                    onScanner(mBtName);
                                }
                                break;
                            case 3:
                                if (null != mBtName) {
                                    onScanner(mBtName);
                                }
                                break;
                        }
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
        getClient().writeClassic(bytes, new ClassicResponse() {
            @Override
            public void onResponse(int code, Object data) {
                if (code == ConstantsClassic.MESSAGE_WRITE) {
                    String s = ByteUtils.byteToString((byte[]) data);
                    Log.i(TAG, "writeClassic data = " + s);
                    if (null != mSendCommandCallback) {
                        mSendCommandCallback.onSendData(true, (byte[]) data);
                    }
                } else {
                    Log.i(TAG, "writeClassic data failed ,bt not connected ");
                    if (null != mSendCommandCallback) {
                        mSendCommandCallback.onSendData(false, (byte[]) data);
                    }
                }
            }
        });
    }


    @Override
    public void onResponse(int code, Object data) {
        if (code == ConstantsClassic.MESSAGE_READ) {
            byte[] bytes = (byte[]) data;
            String hexStr = ByteUtils.byteToString(bytes);
            final CommandResult commandResult = mBluetoothDataParserImpl.parseFromBytes(bytes);
            if (null != commandResult) {
                if (commandResult.isResult()) {
                    if (commandResult.getType().code == CommandResult.CommandType.AUTH.code) {
                        if (null != mCommandResultCallback) {
                            mCommandResultCallback.onCommandResult(commandResult);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendByteData((byte) 0x32, commandResult.getSecondCode(), 0);
                            }
                        }, 500);


                    } else if (commandResult.getType().code == CommandResult.CommandType.SECOND_AUTH.code) {
                        if (null != mConAlertDialog && mConAlertDialog.isShowing()) {
                            mConAlertDialog.dismiss();
                        }
                        if (null != mCommandResultCallback) {
                            mCommandResultCallback.onCommandResult(commandResult);
                        }

                    } else {
                        if (null != mCommandResultCallback) {
                            mCommandResultCallback.onCommandResult(commandResult);
                        }
                    }
                } else {
                    if (commandResult.getType().code != CommandResult.CommandType.ILLEGAL_DATA.code) {
                        if (commandResult.getType().code == CommandResult.CommandType.AUTH.code) {
                            if (null != mCommandResultCallback) {
                                mCommandResultCallback.onCommandResult(commandResult);
                            }
                            showErrorDialog("蓝牙连接失败", "蓝牙连接失败，是否重新连接", 1);
                            Toast.makeText(mContext, commandResult.getDesc(), Toast.LENGTH_SHORT).show();
                        } else if (commandResult.getType().code == CommandResult.CommandType.SECOND_AUTH.code) {
                            if (null != mCommandResultCallback) {
                                mCommandResultCallback.onCommandResult(commandResult);
                            }
                            showErrorDialog("蓝牙连接失败", "蓝牙连接失败，是否重新连接", 1);
                            Toast.makeText(mContext, commandResult.getDesc(), Toast.LENGTH_SHORT).show();
                        } else {
                            if (null != mCommandResultCallback) {
                                mCommandResultCallback.onCommandResult(commandResult);
                            }
                        }

                    } else {
                        if (null != mCommandResultCallback) {
                            mCommandResultCallback.onCommandResult(commandResult);
                        }
                    }
                }

            }


        }
    }

    public void onDestroy() {
        onDisconnect();
        if (null != mDevice) {
            getClient().unregisterConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
        }
        getClient().unregisterBluetoothBondListener(mBluetoothBondListener);

        mBtName = null;
        mContext = null;
        mDevice = null;
        mClientManager = null;
        mBtName = null;
        mClientManager = null;
        mClient = null;
        mContext = null;
        mScannerDialog = null;
        mConAlertDialog = null;
        mBluetoothDataParserImpl = null;
        mDevice = null;
        mCommandResultCallback = null;
        mSendCommandCallback = null;
        mConnectStatusCallback = null;
    }


}
