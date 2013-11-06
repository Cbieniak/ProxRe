package com.BienProgramming.proxre;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Locale;
import java.util.Set;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
/**Main activity for ProxRE. Given this was an MVP. Most actions are handled here.
 * 
 * @author Christian
 * @ToDo Add delete contacts option,Scan again button
 */
public class MainActivity extends FragmentActivity {
	static ArrayList<Device> devList;
	Intent mServiceIntent;
	static ArrayList<Contact> conList;
	static ArrayList<String> remDev;
	static ArrayAdapter<Device> devArrayAdapter;
	static ArrayAdapter<Contact> conArrayAdapter;
	static ArrayAdapter<String> remArrayAdapter;	
	static ListView myListView;
	static ListView conListView;
	static ListView reminderListView;
	BroadcastReceiver mReceiver;
	static Database db;
	static Context mContext;
	boolean running=false;
	boolean btRunning=false;
	
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		conArrayAdapter= new ArrayAdapter<Contact>(this, android.R.layout.simple_list_item_1);
		devArrayAdapter= new ArrayAdapter<Device>(this, android.R.layout.simple_list_item_1);
		remArrayAdapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		remDev = new ArrayList<String>();
		devList = new ArrayList<Device>();
		conList = new ArrayList<Contact>();
		mContext=this;

		db = new Database(this);


		// Create the adapter that will return a fragment for each of the two
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		//Check if help has been shown
		SharedPreferences reminders = getSharedPreferences("rem", 0);
		SharedPreferences.Editor editor = reminders.edit();
		if(reminders.getAll().size()==0){
			about();
			editor.putString("help", "0");
			editor.commit();
		}
		Set<String> defaultVal = new HashSet<String>();

		//Get the reminders from shared preferences
		Set<String> SPremind =reminders.getStringSet("rem", defaultVal);
		remDev.addAll(SPremind);
		for(String rem:SPremind){
			
			remArrayAdapter.add(rem.split("\n")[1]+"-"+rem.split("\n")[2]);
		}
		//Get contacts from the database
		for(Contact contact:db.getAllContacts()){
			conArrayAdapter.add(contact);
			conList.add(contact);
		}


