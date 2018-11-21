package com.inuker.bluetooth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.inuker.bluetooth.library.ConstantsClassic;
import com.inuker.bluetooth.library.connect.response.ClassicResponse;

public class ClassicStepActivity extends FragmentActivity implements View.OnClickListener {
    private final static String TAG = ClassicStepActivity.class.getName();

    private ListView mConversationView;

    private ArrayAdapter<String> mConversationArrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classic_step);
        mConversationView = (ListView) findViewById(R.id.in);
        Button btnFirstAuth = (Button) findViewById(R.id.btnFirstAuth);
        Button btnSecondAuth = (Button) findViewById(R.id.btnSecondAuth);
        Button btnChargeStart = (Button) findViewById(R.id.btnChargeStart);
        Button btnChargeStop = (Button) findViewById(R.id.btnChargeStop);
        Button btnOffChargeStart = (Button) findViewById(R.id.btnOffChargeStart);
        Button btnUnoffChargeStart = (Button) findViewById(R.id.btnUnoffChargeStart);
        Button btnClose = (Button) findViewById(R.id.btnClose);
        btnFirstAuth.setOnClickListener(this);
        btnSecondAuth.setOnClickListener(this);
        btnChargeStart.setOnClickListener(this);
        btnChargeStop.setOnClickListener(this);
        btnOffChargeStart.setOnClickListener(this);
        btnUnoffChargeStart.setOnClickListener(this);
        btnClose.setOnClickListener(this);


        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        mConversationView.setAdapter(mConversationArrayAdapter);

        ClientManager.getClient().readClassic(new ClassicResponse() {
            @Override
            public void onResponse(int code, Object data) {
                if (code == ConstantsClassic.MESSAGE_READ) {
                    Log.i(TAG, "readClassic data = " + data);

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnFirstAuth) {
            sendByteData((byte) 0x31, null, 0);
        } else if (i == R.id.btnSecondAuth) {
            sendByteData((byte) 0x32, null, 0);
        } else if (i == R.id.btnChargeStart) {
            byte[] parms = new byte[]{(byte) 0xA1};
            sendByteData((byte) 0x10, parms, 0);
        } else if (i == R.id.btnChargeStop) {
            byte[] parms = new byte[]{(byte) 0xA2};
            sendByteData((byte) 0x10, parms, 0);
        } else if (i == R.id.btnOffChargeStart) {
            byte[] parms = new byte[]{(byte) 0xA3};
            sendByteData((byte) 0x10, parms, 0);
        } else if (i == R.id.btnUnoffChargeStart) {
            byte[] parms = new byte[]{(byte) 0xA4};
            sendByteData((byte) 0x10, parms, 0);
        } else if (i == R.id.btnClose) {

        }

    }


    private void sendByteData(byte command, byte[] params, int serialNum) {
        ClientManager.getClient().writeClassic(new byte[]{command}, new ClassicResponse() {
            @Override
            public void onResponse(int code, Object data) {
                if (code == ConstantsClassic.MESSAGE_WRITE) {
                    Log.i(TAG, "writeClassic data = " + data);
                }
            }
        });


    }
}
