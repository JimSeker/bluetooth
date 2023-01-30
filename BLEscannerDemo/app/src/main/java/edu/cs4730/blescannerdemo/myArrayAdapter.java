package edu.cs4730.blescannerdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Seker on 3/2/2018.
 */

public class myArrayAdapter extends ArrayAdapter<BluetoothDevice> {

    private List<BluetoothDevice> list;
    private final Activity context;

    public myArrayAdapter(@NonNull Activity context, @NonNull List<BluetoothDevice> list) {
        super(context, R.layout.row, list);
        this.context = context;
        this.list = list;
    }


    public void setData(List<BluetoothDevice> list) {
        this.list = list;
        notifyDataSetInvalidated();
    }

    @SuppressLint("MissingPermission")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView name, address;
        if (convertView == null) {
            //having problems with the convertVeiw when not null, so just redoing it each time.  ...
            LayoutInflater inflator = context.getLayoutInflater();
            convertView = inflator.inflate(R.layout.row, null);
        }

        name = convertView.findViewById(R.id.name);
        name.setText("Name: " + list.get(position).getName());
        address = convertView.findViewById(R.id.address);
        address.setText("Name: " + list.get(position).getAddress());

        return convertView;
    }
}
