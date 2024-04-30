package edu.cs4730.androidbeaconlibrary;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.Collection;
import java.util.Map;

/**
 * very beginning of a beacon replacement example since google is dropping all support.
 * <p>
 * https://altbeacon.github.io/android-beacon-library/index.html
 * https://github.com/AltBeacon
 */


public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private static final String TAG = "MainActivity";
    private String[] REQUIRED_PERMISSIONS;
    TextView logger;
    Region myRegion;

    //the piece for permissions.
    private ActivityResultLauncher<String[]> rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> isGranted) {
            boolean granted = true;
            for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                logthis(x.getKey() + " is " + x.getValue());
                if (!x.getValue()) granted = false;
            }
            if (granted) startexample();
            else
                logthis("Don't have permissions");
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logger = findViewById(R.id.logger);
        //which permissions are needed at varying apis.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION};
            logthis("Android 12+, we need scan and connect.");
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            //this one may not work, but I don't have a 29 to test with, access_background_location may not be needed here.  it breaks the one above, so I remove it and now it works.
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
            logthis("api 29 for background access  ");
        } else {
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            logthis("api 28 or less fine location only.  ");
        }


        logthis("App Starting");
        //check for permissions and start the beacons.
        beaconManager = BeaconManager.getInstanceForApplication(this);
        //added eddystone, since I'm moving from google's beacons to altbeacon.  RedBeacon can broadcast both.
        // Detect the main identifier (UID) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Detect the telemetry (TLM) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        // Detect the URL frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));

        if (!allPermissionsGranted()) rpl.launch(REQUIRED_PERMISSIONS);
        else {
            logthis("All permissions have been granted already.");
            startexample();
        }

    }

    void startexample() {
        logthis("Starting up!");
        // Set up a Live Data observer so this Activity can get monitoring callbacks
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        myRegion = new Region("myMonitoringUniqueId", null, null, null);
        //first the monitor
        logthis("beacon monitor observer has been added.");
        beaconManager.getRegionViewModel(myRegion).getRegionState().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer state) {
                if (state == MonitorNotifier.INSIDE) {
                    Log.d(TAG, "Detected beacons(s)");
                    logthis("I can see at least one beacon");
                } else {
                    Log.d(TAG, "Stopped detecting beacons");
                    logthis("I no longer see an beacon");

                }
            }
        });
        beaconManager.startMonitoring(myRegion);

        //now the ranged information.
        logthis("beacon range observer has been added.");
        beaconManager.getRegionViewModel(myRegion).getRangedBeacons().observe(this, new Observer<Collection<Beacon>>() {
            @Override
            public void onChanged(Collection<Beacon> beacons) {
                logthis("didRangeBeaconsInRegion called with beacon count:  " + beacons.size());
                for (Beacon beacon : beacons) {
                    if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                        // This is a Eddystone-UID frame
                        Identifier namespaceId = beacon.getId1();
                        Identifier instanceId = beacon.getId2();
                        logthis("I see a beacon transmitting namespace id: " + namespaceId + " and instance id: " + instanceId + " approximately " + beacon.getDistance() + " meters away.");

                        // Do we have telemetry data?
                        if (!beacon.getExtraDataFields().isEmpty()) {
                            long telemetryVersion = beacon.getExtraDataFields().get(0);
                            long batteryMilliVolts = beacon.getExtraDataFields().get(1);
                            long pduCount = beacon.getExtraDataFields().get(3);
                            long uptime = beacon.getExtraDataFields().get(4);

                            logthis("The above beacon is sending telemetry version " + telemetryVersion + ", has been up for : " + uptime + " seconds" + ", has a battery level of " + batteryMilliVolts + " mV" + ", and has transmitted " + pduCount + " advertisements.");

                        }

                    } else if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x10) {
                        // This is a Eddystone-URL frame
                        String url = UrlBeaconUrlCompressor.uncompress(beacon.getId1().toByteArray());
                        logthis("I see a beacon transmitting a url: " + url + " approximately " + beacon.getDistance() + " meters away.");
                    } else {
                        //no clue what we found here.
                        logthis("found a beacon, (not eddy) " + beacon.toString() + " and is approximately " + beacon.getDistance() + "meters away");
                    }

                }
            }
        });
        beaconManager.startRangingBeacons(myRegion);
    }

    /**
     * helper method to send strings to both the logcat and to a textview call logger.
     */
    private void logthis(String item) {
        Log.v(TAG, item);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logger.append("\n" + item);
            }
        });
    }

    @Override
    protected void onStop() {
        beaconManager.stopMonitoring(myRegion);
        beaconManager.stopRangingBeacons(myRegion);
        super.onStop();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}