package edu.cs4730.bluetoothledemo;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class DiscoverFragment extends Fragment {
    private final static String TAG = "DiscoverFragment";
    TextView logger;
    private Button discover;
    private Boolean discovering = false;


    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler();

    public DiscoverFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_discover, container, false);

        logger = myView.findViewById(R.id.loggerd);
        discover = myView.findViewById(R.id.discover);
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (discovering) { //stop it
                    stop_discover();
                } else { //start
                    start_discover();
                }
            }
        });

        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        return myView;
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (result == null || result.getDevice() == null) {
                logthis("The data result is empty or no data");
            } else {
                //should have useful info.
                //name
                StringBuilder builder = new StringBuilder("Name: ").append(result.getDevice().getName());
                //address
                builder.append("\n").append("address: ").append(result.getDevice().getAddress());
                //data
                builder.append("\n").append("data: ").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));
                logthis(builder.toString());
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            logthis("Got batch something");
            for (ScanResult result : results) {
                //name
                StringBuilder builder = new StringBuilder("Name: ").append(result.getDevice().getName());
                //address
                builder.append("\n").append("address: ").append(result.getDevice().getAddress());
                //data
                builder.append("\n").append("data: ").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));
                logthis(builder.toString());
            }
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            logthis("Discovery onScanFailed: " + errorCode);
            discovering = false;
            discover.setText("Start Discovering");
            super.onScanFailed(errorCode);

        }
    };

    void stop_discover() {
        mBluetoothLeScanner.stopScan(mScanCallback);
        logthis("Discovery stopped");
        discovering = false;
        discover.setText("Start Discovering");
    }

    void start_discover() {

        List<ScanFilter> filters = new ArrayList<ScanFilter>();

        ScanFilter filter = new ScanFilter.Builder()
            .setServiceUuid(new ParcelUuid(UUID.fromString(getString(R.string.blue_uuid))))
            .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mScanCallback);
                logthis("Discovery stopped");
                discovering = false;
                discover.setText("Start Discovering");
            }
        }, 5000);
        logthis("Discovery started");
        discovering = true;
        discover.setText("Stop Discovering");
    }

    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }

}
