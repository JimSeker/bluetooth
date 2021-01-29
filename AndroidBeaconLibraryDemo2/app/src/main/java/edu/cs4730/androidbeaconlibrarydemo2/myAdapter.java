package edu.cs4730.androidbeaconlibrarydemo2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.util.Collection;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * this adapter is very similar to the adapters used for listview, except a ViewHolder is required
 * see http://developer.android.com/training/improving-layouts/smooth-scrolling.html
 * except instead having to implement a ViewHolder, it is implemented within
 * the adapter.
 *
 * This code has a ViewModel/LiveData so that the click can be observed by the Fragment and mainActivity.
 * Since the viewmodel can't be static, it is passed the viewholder.
 * likely we should pass the viewmodel to the adapter instead of the fragment, but this example is showing
 * how to get an instance all the three levels (activity, fragment, and adapter).
 */

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private Collection<Beacon> myList;
    private int rowLayout;
    public Context context;
    private final String TAG = "myAdapter";

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mName;
        public CardView mCardView;


        private final String TAG = "ViewHolder";

        public ViewHolder(View view, Context c) {
            super(view);
            mName = view.findViewById(R.id.name);
            mCardView = view.findViewById(R.id.card);
            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(c, mName.getText().toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //constructor
    public myAdapter(Collection<Beacon> myList, int rowLayout, Context context) {
        this.myList = myList;
        this.rowLayout = rowLayout;
        this.context = context;
    }

    public void setMyList(Collection<Beacon> myList) {
        this.myList = myList;
        Log.wtf(TAG, "new data.");
        notifyDataSetChanged();
    }
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v, context);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        //collections don't have a at/get operator!
        int pos =0;
        Beacon entry = null;
        for (Beacon beacon : myList) {
            if (pos == i) {
                entry = beacon;
            }
            pos++;
        }
        if (entry != null) {
            String temp = "Nothing";
            if (entry.getServiceUuid() == 0xfeaa && entry.getBeaconTypeCode() == 0x00) {
                // This is a Eddystone-UID frame
                Identifier namespaceId = entry.getId1();
                Identifier instanceId = entry.getId2();
                temp = "Eddystone -UID data" +
                        "\nnamespace id: " + namespaceId +
                        "\ninstance id: " + instanceId +
                        "\n distance approximately " + entry.getDistance() + " meters away.";
                if (entry.getExtraDataFields().size() > 0) {
                    long telemetryVersion = entry.getExtraDataFields().get(0);
                    long batteryMilliVolts = entry.getExtraDataFields().get(1);
                    long pduCount = entry.getExtraDataFields().get(3);
                    long uptime = entry.getExtraDataFields().get(4);
                    temp += "telemetry version " + telemetryVersion +
                            "\n has been up for : " + uptime + " seconds" +
                            "\n has a battery level of " + batteryMilliVolts + " mV" +
                            "\n and has transmitted " + pduCount + " advertisements.";
                }
                temp += "\nstring " + entry.toString();
            } else if (entry.getServiceUuid() == 0xfeaa && entry.getBeaconTypeCode() == 0x10) {
                String url = UrlBeaconUrlCompressor.uncompress(entry.getId1().toByteArray());

                temp = "Eddystone-URL " +
                        "\nurl: " + url +
                        "\n distance approximately " + entry.getDistance() + " meters away.";
            }    else {
                temp = "Altbeacon " +
                        "\nstring: " + entry.toString() +
                        "\n distance approximately " + entry.getDistance() + " meters away.";
            }

            viewHolder.mName.setText(temp);
            viewHolder.mName.setTag(entry);  //sample data to show.
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myList == null ? 0 : myList.size();
    }
}
