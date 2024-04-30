package edu.cs4730.androidbeaconlibrarydemo2;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.Map;

import edu.cs4730.androidbeaconlibrarydemo2.databinding.ActivityMainBinding;

/**
 *
 *  more complex version with seperate fragments for the  found vs range.
 *  Note, for some reason, on android 11 (pixel2) location is automatically denied.  I've updated the permissions to try and fix it, still doesn't work.
 *    so you have to manually give it permissions.
 */

public class MainActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private static final String TAG = "MainActivity";
    private String[] REQUIRED_PERMISSIONS;
    ActivityResultLauncher<String[]> rpl;
    HomeFragment homeFrag;
    RangeFragment rangeFrag;
    myViewModel mViewModel;
    Region myRegion;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup the view model first.
        mViewModel = new ViewModelProvider(this).get(myViewModel.class);
        //setup the correct permissions needed, depending on which version. (31 changed the permissions.).
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            REQUIRED_PERMISSIONS = new String[]{"android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT", "android.permission.ACCESS_FINE_LOCATION"};
            logthis("Android 12+, we need scan and connect.", 1);
        } else {
            REQUIRED_PERMISSIONS = new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_BACKGROUND_LOCATION"};
            logthis("Android 11 or less, location and bluetooth permissions.", 1);
        }

        rpl = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    boolean granted = true;
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        logthis( entry.getKey() + " " + entry.getValue(),1);
                        if (!entry.getValue()) granted = false;
                    }
                    if (granted)
                        startexample();
                    else
                        logthis("Don't have permissions",1);

                }
            });



        binding.navView.setOnItemSelectedListener(
            new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.navigation_home) {
                        getSupportFragmentManager().beginTransaction()
                            .replace(binding.container.getId(), homeFrag)
                            .commit();
                        return true;
                    } else if (item.getItemId() == R.id.navigation_notifications) {
                        getSupportFragmentManager().beginTransaction()
                            .replace(binding.container.getId(), rangeFrag)
                            .commit();
                        return true;
                    }
                    return false;
                }

            }
        );


        logthis("App Starting", 0);
        //check for permissions and start the beacons.
        beaconManager = BeaconManager.getInstanceForApplication(this);
        //added eddystone, since I'm moving from google's beacons to altbeacon.  RedBeacon can broadcast both.
        // Detect the main identifier (UID) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        // Detect the telemetry (TLM) frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        // Detect the URL frame:
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));


        homeFrag = new HomeFragment();
        rangeFrag = new RangeFragment();
        if (!allPermissionsGranted())
            rpl.launch(REQUIRED_PERMISSIONS);
        else {
            logthis("All permissions have been granted already.", 1);
            startexample();
        }

        getSupportFragmentManager().beginTransaction()
            .add(binding.container.getId(), homeFrag)
            .commit();

    }

    /**
     * helper method to send strings to both the logcat and to a textview call logger.
     */
    private void logthis(String item, int which) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, item);
                if (which == 1) {
                    mViewModel.setItem(item);
                } else if (which == 2) {
                    if (rangeFrag != null) {
                        rangeFrag.logthis(item);
                    }
                }
            }
        });
    }

    /**
     * Setup and start the observers here.
     */
    void startexample() {
        logthis("Starting up!", 1);
        // Set up a Live Data observer so this Activity can get monitoring callbacks
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        myRegion = new Region("myMonitoringUniqueId", null, null, null);
        //first the monitor

        beaconManager.getRegionViewModel(myRegion).getRegionState().observe(this, new Observer<Integer>() {
                @Override
                public void onChanged(Integer state) {
                    logthis("monitor change", 1);
                    if (state == MonitorNotifier.INSIDE) {
                        logthis("I can see at least one or more beacons.", 1);
                        // all the region variables appear to be null, because I set them that way?
                        logthis("the region ID is " + myRegion.getUniqueId(), 1);
                    } else {
                        logthis("I no longer see any beacons", 1);

                    }
                }
            }


        );
        beaconManager.startMonitoring(myRegion);
        logthis("beacon monitor observer has been added.", 1);


        //now the ranged information.
        logthis("beacon range observer has been added.", 2);
        beaconManager.getRegionViewModel(myRegion).getRangedBeacons().observe(this, new Observer<Collection<Beacon>>() {
            @Override
            public void onChanged(Collection<Beacon> beacons) {
                logthis("didRangeBeaconsInRegion called with beacon count:  " + beacons.size(), 2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mViewModel.setMlist(beacons);
                    }
                });
            }
        });
        beaconManager.startRangingBeacons(myRegion);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    protected void onStop() {
        beaconManager.stopMonitoring(myRegion);
        beaconManager.stopRangingBeacons(myRegion);
        super.onStop();
    }


}