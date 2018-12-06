package com.inuker.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.Constants;
import com.inuker.bluetooth.library.beacon.CommandResult;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.utils.ByteUtils;
import com.inuker.bluetooth.newcode.BTClientManager;
import com.inuker.bluetooth.newcode.CommandResultCallback;
import com.inuker.bluetooth.newcode.ConnectStatusCallback;
import com.inuker.bluetooth.newcode.SendCommandCallback;


public class ClassicProductionActivity extends AppCompatActivity implements View.OnClickListener,
        CommandResultCallback,
        SendCommandCallback,
        ConnectStatusCallback {
    private final static String TAG = ClassicProductionActivity.class.getName();

    private ListView mConversationView;

    private ArrayAdapter<String> mConversationArrayAdapter;
    private SearchResult device;
    private CommandResult mCommandResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classic_production);


        Intent intent = getIntent();
        device = intent.getParcelableExtra("SearchResult");

        mConversationView = (ListView) findViewById(R.id.in);
        TextView title = findViewById(R.id.title);
        title.setText(device.getName());
        Button btnFirstAuth = (Button) findViewById(R.id.btnFirstAuth);
        Button btnSecondAuth = (Button) findViewById(R.id.btnSecondAuth);
        Button btnChargeStart = (Button) findViewById(R.id.btnChargeStart);
        Button btnChargeStop = (Button) findViewById(R.id.btnChargeStop);
        Button btnOffChargeStart = (Button) findViewById(R.id.btnOffChargeStart);
        Button btnUnoffChargeStart = (Button) findViewById(R.id.btnUnoffChargeStart);
        Button btnPileNo = (Button) findViewById(R.id.btnPileNo);
        Button btnClose = (Button) findViewById(R.id.btnClose);
        Button btnRecon = (Button) findViewById(R.id.btnRecon);
        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnFirstAuth.setOnClickListener(this);
        btnSecondAuth.setOnClickListener(this);
        btnChargeStart.setOnClickListener(this);
        btnChargeStop.setOnClickListener(this);
        btnOffChargeStart.setOnClickListener(this);
        btnUnoffChargeStart.setOnClickListener(this);
        btnPileNo.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnRecon.setOnClickListener(this);
        btnClear.setOnClickListener(this);


        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);

        BTClientManager.getInstance(this).onScanner(device.getName());
        BTClientManager.getInstance(this).setCommandResultCallback(this);
        BTClientManager.getInstance(this).setSendCommandCallback(this);
        BTClientManager.getInstance(this).setConnectStatusCallback(this);

    }


    @Override
    public void onClick(View v) {

        if (NoFastClickUtils.isFastClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btnFirstAuth) {
        } else if (i == R.id.btnSecondAuth) {
        } else if (i == R.id.btnChargeStart) {
            byte[] parms = new byte[]{(byte) 0xA1};
            BTClientManager.getInstance(this).sendByteData((byte) 0x10, parms, 0);
        } else if (i == R.id.btnChargeStop) {
            byte[] parms = new byte[]{(byte) 0xA2};
            BTClientManager.getInstance(this).sendByteData((byte) 0x10, parms, 0);
        } else if (i == R.id.btnOffChargeStart) {
            byte[] parms = new byte[]{(byte) 0xA3};
            BTClientManager.getInstance(this).sendByteData((byte) 0x10, parms, 0);
        } else if (i == R.id.btnUnoffChargeStart) {
            byte[] parms = new byte[]{(byte) 0xA4};
            BTClientManager.getInstance(this).sendByteData((byte) 0x10, parms, 0);
        } else if (i == R.id.btnPileNo) {
            showSetPileNo();
        } else if (i == R.id.btnClose) {
            BTClientManager.getInstance(this).onDisconnect();
        } else if (i == R.id.btnRecon) {
            BTClientManager.getInstance(this).onScanner(device.getName());
        } else if (i == R.id.btnClear) {
            mConversationArrayAdapter.clear();
            mConversationArrayAdapter.notifyDataSetChanged();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        BTClientManager.getInstance(this).onDestroy();
    }


    /**
     * 设置桩号
     */
    private void showSetPileNo() {
        // 创建对话框构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 获取布局
        View view2 = View.inflate(this, R.layout.dialog_set_pile_no, null);
        // 获取布局中的控件
        final EditText etPileNo = (EditText) view2.findViewById(R.id.etPileNo);
        final Button btnConfirm = (Button) view2.findViewById(R.id.btnConfirm);
        final Button btnCancel = (Button) view2.findViewById(R.id.btnCancel);
        // 创建对话框
        final AlertDialog alertDialog = builder
                .setCancelable(false)
                .setView(view2)
                .create();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pileNo = etPileNo.getText().toString().trim();
                if (null == pileNo) {
                    Toast.makeText(ClassicProductionActivity.this, "请输入16位数字桩号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (16 != pileNo.length()) {
                    Toast.makeText(ClassicProductionActivity.this, "请输入16位数字桩号", Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] b1 = new byte[]{(byte) 0xA5};
                byte[] b2 = ByteUtils.stringToBytes(pileNo);
                byte[] bytes = byteMerger(b1, b2);
                BTClientManager.getInstance(ClassicProductionActivity.this).sendByteData((byte) 0x10, bytes, 0);
                alertDialog.dismiss();

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }


    /**
     * byte[] 合并
     * System.arraycopy()方法
     *
     * @param bt1
     * @param bt2
     * @return
     */
    public static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }


    @Override
    public void onCommandResult(CommandResult commandResult) {
        String resultData = ByteUtils.byteToString(commandResult.getResultBytes());
        mConversationArrayAdapter.add("received command:" + resultData);
        mConversationArrayAdapter.add("received desc:" + commandResult.getDesc());
        mConversationArrayAdapter.notifyDataSetChanged();


    }

    @Override
    public void onSendData(boolean isSuccess, byte[] data) {
        String sendData = ByteUtils.byteToString(data);
        if (isSuccess) {
            mConversationArrayAdapter.add("send success:" + sendData);
        } else {
            mConversationArrayAdapter.add("send fail:" + sendData + "->需要重新连接蓝牙重试");
        }
        mConversationArrayAdapter.notifyDataSetChanged();

    }

    @Override
    public void onConnectStatus(String mac, int status) {
        if (status == Constants.STATUS_CONNECTED) {
            mConversationArrayAdapter.add("connect " + mac + " success:->正在首次鉴权...");
        } else {
            mConversationArrayAdapter.add("disconnect " + mac + " :->蓝牙连接断开");
        }

    }
}
