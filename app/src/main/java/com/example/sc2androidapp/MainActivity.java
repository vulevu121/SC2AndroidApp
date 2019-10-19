package com.example.sc2androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.sc2androidapp.ui.main.SectionsPagerAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private BluetoothDevice device = null;
    private BluetoothSocket socket = null;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private boolean deviceConnected = false;
    private boolean driverDoorOpen = false;
    private boolean passengerDoorOpen = false;
    private ProgressBar progressBar = null;
    private ImageView bluetoothButton = null;
    private boolean stopListenThread = false;
//    private byte buffer[];
    private int carViewResId = 0;
    private ImageView carView = null;
    private ImageView carBaseView = null;
    private Animation fadeIn = null;
    private Animation fadeOut = null;

//    private View.OnTouchListener handleTouch = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            return true;
//        }
//
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        progressBar = findViewById(R.id.busyIndicator);
        bluetoothButton = findViewById(R.id.bluetoothButton);

        carView = findViewById(R.id.carView);
        carBaseView = findViewById(R.id.carBaseView);

        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator()); //add this
        fadeIn.setDuration(1000);

        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(1000);

//        AnimationSet animation = new AnimationSet(false); //change to false
//        animation.addAnimation(fadeIn);
//        animation.addAnimation(fadeOut);
//
//        carView.setAnimation(animation);
//        carBaseView.setAnimation(animation);

        carViewResId = R.drawable. both_doors_down;
        carView.setImageResource(carViewResId);
        carBaseView.setImageResource(carViewResId);

        carView.startAnimation(fadeIn);
        carBaseView.startAnimation(fadeIn);
    }



