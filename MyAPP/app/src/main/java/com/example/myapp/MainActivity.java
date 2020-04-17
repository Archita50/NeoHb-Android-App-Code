package com.example.myapp;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Button;
import android.widget.TextView;

import java.util.Set;


public class MainActivity extends Activity {//mainactivity is extending the functionalities of activity
    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private CalculatedValueActivity c = new CalculatedValueActivity(); //we're using a constructor function ??(new calxculated value activity, to the right of equal sign) to create abn object, and to set default values to that object.


    @Override //reimplementing a function: an animal doesn't run at all, how will we include that under the Horse class? We reimplement it.
    protected void onCreate(Bundle savedInstanceState) { //this is like the main function, code of UI loading onto the app.

        Button beginButton;

        super.onCreate(savedInstanceState); //DO NOT DELETE THIS
        setContentView(R.layout.activity_main); //DO NOT DELETE THIS

        turnOnBlueTooth();

        beginButton = (Button) findViewById(R.id.beginButton); //getting the id of beginbutton

        beginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) { //this is a listener code that listens to an event on the on click event of the begin button. It creates a new page.

                c.connectBluetooth(); // connect to the bluetooth

                // send a message to arduino to turn on RED LED, IR led, and tell arduino to capture i0 values of Red LED and IR LED, command is 1
                c.sendCommandToArduino('1');


                //this registration activity should be declared in the android manifest.
                Intent intent = new Intent(view.getContext(), CalculatedValueActivity.class);
                startActivityForResult(intent, 0);

            }

        });



    }

    private void turnOnBlueTooth() { //function for turning on the bluetooth.

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //first get a bluetooth adapter, which is an android class.
        if (bluetoothAdapter == null) {
            // Feature --> This Device doesn't support Bluetooth, display some message on screen
        } else {

            if (!bluetoothAdapter.isEnabled()) { //if bluetooth IS NOT ENABLED, enable it.
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); //goes into the phone settings, asks for enabling bluetooth (pop up question from the phone, should MYAPP allow bluetooth?).
            } else {
                if (bluetoothAdapter.isEnabled()) {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device : pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            TextView textView = (TextView) findViewById(R.id.textView);
                            if (deviceName.equals("HC-05")) {
                                CalculatedValueActivity.mmDevice = device;
                                textView.setText("HC-05 connected over bluetooth");
                            } else {
                                // connect
                                textView.setText("HC-05 not connected over bluetooth");
                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //the phone is going to connect to bluetooth, but then notifies the app whether it's connected to bluetooth of not.
        //th ephone notifies the app about connection to bluetooth using this function.
        if(requestCode == REQUEST_ENABLE_BT){ //has the user tried to enable bluetooth?
            if(bluetoothAdapter.isEnabled()) { //is BLUETOOTH on?
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices(); //if so, get all the devices that are paired to this phone's bluetooth. Which in thsi case, it can be handsfree link, corola, hc05
                if (pairedDevices.size() > 0) { //if there are more than 0 paried devices
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) { //go thru the paired devices
                        String deviceName = device.getName(); //get their name
                        String deviceHardwareAddress = device.getAddress(); // get their MAC address, or unique hardware address
                        TextView textView = (TextView) findViewById(R.id.textView); //get the id of textview from the activity main xml
                        if (deviceName.equals("HC-05")) { //if a paired devic has a name of hc 05
                            CalculatedValueActivity.mmDevice = device; //get the device information

                            textView.setText("HC-05 connected over bluetooth"); //set the text of the text view on the UI, saying hc 05 is connected
                        } else {
                            // connect
                            textView.setText("HC-05 not connected over bluetooth"); //connect to the hc 05 bluetooth module.
                        }
                    }
                }
            } else {
                // Bluetooth is not enabled - display a message, future implementation.
                TextView textView = (TextView) findViewById(R.id.AnemiaText);
                textView.setText("Bluetooth not enabled");
            }
        }
    }
    }

