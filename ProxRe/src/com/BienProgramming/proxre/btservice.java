package com.BienProgramming.proxre;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.IntentService;
import android.app.Notification;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


/**A service that scans for Bluetooth devices every 10 minutes
 * And Alerts the user if the devices found are of importance
 * 
 * @author Christian
 *
 */
public class btservice extends IntentService{
	Context mContext= this;
	ArrayList<String> remDev;
	String[] DeviceArray;
	NotificationManager mNotificationManager;
	BroadcastReceiver serReceiver;
	public btservice() {
		super("btservice");

	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		remDev=intent.getStringArrayListExtra("devs");

		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
	
		Timer timer = new Timer ();
		TimerTask hourlyTask = new TimerTask () {
			@Override
			public void run () {
				if(remDev.size()!=0)
				BlueTooth();
			}
		};
		//Search after 10 minutes every 10 minutes
		timer.schedule(hourlyTask, 1000*60*60, 1000*60*60); 

	}
	/**
	 * Handles the search, checking if each device found is important to the user
	 */
	public void BlueTooth(){
		

		final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		btAdapter.startDiscovery();

		serReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
	
				if (BluetoothDevice.ACTION_FOUND.equals(action)) 
				{
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// Add the name and address to an array adapter to show in a ListView
					for(String dev:remDev){
						System.out.println(dev.split("\n")[0]);

						if(dev.split("\n")[0].equals(device.getAddress())){

							
							//alert the people
							Notification.Builder noti = new Notification.Builder(mContext)
							.setContentTitle("ProxRE")
							.setContentText(dev.split("\n")[1]+" "+dev.split("\n")[2]).setSmallIcon(R.drawable.ic_launcher);

							mNotificationManager.notify(
									1,
									noti.build());

						}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
							
							unregisterReceiver(serReceiver);
						}


					}
				}

			}
		};

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 
		registerReceiver(serReceiver, filter);


	}
}