		BlueTooth();

	}
	/**Use bluetooth to find devices around the user
	 * 
	 */
	public void BlueTooth(){
		devList.clear();
		devArrayAdapter.clear();
		setProgressBarIndeterminateVisibility(true); 
		btRunning=true;
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		//Check that the devices has bluetooth
		if (btAdapter == null) {
			@SuppressWarnings("unused")
			AlertDialog alertDialogBuilder = new AlertDialog.Builder(
					mContext).setTitle("No bluetooth").setMessage("Unfortunatly this app will not work without bluetooth").setNeutralButton("OK", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							android.os.Process.killProcess(android.os.Process.myPid());

						}

					}).show();
		}
		//Check it is enabled
		if(!btAdapter.isEnabled()){
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);

			startActivity(discoverableIntent);
		}

		btAdapter.startDiscovery();

		mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (BluetoothDevice.ACTION_FOUND.equals(action)) 
				{
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// Add the name and address to an array adapter to show in a ListView
					//New stuff
					Device mDev;
					if(!device.getName().equals(null)){
						 mDev= new Device(device.getAddress(),device.getName());	
					}
					else{
						mDev = new Device(device.getAddress(),"unnamed");
					}
						
						
					if(!devList.contains(mDev)){
						
						devList.add(mDev);
						devArrayAdapter.add(mDev);
					}



					//When the search is finished stop showing the progress bar
				}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
					setProgressBarIndeterminateVisibility(false);
					btRunning=false;
					unregisterReceiver(mReceiver);
				}

			}


		};

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);
		

	}
	@Override
	public void onPause(){
		try{
			unregisterReceiver(mReceiver);
		}catch(Exception e){
			//Receiver isn't registered
		}
		//Add all reminders to the shared preferences
		SharedPreferences reminders = getSharedPreferences("rem", 0);
		SharedPreferences.Editor editor = reminders.edit();
		Set<String> reminderSet = new HashSet<String>();

		for(String dev:remDev){
			reminderSet.add(dev);	
		}
		editor.putStringSet("rem", reminderSet);
		editor.commit();
		try{if(running){
			mContext.stopService(mServiceIntent);
			running=false;}
		}catch(Exception e){
			Log.d("try stopping service",e.toString());
		}
		if(!running){
			Intent mServiceIntent = new Intent(mContext, btservice.class);
			mServiceIntent.putStringArrayListExtra("devs", remDev);

			mContext.startService(mServiceIntent);

			running=true;
		}

		super.onPause();
	}
	@Override
	public void onResume(){
		super.onResume();
		
		if(!btRunning)
			BlueTooth();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
		case R.id.action_about:
			about();
			break;
		case R.id.action_Scan:
			BlueTooth();
			break;		
		}

		return true;
	}
	@Override
	protected void onStop()
	{	
		try{
			unregisterReceiver(mReceiver);
		}catch(Exception e){
			//exception;
		}
		super.onStop();
	}
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment;
			if(position == 0){
				fragment = new DevicesSectionFragment();
				Bundle args = new Bundle();
				args.putInt(DevicesSectionFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
			}
			else{
				fragment = new ReminderSectionFragment();
				Bundle args = new Bundle();
				args.putInt(ReminderSectionFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);

			}
			return null;
		}
	}

	/**Devices Fragment
	 * Shows all devices in range as well as all contacts
	 * Allows users to add contacts and reminders
	 * @author Christian
	 *
	 */
	public static class DevicesSectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DevicesSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_devices,
					container, false);

			myListView = (ListView)rootView.findViewById(R.id.devlistView);
			myListView.setAdapter(devArrayAdapter);
			myListView.setOnItemClickListener(new OnItemClickListener(){


				//Create a reminder from a device around you
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					final int position=arg2;
					final EditText name =new EditText(mContext);
					final EditText input =new EditText(mContext);
					name.setHint("Name");
					input.setHint("Reminder");
					LinearLayout ll = new LinearLayout(mContext);
					ll.setOrientation(LinearLayout.VERTICAL);
					ll.addView(name);
					ll.addView(input);


					new AlertDialog.Builder(mContext)
					.setTitle("Reminder")
					.setMessage("Create a Contact for this device?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							//if reminder list already contains the device
							if(true){


								//New stuff
								Device mDev =devList.get(position);

								Contact mCon = new Contact(mDev.getId(),mDev.name,name.getText().toString().trim());
								conList.add(mCon);
								conArrayAdapter.add(mCon);
								db.addContact(mCon);
								remDev.add(mCon.getDevice_id()+"\n"+mCon.getName()+"\n"+input.getText().toString().trim());

								remArrayAdapter.add(mCon.getName()+"-"+input.getText().toString().trim());
							}
						}
					}).setView(ll)
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							// do nothing
						}
					})
					.show();

				}

			});

			conListView = (ListView)rootView.findViewById(R.id.conlistView);
			conListView.setAdapter(conArrayAdapter);
			conListView.setOnItemClickListener(new OnItemClickListener(){


				//Create a reminder from a contact
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int pos, long arg3) {

					final int pos1=pos;

					final EditText input =new EditText(mContext);

					input.setHint("Reminder");
					final String name =conArrayAdapter.getItem(pos).getName();


					new AlertDialog.Builder(mContext)
					.setTitle("Reminder")
					.setMessage("Create a Reminder for "+name+"?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 

							Contact mCon = conList.get(pos1);
							remDev.add(mCon.getDevice_id()+"\n"+mCon.getName()+"\n"+input.getText().toString().trim());
							remArrayAdapter.add(mCon.getName()+"-"+input.getText().toString().trim());


						}
					}).setView(input)
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							// do nothing
						}
					})
					.show();

				}

			});
			return rootView;
		}
	}
	/**Fragment for the reminders section.
	 * Allows users to delete reminders
	 * 
	 * @author Christian
	 *
	 */
	public static class ReminderSectionFragment extends Fragment {
		
		public static final String ARG_SECTION_NUMBER = "section_number";
		public ReminderSectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_reminder,
					container, false);


			reminderListView = (ListView)rootView.findViewById(R.id.remindersList);
			reminderListView.setAdapter(remArrayAdapter);
			reminderListView.setOnItemClickListener(new OnItemClickListener(){


				//Delete a reminder
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					final int position=arg2;
					new AlertDialog.Builder(mContext)
					.setTitle("Delete")
					.setMessage("Delete this reminder?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 

							remArrayAdapter.remove(remArrayAdapter.getItem(position));
							remDev.remove(position);

						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) { 
							// do nothing
						}
					})
					.show();

				}

			});

			return rootView;
		}
	}
	/**An about screen that contains bugs, howto and the ability to email me
	 * 
	 */
	public void about(){
		TextView desc = new TextView(mContext);
		desc.setText(R.string.desc);
		desc.setPaddingRelative(16, 16,0, 16);
		TextView knownbugs = new TextView(mContext);
		knownbugs.setText(R.string.known_bugs);
		knownbugs.setPaddingRelative(16, 0, 0, 16);
		TextView howTo = new TextView(mContext);
		howTo.setText(R.string.howto);
		howTo.setPaddingRelative(16, 0, 16, 16);
		LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.addView(desc);
		ll.addView(knownbugs);
		ll.addView(howTo);

		new AlertDialog.Builder(mContext)
		.setTitle("ProxRe")

		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}

		}).setView(ll)
		.setNegativeButton("Email me", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
						"mailto","bienprogramming@gmail.com", null));
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "PROXRE FEEDBACK");
				startActivity(Intent.createChooser(emailIntent, "Send email"));
			}
		})
		.show();
	}
	/**Finds the position of contact
	 * In order to create an accurate reminder
	 * 
	 * @param name Of the device
	 * @return Spot on the list
	 */
	public static int findPosition(String name){
		for(int i =0;i<conList.size();i++){
			if(name.equals(conList.get(i).getName()))
				return i;

		}
		return -1;
	}

}
