package edu.cs4730.bluetoothledemo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import edu.cs4730.bluetoothledemo.databinding.FragmentAdvertiseBinding;


/**
 * This fragment will advertise data via bluetooth LE.
 */
@SuppressLint("MissingPermission")
public class AdvertiseFragment extends Fragment {
    private final static String TAG = "AdvertiseFragment";
    private Boolean advertising = false;
    FragmentAdvertiseBinding binding;
    private BluetoothLeAdvertiser advertiser;
    private AdvertiseCallback advertisingCallback;

    public AdvertiseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAdvertiseBinding.inflate(inflater, container, false);

        advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        binding.advertise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (advertising) { //turn it off
                    stop_advertise();
                } else { //turn on adversting
                    start_advertise();
                }
            }
        });

        return binding.getRoot();
    }

    /**
     * This will stop the advertising.  It needs the callback that is created in the start advertising.
     */
    private void stop_advertise() {
        if (advertisingCallback != null && advertising)
            advertiser.stopAdvertising(advertisingCallback);
        advertising = false;
        binding.advertise.setText("Start Advertising");
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
            .addServiceData(pUuid, binding.editText.getText().toString().getBytes(StandardCharsets.UTF_8))
            .build();
        advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                logthis("Advertising has started");
                logthis("message is " + binding.editText.getText().toString());
                advertising = true;
                binding.advertise.setText("Stop Advertising");
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                logthis("Advertising onStartFailure: " + errorCode);
                advertising = false;
                binding.advertise.setText("Start Advertising");
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising(settings, data, advertisingCallback);

    }

    public void logthis(String msg) {
        binding.loggera.append(msg + "\n");
        Log.d(TAG, msg);
    }

}