//    @Override
//    protected void onStart() {
//        super.onStart();
//        btConnect();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        btConnect();


    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        btDisconnect();
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        btDisconnect();
    }

    public void btConnect() {
        if (deviceConnected) return;

        progressBar.setVisibility(View.VISIBLE);

        // gets the default bluetooth adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // toast a message if no default bluetooth adapter is found
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Bluetooth was not found on device",Toast.LENGTH_SHORT).show();
        }

        // if not enabled, ask user to enable bluetooth
        if(!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(),"Please enable Bluetooth",Toast.LENGTH_SHORT).show();
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
        }

        // get list of paired bluetooth devices, including our esp32
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();

        // toast a message if no paired devices found
        if(!bondedDevices.isEmpty()) {
            for (BluetoothDevice bluetoothDevice : bondedDevices) {

                // open a socket if our esp32 is found
                if (bluetoothDevice.getName().equals("SC2 Vision")) {
                    device = bluetoothDevice;

                    try {
                        // create and connect to a bluetooth socket using SPP uuid
                        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
                        socket.connect();
                        outputStream = socket.getOutputStream();
                        inputStream = socket.getInputStream();
                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                        bluetoothButton.setColorFilter(getResources().getColor(R.color.colorAccent));

                        // begin to listen on bluetooth for replies
                        btListen();
                        deviceConnected = true;
                        return;
                    } catch (IOException e) {
                        deviceConnected = false;
                        Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    finally {
                        progressBar.setVisibility(View.GONE);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "SC2 is not paired", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }
    }

    public void btDisconnect() {
        try {
            if (socket.isConnected()) {
                socket.close();
                ImageView bluetoothButton = findViewById(R.id.bluetoothButton);
                bluetoothButton.setColorFilter(getResources().getColor(R.color.colorPrimary));
                Toast.makeText(getApplicationContext(),"Disconnected",Toast.LENGTH_SHORT).show();
                deviceConnected = false;
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"Unable to close socket",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void btWrite(String msg) {
        if (socket == null) return;

        if (socket.isConnected() && deviceConnected) {

            progressBar.setVisibility(View.VISIBLE);

            try {
                outputStream.write(msg.getBytes());
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Unable to write msg", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            finally {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    public void btListen() {
        final Handler handler= new Handler();
        stopListenThread = false;
//        buffer = new byte[1024];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopListenThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            byte rawBytes[] = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("SERIAL INPUT", string);

//                                    String string2 = "FL=20,FR=21";
//                                    try {
//                                        final Pattern pattern = Pattern.compile("FL=(\\d+),FR=(\\d+)");
//                                        final Matcher matcher = pattern.matcher(string2);
//
//                                        int FL = Integer.parseInt(matcher.group(1));
//                                        int FR = Integer.parseInt(matcher.group(2));
//
//                                        Log.d("Front Left Door", String.valueOf(FL));
//                                        Log.d("Front Right Door", String.valueOf(FR));
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }

                                }
                            });
                        }
                    }
                    catch (IOException e) {
                        stopListenThread = true;
                    }

                }
            }
        });

        thread.start();
    }

    public void setCarView() {
        carBaseView.setImageResource(carViewResId);

        if (driverDoorOpen & !passengerDoorOpen) {
            carViewResId = R.drawable.driver_door_up;
            carView.setImageResource(carViewResId);
            carView.startAnimation(fadeIn);
        }
        else if (driverDoorOpen & passengerDoorOpen) {
            carViewResId = R.drawable.both_doors_up;
            carView.setImageResource(carViewResId);
            carView.startAnimation(fadeIn);
        }
        else if (!driverDoorOpen & !passengerDoorOpen) {
            carViewResId = R.drawable.both_doors_down;
            carView.setImageResource(carViewResId);
            carView.startAnimation(fadeIn);
        }
        else if (!driverDoorOpen & passengerDoorOpen) {
            carViewResId = R.drawable.passenger_door_up;
            carView.setImageResource(carViewResId);
            carView.startAnimation(fadeIn);
        }
    }

    public boolean isDeviceConnected() {
        if (!deviceConnected) {
            Toast.makeText(getApplicationContext(), "SC2 not connected", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void getDoorStatus() {
        if (!isDeviceConnected()) return;
        btWrite("Door Status?");
    }

    public void autoDriverOpen(View v) {
        if (!isDeviceConnected()) return;

        Button button = v.findViewById(R.id.autoDriverOpenButton);

        if (button.getText().equals("Open Driver")) {
            btWrite("Auto open driver\n");
//            Toast.makeText(getApplicationContext(), "Auto Open Driver", Toast.LENGTH_SHORT).show();
            driverDoorOpen = true;
            button.setText("Close Driver");
        }
        else {
            btWrite("Auto close driver\n");
//            Toast.makeText(getApplicationContext(), "Auto Close Driver", Toast.LENGTH_SHORT).show();
            driverDoorOpen = false;
            button.setText("Open Driver");
        }

        setCarView();

    }

    public void autoPassengerOpen(View v) {
        if (!isDeviceConnected()) return;

        Button button = v.findViewById(R.id.autoPassengerOpenButton);

        if (button.getText() == "Open Passenger") {
            btWrite("Auto open passenger\n");
//            Toast.makeText(getApplicationContext(), "Auto Open Passenger", Toast.LENGTH_SHORT).show();
            passengerDoorOpen = true;
            button.setText("Close Passenger");
        }
        else {
            btWrite("Auto close passenger\n");
//            Toast.makeText(getApplicationContext(), "Auto Close Passenger", Toast.LENGTH_SHORT).show();
            passengerDoorOpen = false;
            button.setText("Open Passenger");
        }

        setCarView();
    }



    // FOR DRIVER SIDE

    public void manualDriverOpenPress() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual driver open", Toast.LENGTH_SHORT).show();
        btWrite("Manual driver open\n");
    }

    public void manualDriverOpenRelease() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual driver stop", Toast.LENGTH_SHORT).show();
        btWrite("Manual driver stop\n");
    }

    public void manualDriverClosePress() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual driver close", Toast.LENGTH_SHORT).show();
        btWrite("Manual driver close\n");
    }

    public void manualDriverCloseRelease() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual driver stop", Toast.LENGTH_SHORT).show();
        btWrite("Manual driver stop\n");
    }
    
    // FOR PASSENGER SIDE

    public void manualPassengerOpenPress() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual passenger open", Toast.LENGTH_SHORT).show();
        btWrite("Manual passenger open\n");
    }

    public void manualPassengerOpenRelease() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual passenger stop", Toast.LENGTH_SHORT).show();
        btWrite("Manual passenger stop\n");
    }

    public void manualPassengerClosePress() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual passenger close", Toast.LENGTH_SHORT).show();
        btWrite("Manual passenger close\n");
    }

    public void manualPassengerCloseRelease() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual passenger stop", Toast.LENGTH_SHORT).show();
        btWrite("Manual passenger stop\n");
    }
    
    public void bluetoothButtonClicked(View v) {
        if (deviceConnected) {
            btDisconnect();
        }
        else {
            btConnect();
        }
    }
}