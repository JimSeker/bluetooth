package edu.cs4730.btDemo;

import java.util.UUID;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import edu.cs4730.btDemo.databinding.ActivityMainBinding;

/**
 * most of the work is done in the fragments.  You will need to install this example onto 2 devices
 * with bluetooth in order to make this example work.
 * <p>
 * The help fragment starts up first, to check and see if this example will work.
 * Server fragment is the for the "server" code version with bluetooth.
 * Client fragment is will connect to the server code.
 */

public class MainActivity extends AppCompatActivity {

    public static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final String NAME = "BluetoothDemo";
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

}
