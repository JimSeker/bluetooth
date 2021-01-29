package edu.cs4730.androidbeaconlibrarydemo2;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

public class myViewModel extends AndroidViewModel {
    private MutableLiveData<String> item;
    private MutableLiveData<Collection<Beacon>> mlist;

    public myViewModel(@NonNull Application application) {
        super(application);
        item = new MutableLiveData<String>();
        item.setValue("start\n");
        mlist = new MutableLiveData<Collection<Beacon>>();
    }

    LiveData<String> getItemLD() {
        return item;
    }

    String getItem() {
        return item.getValue();
    }

    void setItem(String n) {

        item.setValue(item.getValue() + n + "\n");

    }

    MutableLiveData<Collection<Beacon>> getBeaconlist() {
        return mlist;
    }

    void setMlist(Collection<Beacon> n) {
        mlist.setValue(n);
    }

    Collection<Beacon> getMlist() {
        return mlist.getValue();

    }

}
