package com.inuker.bluetooth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void clickApi(View view) {
        startActivity(new Intent(this, ApiActivity.class));
    }

    public void clickBluetooth(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }


}
