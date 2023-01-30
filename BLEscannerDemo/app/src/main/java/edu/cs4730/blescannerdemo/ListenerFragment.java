package edu.cs4730.blescannerdemo;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ListenerFragment extends Fragment {

    //bluetooth ble variables
    BluetoothAdapter adapter;
    BluetoothLeScanner scanner;
    MyScanCallback myScanCallback;

    //fragment variables
    Context context;
    Button btn_start, btn_stop;
    TextView logger;
    static final String TAG = "serverFragment";
    private OnFragmentInteractionListener mListener;

    //lisview stuff
    myArrayAdapter myAdapter;
    ListView list;
    List<BluetoothDevice> myList;

    public ListenerFragment() {
        // Required empty public constructor
        myList = new ArrayList<BluetoothDevice>();
    }


    public interface OnFragmentInteractionListener {
        /**
         * Callback for when an item has been selected.
         */
        public void onDeviceSelected(BluetoothDevice d);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_server, container, false);
        //output textview
        //logger = myView.findViewById(R.id.s_output);
        //listview
        list = myView.findViewById(R.id.list);
        myAdapter = new myArrayAdapter(requireActivity(),	myList);
        list.setAdapter(myAdapter);

        btn_start = myView.findViewById(R.id.start_server);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanning();
            }
        });
        btn_stop = myView.findViewById(R.id.stop_server);
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScanning();
            }
        });

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            //Handle this issue. Report to the user that the device does not support BLE
            logthis("no bluetooth, shouldn't be here");
        } else {
            adapter = bluetoothManager.getAdapter();
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onDeviceSelected( myList.get(position));
            }
        });
        return myView;
    }
    @SuppressLint("MissingPermission")
    public void startScanning() {

        scanner = adapter.getBluetoothLeScanner();
        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        List<ScanFilter> scanFilters = Arrays.asList(
            new ScanFilter.Builder()
                //.setServiceUuid(ParcelUuid.fromString("some uuid"))
                .build());
        myScanCallback = new MyScanCallback();
        scanner.startScan(scanFilters, scanSettings, myScanCallback);
    }

    public class MyScanCallback extends ScanCallback {

        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            //Do something with results
            BluetoothDevice device = adapter.getRemoteDevice(result.getDevice().getAddress());
            logthis(device.getName() + " " + device.getAddress());
            if (!myList.contains(device)) {
                myList.add(device);
                myAdapter.setData(myList);
            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            //Do something with batch of results
        }

        @Override
        public void onScanFailed(int errorCode) {
            //Handle error
        }
    }
    @SuppressLint("MissingPermission")
    public void stopScanning() {
        if (scanner != null) {
            scanner.stopScan(myScanCallback);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void logthis(String msg) {
        //logger.append(msg + "\n");
        Log.d(TAG, msg);
    }


}
