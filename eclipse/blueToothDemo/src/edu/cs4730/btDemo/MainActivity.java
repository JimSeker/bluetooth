package edu.cs4730.btDemo;

import java.util.Locale;
import java.util.UUID;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;


public class MainActivity extends FragmentActivity implements Help_Fragment.Callbacks {

	String TAG = "MainActivity";
	//SectionsPagerAdapter mSectionsPagerAdapter;

	public static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	public static final String NAME = "BluetoothDemo";
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	//ViewPager mViewPager;
	FragmentManager fragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.frag_container, new Help_Fragment()).commit();
		
	}

	@Override
	public void onButtonSelected(int id) {
		
		FragmentTransaction transaction =fragmentManager.beginTransaction();
		// Replace whatever is in the fragment_container view with this fragment,
		if (id == 2) { //client
			transaction.replace(R.id.frag_container, new Client_Fragment());
		} else { //server
			transaction.replace(R.id.frag_container, new Server_Fragment());
		}
		// and add the transaction to the back stack so the user can navigate back
		transaction.addToBackStack(null);
		// Commit the transaction
		transaction.commit();
	}

}
