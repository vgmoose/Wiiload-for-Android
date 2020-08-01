package com.vgmoose.jwiiload;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.zip.*;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WiiloadActivity extends Activity implements OnClickListener {

	public static Context getAppContext() {
		return context;
	}

	public static final String PREFS_NAME = "wiimote";

	private static Context context;
	static Socket socket;
	static String host;
	static int port = 4299;

	static Button open;
	static Button scan;
	static Button send;

	static String ip2;
	static String warning;

	static String s;
	static String arguments = "";
	static File filename;

	static MenuItem mens1;
	static MenuItem mens2;

	static String title;

	static File compressed;
	static  boolean stopscan = false;

	static TextView status;
	static TextView fname;
	static String lastip;
	static EditText wiiip;

	static boolean pester=true;
	static String homeDir;
	static boolean showAds;
	static WiiloadActivity curInstance;

	static boolean[] filetypes = new boolean[6];

	public static void tripleScan()
	{
		stopscan = false;
		host = null;
		for (int x=1; x<3; x++)
		{
			scan(x);
			if (host!=null)
				break;
		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();

		SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);

		lastip = settings.getString("lastip", "0.0.0.0");

		showAds = settings.getBoolean("ads",true);

		port = settings.getInt("port", 4299);
		arguments = settings.getString("args",arguments);

		status = new TextView(context);
		setContentView(R.layout.main);
		fname = (TextView)(findViewById(R.id.textView1));

		wiiip = (EditText)findViewById(R.id.editText1);
		wiiip.setText(lastip);

		//		wiiip.setText("Hello");

		//		updateStatus("Ready to send data");
		open = (Button)(findViewById(R.id.button3));
		scan = (Button)(findViewById(R.id.button2));
		send = (Button)(findViewById(R.id.button1));
		//		b.setText("Choose File");
		//		c.setText("Send to Wii");
		send.setEnabled(false);
		//		a.addView(b);
		//		a.addView(status);

		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();

		if (!wifiManager.isWifiEnabled() && pester){  
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Turning on Wi-Fi is recommended, but not required.\nWould you like to enable Wi-Fi?").setPositiveButton("Confirm", dialogClickListener)
			.setNegativeButton("Cancel", dialogClickListener).setTitle("Wi-Fi is Recommended").show();
			pester=false;
		}


		int ipAddress = wifiInfo.getIpAddress();
		ip2 = intToIp(ipAddress);

		//		a.addView(c,params);  
		//		a.addView(fname, params2);
		open.setOnClickListener(this);
		scan.setOnClickListener(this);
		send.setOnClickListener(this);

		String[] bsarray = settings.getString("files", "true,true,true,true,true,false").split(",");
		Log.d("test",Arrays.toString(bsarray));

		for (int x=0;x<6;x++)
			filetypes[x] = Boolean.parseBoolean(bsarray[x]);
		Log.d("test",Arrays.toString(filetypes));


		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY))
			homeDir = settings.getString("home", "/sdcard/");
		else
			homeDir = settings.getString("home", "/");	

		if (filename!=null)
		{
			updateName();
			send.setEnabled(true);
		}


		if (showAds)
		{

			// Admob Code
			// Create the adView
			AdView av = new AdView(this);
			av.setAdSize(AdSize.BANNER);
			av.setAdUnitId("a14fce128a3b08a");
			// Lookup your LinearLayout assuming it�s been given
			// the attribute android:id="@+id/mainLayout"
			LinearLayout layout = (LinearLayout)findViewById(R.id.bottom);
			// Add the adView to it
			//	    layout.addView(av);
			// Initiate a generic request to load it with an ad
			AdRequest adr = new AdRequest.Builder().build();
			av.loadAd(adr);


			//	    RelativeLayout layout2 = (RelativeLayout)findViewById(R.id.relish);

			layout.addView(av);

		}

		Intent intent = getIntent();
		if (intent.getData()!=null)
		{
			//		Log.d("intent",intent.getData().getPath());
			filename = new File(intent.getData().getPath());
			handler.sendEmptyMessage(1);
		}



	}

	public void onClick(View v) {
		if (v==open)
		{
			curInstance = this;
			Intent intent = new Intent(this, FileChooser.class);
			this.startActivity(intent);
		}
		else if (v==send)
		{
			host = wiiip.getText().toString();
			send.setEnabled(false);
			open.setEnabled(false);
			scan.setEnabled(false);
			wiiip.setEnabled(false);
			mens1.setEnabled(false);
			mens2.setEnabled(false);

			new Thread()
			{
				@Override
				public void run()
				{
					try {
						if (socket!=null)
						{
							Log.d("CONNECT",socket.isClosed()+" Opening connection to "+host);
							Log.d("CONNECT",socket.getInetAddress().toString().substring(1)+"!="+host);

							Log.d("CONNECT",""+!myEquals(socket.getInetAddress().toString().substring(1),(host)));
						}
						if (socket==null || (!myEquals(socket.getInetAddress().toString().substring(1),(host)) && socket.isClosed()))
						{
							socket = new Socket(host,port);
							Log.d("socket","creating new socket");
						}
						wiisend();
					} catch (UnknownHostException e) {
						Log.d("","Poor format");
						title = "Hostname Error";
						warning = e.getMessage();
						handler.sendEmptyMessage(4);
					} catch (IOException e) {
						Log.d("","Network error");
						title = "Network Error";
						warning = e.getMessage();
						handler.sendEmptyMessage(4);
					}
					handler.sendEmptyMessage(3);
				}
			}.start();
		}
		else if (v==scan)
		{
			scan.setEnabled(false);
			wiiip.setEnabled(false);
			send.setEnabled(false);
			new Thread()
			{
				@Override
				public void run()
				{
					tripleScan();
					handler.sendEmptyMessage(0);
				}
			}.start();
		}
	}

	public static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (msg.what==0) // Scan button ending
			{
				scan.setEnabled(true);
				wiiip.setEnabled(true);
				if (open.isEnabled() && filename!=null)
					send.setEnabled(true);
				//			open.setEnabled(true);
				if (host!=null && !host.equals("rate"))
					wiiip.setText(host);



			} else if (msg.what==1)
			{
				send.setEnabled(false);
				//				fname.setText("Compressing data...");
				open.setEnabled(false);
				new Thread()
				{
					@Override
					public void run()
					{
						compressData();
						handler.sendEmptyMessage(2);
					}
				}.start();
			} else if (msg.what==2)
			{
				updateName();
				if (wiiip.isEnabled())
					send.setEnabled(true);
				open.setEnabled(true);
			}
			else if (msg.what==3)
			{
				send.setEnabled(true);
				open.setEnabled(true);
				scan.setEnabled(true);
				wiiip.setEnabled(true);
				mens1.setEnabled(true);
				mens2.setEnabled(true);


			}
			else if (msg.what==4)
			{
				try
				{

					AlertDialog.Builder builder = new AlertDialog.Builder(curInstance);
					builder.setTitle(title);
					builder.setMessage(warning);
					builder.setNeutralButton("Confirm", null);
					AlertDialog alert = builder.create();
					alert.show();

				} catch (NullPointerException e){}
			}
			else if (msg.what==5)
			{
				fname.setText(warning);
			}

		}
	};

	public static void compressData()
	{
		try
		{

			if (compressed!=null && filename!=compressed)
				compressed.delete();

			// Compress the file to send it faster
			updateStatus("Compressing data...");
			compressed = compressFile(filename);
			updateStatus("Data compressed!");

		} catch(Exception e){
			// Fall back in case compressed file can't be written
			compressed = filename;
		}
	}



	public static void wiisend()
	{

		try
		{
			// Open socket to wii with host and port and setup output stream

			//			updateStatus("Compressing Data");

			if (compressed==null || !compressed.exists())
				compressed=filename;

			updateStatus("Talking to Wii...");

			if (host==null)
			{
				Log.d("NETWORK","host is "+host+", and we're doing this check.");
				host = wiiip.getText().toString();
				socket = new Socket(host, port);
			}

			//			compressData();

			updateStatus("Talking to Wii...");

			OutputStream os = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);

			updateStatus("Preparing data...");

			byte max = 0;
			byte min = 5;

			short argslength = (short) (filename.getName().length()+arguments.length()+1);

			int clength = (int) (compressed.length());  // compressed filesize
			int ulength = (int) (filename.length());        // uncompressed filesize

			// Setup input stream for sending bytes later of compressed file
			InputStream is = new FileInputStream(compressed);
			BufferedInputStream bis = new BufferedInputStream(is);

			byte b[]=new byte[128*1024];
			int numRead=0;

			updateStatus("Talking to Wii...");

			dos.writeBytes("HAXX");

			dos.writeByte(max);
			dos.writeByte(min);

			dos.writeShort(argslength);

			dos.writeInt(clength);  // writeLong() sends 8 bytes, writeInt() sends 4
			dos.writeInt(ulength);

			//dos.size();   // Number of bytes sent so far, should be 16

			//			updateStatus("Sending "+filename.getName());
			Log.d("NETWORK","Sending "+filename.getName()+"...");
			dos.flush();

			int chunk=0;

			while ( ( numRead=bis.read(b)) > 0) {
				dos.write(b,0,numRead);
				chunk++;
				updateStatus("Sending... ("+(int)((((double)chunk)/(clength/((double)(b.length))))*100)+"%)");

				dos.flush();
			}
			dos.flush();

			if (arguments.length()!=0)
			{
				updateStatus("Sending arguments...");
				Log.d("NETWORK","Sending arguments...");
			}

			updateStatus("Finishing up...");

			dos.writeBytes(filename.getName()+"\0");

			String[] argue = arguments.split(" ");

			for (String x : argue)
				dos.writeBytes(x+"\0");

			updateStatus("File transfer successful!");
			Log.d("NETWORK","\nFile transfer successful!");

			lastip = host;

			socket.close();
			socket = null;



			//			if (compressed!=filename)
			//				compressed.delete();

		}
		catch (Exception ce)
		{
			//			updateStatus("No Wii found");
			//                    int a=0;

			title = "No Wii Found";
			ce.printStackTrace();
			warning = ce.getMessage();
			handler.sendEmptyMessage(4);

			if (host==null)
				host="";

			Log.d("NETWORK","No Wii found at "+host+"!");

			//                    if (!cli)
			//                    {
			//                            if (host.equals("rate"))
			//                                    a = framey.showRate();
			//                            else
			//                                    a= framey.showLost();
			//                    }
			//                  
			//                    if (a==0)
			//                    {
			//                            tripleScan();
			//                            wiisend();
			//                    }

		}
	}

	static void updateName()
	{
		fname.setText(filename.getName()+" "+arguments);
	}

	static void updateStatus(String s)
	{
		Log.d("STRING",s);
		warning = s;
		handler.sendEmptyMessage(5);
	}

	public String intToIp(int i) {

		// get xxx.xxx.xxx. to prepare for search
		return (i & 0xFF ) + "." +
		((i >> 8 ) & 0xFF) + "." +
		((i >> 16 ) & 0xFF) + ".";
	}

	static void scan(int t)
	{                       
		host=null;

		//		updateStatus("Finding Wii...");
		Log.d("NETWORK","Searching for a Wii...");
		String output = null;

		// this code assumes IPv4 is used
		String ip = ip2;	

		Log.d("ip2",ip2);

		for (int i = 1; i <= 254; i++)
		{
			try
			{
				ip = ip2 + i; 
				InetAddress address = InetAddress.getByName(ip);
				//				Log.d("NETWORK","Checking "+ip);

				if (address.isReachable(10*t))
				{
					output = address.toString().substring(1);
					Log.d("NETWORK",output + " is on the network");

					// Attempt to open a socket
					try
					{
						socket = new Socket(output,port);
						Log.d("NETWORK","and is potentially a Wii!");

						//						updateStatus("Wii found!");
						//						socket.close();
						//						 wiiip.setText(output);

						host=output;
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			} catch (ConnectException e) {
				//				updateStatus("Rate limited");
				host="rate";
				title="Rate Limited";
				warning=e.getMessage();
				handler.sendEmptyMessage(4);
				e.printStackTrace();
				return;
			} catch (Exception e) {
				title="Error Occurred";
				warning=e.getMessage();
				handler.sendEmptyMessage(4);
				e.printStackTrace();
				return;
			}

			if (stopscan)
			{
				//				updateStatus("Scan aborted");
				Log.d("NETWORK","Scan aborted");
				break;
			}
		} 

		return;

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		mens1 = menu.add(0, 1, 2, "Set Arguments");
		mens2 = menu.add(0,2,2,"Change Port");
		return true;
	}


	public void createAlert(final int type)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		if (type==1)
		{
			alert.setTitle("Set Arguments");
			alert.setMessage("Enter any number of arguments for the program.");
		}
		else
		{
			alert.setTitle("Change Port");
			alert.setMessage("Enter a new port number to use. (default: 4299)");
		}

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		if (type!=1)
		{
			input.setInputType(InputType.TYPE_CLASS_PHONE);
			input.setKeyListener(DigitsKeyListener.getInstance());
			input.setText(""+port);
		}
		else
			input.setText(arguments);
		alert.setView(input);

		alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				if (type==1)
				{
					arguments=value;
					if (send.isEnabled())
						updateName();
				}
				else
					port=Integer.parseInt(value);
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//				if (type==1)
				//					arguments="";
				//				else
				//					port=Integer.parseInt(value);
			}
		});

		alert.show();
	}

	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
			case DialogInterface.BUTTON_POSITIVE:
				WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
				wifiManager.setWifiEnabled(true);
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			createAlert(1);
			return true;
		case 2:
			createAlert(2);
			return true;
		}
		return false;
	}

	public static File compressFile(File raw) throws IOException
	{
		File compressed = new File(filename+".wiiload.gz");
		InputStream in = new FileInputStream(raw);
		OutputStream out =
			new DeflaterOutputStream(new FileOutputStream(compressed));
		byte[] buffer = new byte[1024];
		int len;
		while((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}
		in.close();
		out.close();
		return compressed;
	}

	@Override
	public void onStop()
	{
		super.onStop();

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("lastip",lastip);
		editor.putInt("port", port);
		editor.putString("args",arguments);
		editor.putString("home", homeDir);
		editor.putBoolean("ads", showAds);
		String files="";

		for (boolean s : filetypes)
			files+=""+s+",";

		files = files.substring(0, files.length()-1);

		editor.putString("files", files);
		editor.commit();

		if (compressed!=null && filename!=compressed)
			compressed.delete();
	}

	public void finish()
	{
		filename=null;
		compressed=null;

		send.setEnabled(false);
		super.finish();
	}

	public static boolean myEquals(String s, String t)
	{
		if (s.length()!=t.length())
			return false;

		for (int x=0;x<s.length();x++)
			if (s.charAt(x)!=t.charAt(x))
				return false;

		return true;

	}
}