package com.example.myapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


import androidx.appcompat.app.AppCompatActivity;

import static java.lang.Integer.parseInt;
import static java.lang.Math.log10; //this is the logarithm library.

public class CalculatedValueActivity extends Activity {

    public static final int I0 = 1;// i0 is the light intesity of the light 660 nm. You shd also have anbother i0 for the 940 nm light.

    BluetoothAdapter bluetoothAdapter;
    public static BluetoothSocket mmSocket;
    public static BluetoothDevice mmDevice;
    public static OutputStream mmOutputStream;
    public static InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    ArrayList<Integer> LEDValues = new ArrayList<Integer>();

    ArrayList<Integer> anemiaValuesREDLED = new ArrayList<Integer>();
    ArrayList<Integer> anemiaValuesIRLED = new ArrayList<Integer>();

    public static ArrayList<Integer> i0ValuesRedLED = new ArrayList<Integer>();
    public static ArrayList<Integer> i0ValuesIRLED = new ArrayList<Integer>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculatedvalue); //the name in purple should match the name of the layout
        double I = 0.125 ; //user input


        try {
 //           connectBluetooth();
            readInfoFromBluetooth();

        } catch(IOException e) {
            Log.println(Log.ERROR, "CalculatedValueActivity", e.getMessage());
            stopWorker = true;
            try {
                closeBTChannel();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }


    public void connectBluetooth() {
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID

            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid); //establishing a software socket
            //mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(uuid); //establishing a software socket
            mmSocket.connect(); //connect to the socket
            mmInputStream = mmSocket.getInputStream();

        } catch(IOException e) {
            Log.println(Log.ERROR, "CalculatedValueActivity", e.getMessage());
            try {
                closeBTChannel();
            } catch (IOException ex) {
                Log.println(Log.ERROR, "CalculatedValueActivity", ex.getMessage());
            }
        }
    }


    public void readInfoFromBluetooth() throws IOException
    {
        mmInputStream = mmSocket.getInputStream();
        beginListenForData();

    }

    public void sendCommandToArduino(char command) {
        try {
            mmOutputStream = mmSocket.getOutputStream(); //output stream from socket
            mmOutputStream.write(command); //writing o the output stream, 1.
        } catch(IOException e) {
            Log.println(Log.ERROR, "CalculatedValueActivity", e.getMessage());
        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        //final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[8];

        workerThread = new Thread(new Runnable()// start here when explaining, NEW THREAD IS BEING CREATED.
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {

                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
//                            for(int i=0;i<bytesAvailable;i++)
//                            {
//                                byte b = packetBytes[i];
//                                    byte[] encodedBytes = new byte[readBufferPosition];
//                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                    final String data = new String(encodedBytes, "US-ASCII");
//                                    readBufferPosition = 0;
                                    final String data = new String(packetBytes, "US-ASCII");
                                    if (data.contains("Anemic") || data.contains("Non Anemic"))
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            calculateAnemia(data);
                                        }
                                    });
                            //}
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    private void calculateAnemia(String I) {
        //convert string to double
//        double value = Double.parseDouble(I);
//        double finalABS  = value / 0.03;


        java.lang.String result;

        TextView absValueText = (TextView) findViewById(R.id.absValueNumber);

        TextView anemiaText = (TextView) findViewById(R.id.AnemiaText);

        if (I.contains("Anemic")){
            result = "The patient has anemia.";
            anemiaText.setText(result);

        }else if (I.contains("Non Anemic")){
            result = "Patient does not have anemia";
            anemiaText.setText(result);

        }
        try {
            closeBTChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void calculateAnemia(double I) {
        double transmittance = I/I0 ; //value inside the parentheses of -log10(i0/I).

        double absorption = -log10(transmittance); //you need a log library to perform this function!

        double finalABS = 10 * absorption;

        java.lang.String result;

        TextView absValueText = (TextView) findViewById(R.id.absValueNumber);

        absValueText.setText( Double.toString(finalABS));

        TextView anemiaText = (TextView) findViewById(R.id.AnemiaText);

        if (finalABS < 12.94){
            result = "You have anemia";
            anemiaText.setText(result);

        }else {
            result = "You don't have anemia";
            anemiaText.setText(result);

        }
    }


    void closeBTChannel() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }

}
