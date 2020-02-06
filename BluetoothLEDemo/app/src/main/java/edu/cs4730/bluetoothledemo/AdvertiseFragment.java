package edu.cs4730.bluetoothledemo;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.UUID;


/**
 * This fragment will advertise data via bluetooth LE.
 */
public class AdvertiseFragment extends Fragment {
    private final static String TAG = "AdvertiseFragment";
    private EditText text;
    private Button advertise;
    private Boolean advertising = false;
    private TextView logger;

    private BluetoothLeAdvertiser advertiser;


    private AdvertiseCallback advertisingCallback;

    public AdvertiseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_advertise, container, false);

        text = myView.findViewById(R.id.editText);
        logger = myView.findViewById(R.id.loggera);

        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        advertise = myView.findViewById(R.id.advertise);
        advertise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (advertising) { //turn it off
                    stop_advertise();
                } else { //turn on adversting
                    start_advertise();
                }
            }
        });

        return myView;
    }

    /**
     * This will stop the adversting.  It needs the callback that is created in the start advertising.
     */
    private void stop_advertise() {
        if (advertisingCallback != null && advertising)
            advertiser.stopAdvertising(advertisingCallback);
        advertising = false;
        advertise.setText("Start Advertising");
    }

    /**
     * start advertise
     * setup the power levels, the UUID, and the data
     * which is used the callback then call start advertising.
     */
    private void start_advertise() {

        //define the power settings  could use ADVERTISE_MODE_LOW_POWER, ADVERTISE_MODE_BALANCED too.
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build();
        //get the UUID needed.
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(getString(R.string.blue_uuid)));
        //build
        AdvertiseData data = new AdvertiseData.Builder()
            .setIncludeDeviceName(false)  //should be true, but we are bigger then 31bytes in the name?
            .addServiceUuid(pUuid)
            //this is where the text is added.
            .addServiceData(pUuid, text.getText().toString().getBytes(Charset.forName("UTF-8")))
            .build();
        advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                logthis("Advertising has started");
                logthis("message is " + text.getText().toString());
                advertising = true;
                advertise.setText("Stop Advertising");
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                logthis("Advertising onStartFailure: " + errorCode);
                advertising = false;
                advertise.setText("Start Advertising");
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising(settings, data, advertisingCallback);

    }

    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }

}
