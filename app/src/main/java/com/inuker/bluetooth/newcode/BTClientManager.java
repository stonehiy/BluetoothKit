package com.inuker.bluetooth.newcode;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.inuker.bluetooth.ClientManager;
import com.inuker.bluetooth.MyApplication;
import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.ConstantsClassic;
import com.inuker.bluetooth.library.beacon.BluetoothDataParserImpl;
import com.inuker.bluetooth.library.beacon.CommandResult;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.connect.response.ClassicResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.receiver.listener.BluetoothBondListener;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ByteUtils;

import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
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

    private final static UUID BLE_WRITE_SERVICE_UUID = UUID.fromString("0000ffe5-0000-1000-8000-00805f9b34fb");
    private final static UUID BLE_WRITE_CHARACTER_UUID = UUID.fromString("0000ffe9-0000-1000-8000-00805f9b34fb");


    private final static UUID BLE_NOTIFY_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private final static UUID BLE_NOTIFY_CHARACTER_UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");


    private final static UUID BLE_DESCRIPTOR_UUID = UUID.fromString("000001d0-0000-1000-8000-00805f9b34fb");


    private String mBleType;


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


    /**
     * 有内存泄漏
     * 以后优化把static去掉
     *
     * @param context
     * @return
     */
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
        searchDevice();


    }

    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(5000, 2)// // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000, 2) // 再扫经典蓝牙5s
                .build();

        getClient().search(request, this);
    }


    public void onCreateConnect() {
        getClient().stopSearch();
        if (null != mDevice) {
            if ("3.0".equalsIgnoreCase(mBleType)) {
                getClient().registerClassicConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
            } else {
                getClient().registerConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
            }
        }
        connectDevice();

    }

    public void onPause() {

    }

    public void onDisconnect() {
        getClient().stopSearch();
        if (null != mDevice) {
            if ("3.0".equalsIgnoreCase(mBleType)) {
                getClient().disconnectClassic();
            } else {
                getClient().disconnect(mDevice.getAddress());
            }
        }
    }


    @Override
    public void onSearchStarted() {
        showScannerDialog();

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
            if (null != mScannerDialog && mScannerDialog.isShowing()) {
                mScannerDialog.dismiss();
            }
            String bleName = mDevice.getName();
            if (null != bleName) {
                String[] bleTypes = bleName.split("-");
                if (bleTypes.length == 3) {
                    mBleType = bleTypes[1];
                }
            }
            onCreateConnect();
        }

    }

    @Override
    public void onSearchStopped() {
        if (null != mScannerDialog && mScannerDialog.isShowing()) {
            mScannerDialog.dismiss();
        }
        showErrorDialog("蓝牙扫描失败", "蓝牙扫描失败，请检查设备是否通电", 2);
    }

    @Override
    public void onSearchCanceled() {
        if (null != mScannerDialog && mScannerDialog.isShowing()) {
            mScannerDialog.dismiss();
        }

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
        if (null == mDevice) {
            return;
        }
        showConnectDialog();
        getClient().registerBluetoothBondListener(mBluetoothBondListener);
        if ("3.0".equalsIgnoreCase(mBleType)) {
            getClient().readClassic(this);
            getClient().connectClassic(mDevice.getAddress(), new ClassicResponse() {
                @Override
                public void onResponse(int code, Object data) {
                    if (code == ConstantsClassic.CLASSIC_CON_SECCESS) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendByteData((byte) 0x31, new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,}, 0);
                            }
                        }, 1000);

                    } else {
                        if (null != mConAlertDialog && mConAlertDialog.isShowing()) {
                            mConAlertDialog.dismiss();
                        }
                        showErrorDialog("蓝牙连接失败", "蓝牙连接失败，是否重新连接", 1);
                    }
                }
            });
        } else {
            BleConnectOptions options = new BleConnectOptions.Builder()
                    .setConnectRetry(3)
                    .setConnectTimeout(20000)
                    .setServiceDiscoverRetry(3)
                    .setServiceDiscoverTimeout(10000)
                    .build();
            ClientManager.getClient().connect(mDevice.getAddress(), options, new BleConnectResponse() {
                @Override
                public void onResponse(int code, BleGattProfile profile) {
                    BluetoothLog.v(String.format("profile:\n%s", profile));
                    if (code == REQUEST_SUCCESS) {
                        String bleName = mDevice.getName();
                        if (null != bleName) {
                            String[] bleTypes = bleName.split("-");
                            if (bleTypes.length == 3) {
                                switch (bleTypes[1]) {
                                    case "BLE":
                                        getClient().notify(mDevice.getAddress(), BLE_NOTIFY_SERVICE_UUID, BLE_NOTIFY_CHARACTER_UUID, new BleNotifyResponse() {
                                            @Override
                                            public void onNotify(UUID service, UUID character, byte[] value) {
                                                String data = ByteUtils.byteToString((byte[]) value);
                                                BluetoothLog.v(String.format("BLE notify onNotify value:" + data));
                                                resultCallback(value);
                                            }

                                            @Override
                                            public void onResponse(int code) {
                                                BluetoothLog.v(String.format("BLE notify onResponse code:" + code));
                                            }
                                        });
                                        break;
                                    case "BLE20":
                                        getClient().notify(mDevice.getAddress(), BLE_NOTIFY_SERVICE_UUID, BLE_NOTIFY_SERVICE_UUID, new BleNotifyResponse() {
                                            @Override
                                            public void onNotify(UUID service, UUID character, byte[] value) {
                                                String data = ByteUtils.byteToString((byte[]) value);
                                                BluetoothLog.v(String.format("BLE20 notify onNotify value:" + data));
                                            }

                                            @Override
                                            public void onResponse(int code) {
                                                BluetoothLog.v(String.format("BLE2.0 notify onResponse code:" + code));
                                            }
                                        });


                                        break;
                                }
                            }
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendByteData((byte) 0x31, new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,}, 0);
                            }
                        }, 1000);

                    } else {
                        if (null != mConAlertDialog && mConAlertDialog.isShowing()) {
                            mConAlertDialog.dismiss();
                        }
                        showErrorDialog("蓝牙连接失败", "蓝牙连接失败，是否重新连接", 1);
                    }


                }
            });
        }


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
        if (null != mConAlertDialog && !mConAlertDialog.isShowing()) {
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


    /**
     * 发送蓝牙数据
     *
     * @param command
     * @param params
     * @param serialNum
     */
    public void sendByteData(byte command, byte[] params, int serialNum) {
        if (mDevice.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
            sendClassicByteData(command, params, serialNum);
        } else {
            sendBleByteData(command, params, serialNum);
        }
    }

    /**
     * 发送经典蓝牙数据
     *
     * @param command
     * @param params
     * @param serialNum
     */
    public void sendClassicByteData(byte command, byte[] params, int serialNum) {
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


    /**
     * 发送ble 数据，这里有两种蓝牙
     * 4g和mc2.0
     *
     * @param command
     * @param params
     * @param serialNum
     */
    public void sendBleByteData(byte command, byte[] params, int serialNum) {
        final byte[] bytes = mBluetoothDataParserImpl.toBytes(command, params, serialNum);
        final String data = ByteUtils.byteToString(bytes);
        String bleName = mDevice.getName();
        if (null != bleName) {
            String[] bleTypes = bleName.split("-");
            if (bleTypes.length == 3) {
                switch (bleTypes[1]) {
                    case "BLE"://4g
                        Log.i(TAG, "write BLE data = " + data);
                        getClient().write(mDevice.getAddress(), BLE_WRITE_SERVICE_UUID, BLE_WRITE_CHARACTER_UUID, bytes, new BleWriteResponse() {
                            @Override
                            public void onResponse(int code) {
                                if (0 == code) {
                                    if (null != mSendCommandCallback) {
                                        mSendCommandCallback.onSendData(true, bytes);
                                    }
                                } else {
                                    if (null != mSendCommandCallback) {
                                        mSendCommandCallback.onSendData(false, bytes);
                                    }
                                }
                            }
                        });
                        break;
                    case "BLE20"://mc20
                        Log.i(TAG, "write BLE20 data = " + data);
                        getClient().write(mDevice.getAddress(), BLE_NOTIFY_SERVICE_UUID, BLE_NOTIFY_CHARACTER_UUID, bytes, new BleWriteResponse() {
                            @Override
                            public void onResponse(int code) {
                                if (0 == code) {
                                    if (null != mSendCommandCallback) {
                                        mSendCommandCallback.onSendData(true, bytes);
                                    }
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            ble20Read();
                                        }
                                    }, 1000);
                                } else {
                                    if (null != mSendCommandCallback) {
                                        mSendCommandCallback.onSendData(false, bytes);
                                    }
                                }
                            }
                        });
                        break;
                }
            }
        }

    }

    public void ble20Read() {
        getClient().readDescriptor(mDevice.getAddress(), BLE_NOTIFY_SERVICE_UUID, BLE_NOTIFY_SERVICE_UUID,BLE_DESCRIPTOR_UUID, new BleReadResponse() {
            @Override
            public void onResponse(int code, byte[] data) {
                BluetoothLog.v(String.format("BLE20 read onNotify value:" + data));
                resultCallback(data);
            }
        });
    }


    @Override
    public void onResponse(int code, Object data) {
        if (code == ConstantsClassic.MESSAGE_READ) {
            byte[] bytes = (byte[]) data;
            resultCallback(bytes);
        }
    }


    /**
     * 消息接收处理
     *
     * @param bytes
     */
    public void resultCallback(byte[] bytes) {

        final CommandResult commandResult = mBluetoothDataParserImpl.parseFromBytes(bytes);
        if (null != commandResult) {
            if (commandResult.isResult()) {
                if (commandResult.getType().code == CommandResult.CommandType.AUTH.code) {
                    if (null != mConAlertDialog && mConAlertDialog.isShowing()) {
                        mConAlertDialog.dismiss();
                    }
                    if (null != mCommandResultCallback) {
                        mCommandResultCallback.onCommandResult(commandResult);
                    }
                } else if (commandResult.getType().code == CommandResult.CommandType.SECOND_AUTH.code) {

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

    public void onDestroy() {
        onDisconnect();
        if (null != mDevice) {
            if ("3.0".equalsIgnoreCase(mBleType)) {
                getClient().unregisterClassicConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
            } else {
                getClient().unregisterConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
            }
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
