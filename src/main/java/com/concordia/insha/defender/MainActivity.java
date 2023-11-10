package com.concordia.insha.defender;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.concordia.insha.R;
import com.concordia.insha.defender.services.DefenderService;

public class MainActivity extends AppCompatActivity {
    public DefenderService mBoundService;

    Button start, stop, reset;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBoundService = ((DefenderService.LocalBinder) service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            mBoundService = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        reset = findViewById(R.id.reset);

        Intent intent = new Intent(MainActivity.this, DefenderService.class);
        bindService(intent, mConnection, Service.BIND_AUTO_CREATE);

        start.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mBoundService.startDetection();
                Toast.makeText(MainActivity.this, "Started IDS service",
                        Toast.LENGTH_SHORT).show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mBoundService.stopDetection();
                Toast.makeText(MainActivity.this, "Stopped IDS service",
                        Toast.LENGTH_SHORT).show();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mBoundService.stopDetection();
                mBoundService.resetData();

                Toast.makeText(MainActivity.this,
                        "Stopped IDS service and reset table",
                        Toast.LENGTH_SHORT).show();
            }
        });


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBoundService != null) {
            unbindService(mConnection);
        }
    }

}