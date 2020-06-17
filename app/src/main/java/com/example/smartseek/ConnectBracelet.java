package com.example.smartseek;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class ConnectBracelet extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ImageButton b_infoconnect;
    private ImageButton HomeSymbol;
    private TextView TextPairedDevices;
    private Button b_clear;
    private Button b_connect;
    private Button b_bluetooth;
    private BluetoothAdapter BlueAdapter;
    //make intergers for requesting bluetooth connection
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;
    //list for paired bluetooth devices
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DevicesListBluetooth mDevicelistAdapter;
    private ListView List_PairedDevices;

    //checks the states of the Bluetooth discovery mode
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            //if the connect button is clicked (if action is action found), it stores the devices
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                mDevicelistAdapter = new DevicesListBluetooth(context, R.layout.devices_list_bluetooth, mBTDevices);
                List_PairedDevices.setAdapter(mDevicelistAdapter);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_bracelet);

        //initialize listview and the arraylist for the Bluetooth connected devices
        List_PairedDevices = (ListView) findViewById(R.id.List_PairedDevices);
        mBTDevices = new ArrayList<>();
        List_PairedDevices.setOnItemClickListener(ConnectBracelet.this);

        //initialize info symbol button
        b_infoconnect = (ImageButton) findViewById(R.id.b_infoconnect);
        //when the symbol is clicked it opens up the Connect info popup
        b_infoconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConnectBracelet.this, ConnectInfoPopUp.class));
            }
        });

        //initialize home symbol button
        HomeSymbol = (ImageButton) findViewById(R.id.HomeSymbol);
        //when the home symbol button is clicked open the main menu tab
        HomeSymbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ConnectBracelet.this, StartMainMenu.class));
            }
        });

        //initialize bluetooth button
        b_bluetooth = (Button) findViewById(R.id.b_bluetooth);
        //when the button is clicked bluetooth turns on (if not already on)
        b_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if bluetooth is not on, turn it on after the user allowed usage
                if (!BlueAdapter.isEnabled()) {
                    showToast("Allow this to make the Bluetooth turn on");
                    //pop up that asks persmisson for bluetooth usage
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }
                //if bluetooth is already on show message that it is already on
                else {
                    showToast("Bluetooth is already on");
                }
            }
        });

        //initialize clear button
        b_clear = (Button) findViewById(R.id.b_clear);
        //when the button is clicked the bluetooth turns off (if it is not already off)
        b_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if Bluetooth is on turn it off
                if (BlueAdapter.isEnabled()) {
                    BlueAdapter.disable();
                    showToast("Turning Bluetooth off");
                    //clears the paired devices list

                }
                //if bluetooth is off show message saying its off
                else {
                    showToast("Bluetooth is already off");
                }
            }
        });
        //bluetooth adapter
        BlueAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    //show msg string for short amount of time (this is used in the connect button and in the clear button
    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void b_connect(View view) {
        //if BLuetooth is in discovery mode, turn in off and on again (this is extra check and also permission check)
        if (BlueAdapter.isDiscovering()) {
            BlueAdapter.cancelDiscovery();

            //permission check
            checkBTPermissions();

            BlueAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter((BluetoothDevice.ACTION_FOUND));
            registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
        }
        //if Bluetooth is not in discovery mode, start the discovery mode
        if (!BlueAdapter.isDiscovering()) {

            //permission check
            checkBTPermissions();

            BlueAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter((BluetoothDevice.ACTION_FOUND));
            registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
        }

    }

    //permission check for using bluetooth discovery, so it works for every device
    //required for devices running on API23+
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCES_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCES_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //when clicked on Bluetooth device to pair (from the list), cancel discovery (because it is very memory intensive)
        BlueAdapter.cancelDiscovery();
        //get name and address
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();
        //create the bluetooth connection, method can only be used for API 17+ (thats why jelly bean MR2)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBTDevices.get(i).createBond();
        }
    }
}