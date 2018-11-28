package com.inuker.bluetooth;

import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.inuker.bluetooth.net.ApiUrl;
import com.inuker.bluetooth.net.GsonUtil;
import com.inuker.bluetooth.net.OkHttp3Util;
import com.inuker.bluetooth.net.entity.BaseEntity;
import com.inuker.bluetooth.net.entity.EpileOpenAsk;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ApiActivity extends AppCompatActivity {

    private final static String TAG = ApiActivity.class.getName();

    private AlertDialog mAlertDialog;
    private EpileOpenAsk mEpileOpenAsk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api);
        showConnectDialog();
    }


    public void clickStart(View view) {
        showSetPileNo();
    }

    public void clickStop(View view) {
        if (null == mEpileOpenAsk) {
            return;
        }
        stopCharge(mEpileOpenAsk.e_charge_request_id, mEpileOpenAsk.membercode);

    }


    private void showConnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("获取数据")
                .setCancelable(true)
                .setMessage("获取数据中，请稍等...");
        mAlertDialog = builder.create();
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
                .setTitle("开启充电")
                .setCancelable(false)
                .setView(view2)
                .create();
        alertDialog.show();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pileNo = etPileNo.getText().toString().trim();
                if (TextUtils.isEmpty(pileNo)) {
                    Toast.makeText(ApiActivity.this, "请输入16位数字桩号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (16 != pileNo.length()) {
                    Toast.makeText(ApiActivity.this, "请输入16位数字桩号", Toast.LENGTH_SHORT).show();
                    return;
                }
                startCharge(pileNo);
                alertDialog.dismiss();

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    /**
     * 开启充电
     *
     * @param pileNo
     */
    private void startCharge(String pileNo) {
        showNetDialog();
        Map<String, Object> map = new HashMap<>();
        map.put("controlType_in", "04");
        map.put("epilecode_in", pileNo);
        map.put("epilepoint_in", "00");
        map.put("eusercardNo_in", "86888888888888888888");
        map.put("fixTime_in", "00000000");
        map.put("limitdata_in", "");

        OkHttp3Util.doGet(ApiUrl.API_CHARGE_START, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                hideNetDialog();
                Toast.makeText(ApiActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                hideNetDialog();
                String s = response.body().string();
                BaseEntity baseEntity = GsonUtil.gsonToBean(s, BaseEntity.class);
                if (baseEntity.ok()) {
                    showThreadToast("开启充电成功");
                    Log.i(TAG, "baseEntity.data.toString() = " + baseEntity.data);
                    mEpileOpenAsk = GsonUtil.gsonToBean(new Gson().toJson(baseEntity.data), EpileOpenAsk.class);
                } else {
                    showThreadToast(baseEntity.message);
                }

            }
        });

    }


    /**
     * 关闭充电
     *
     * @param rid
     * @param membercode
     */
    private void stopCharge(String rid, String membercode) {
        showNetDialog();
        //string rid, string membercode
        Map<String, Object> map = new HashMap<>();
        map.put("rid", rid);
        map.put("membercode", membercode);

        OkHttp3Util.doGet(ApiUrl.API_CHARGE_STOP, map, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                hideNetDialog();
                Toast.makeText(ApiActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                hideNetDialog();
                String s = response.body().string();
                BaseEntity baseEntity = GsonUtil.gsonToBean(s, BaseEntity.class);
                if (baseEntity.ok()) {
                    showThreadToast("关闭充电成功");
                } else {
                    showThreadToast(baseEntity.message);
                }

                Log.i(TAG, "response.body().toString() = " + s);

            }
        });

    }

    private void showNetDialog() {
        if (null != mAlertDialog && !mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }

    }

    private void hideNetDialog() {
        if (null != mAlertDialog && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }

    }

    /**
     * 在子线程里弹出土司
     *
     * @param msg
     */
    private void showThreadToast(String msg) {
        Looper.prepare();
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
        Looper.loop();
    }

}
