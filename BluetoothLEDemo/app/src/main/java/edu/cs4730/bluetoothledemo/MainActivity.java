package edu.cs4730.bluetoothledemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import edu.cs4730.bluetoothledemo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
