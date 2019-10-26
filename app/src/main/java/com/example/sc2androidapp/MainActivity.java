package com.example.sc2androidapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;


import android.util.Log;
import android.view.MotionEvent;
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
    private View fragment2View = null;
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

    private int carViewResId = 0;
    private ImageView carView = null;
    private ImageView carBaseView = null;
    private Animation fadeIn = null;
    private Animation fadeOut = null;

    private final Pattern doorStatusPattern = Pattern.compile("DoorStatus:FL=(\\d+),FR=(\\d+)\\n");
    private int driverDoorAngle = 0;
    private int passengerDoorAngle = 0;
    private int OPEN_THRESHOLD = 10;


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


    public void setFragment2View(View view) {
        this.fragment2View = view;

        if (view != null) {
            Button manualDriverOpenButton = view.findViewById(R.id.manualDriverOpenButton);

            manualDriverOpenButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    manualOpenDriverPress();
                    return true;
                }
            });

            manualDriverOpenButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        manualOpenDriverRelease();
                    }
                    return false;
                }
            });

            Button manualDriverCloseButton = view.findViewById(R.id.manualDriverCloseButton);

            manualDriverCloseButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    manualCloseDriverPress();
                    return true;
                }
            });

            manualDriverCloseButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        manualCloseDriverRelease();
                    }
                    return false;
                }
            });

            Button manualPassengerOpenButton = view.findViewById(R.id.manualPassengerOpenButton);

            manualPassengerOpenButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    manualOpenPassengerPress();
                    return true;
                }
            });

            manualPassengerOpenButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        manualOpenPassengerRelease();
                    }
                    return false;
                }
            });

            Button manualPassengerCloseButton = view.findViewById(R.id.manualPassengerCloseButton);

            manualPassengerCloseButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    manualClosePassengerPress();
                    return true;
                }
            });

            manualPassengerCloseButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        manualClosePassengerRelease();
                    }
                    return false;
                }
            });
        }
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

                        // ask for door status
                        btWrite(getString(R.string.doorStatus));

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

//            progressBar.setVisibility(View.VISIBLE);

            try {
                outputStream.write(msg.getBytes());
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Unable to write msg", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
//            finally {
//                progressBar.setVisibility(View.GONE);
//            }
        }
    }



    public void btListen() {
//        final Handler handler= new Handler();
        stopListenThread = false;
//        buffer = new byte[1024];
//        stringBuffer = "";

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopListenThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 5) {
                            byte inputBytes[] = new byte[byteCount];
                            inputStream.read(inputBytes);
                            final String string = new String(inputBytes, "UTF-8");

                            Log.d("SERIAL INPUT", string);

                            if (string.contains("DoorStatus:")) {
                                try {
                                    final Matcher m = doorStatusPattern.matcher(string);
//                                    Log.d("Match", String.valueOf(m.matches()));
                                    if (m.matches()) {
                                        int FL = Integer.parseInt(m.group(1));
                                        int FR = Integer.parseInt(m.group(2));
                                        Log.d("FL Door Angle", String.valueOf(FL));
                                        Log.d("FR Door Angle", String.valueOf(FR));
                                        setDoorAngles(FL, FR);
                                    }

                                    m.reset();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
//                            else if (string.contains("DoorStatus:")) {
//                                try {
//                                    final Matcher m = doorStatusPattern.matcher(string);
////                                    Log.d("Match", String.valueOf(m.matches()));
//                                    if (m.matches()) {
//                                        int FL = Integer.parseInt(m.group(1));
//                                        int FR = Integer.parseInt(m.group(2));
//                                        Log.d("Front Left Door", String.valueOf(FL));
//                                        Log.d("Front Right Door", String.valueOf(FR));
//                                        driverDoorAngle = FL;
//                                        passengerDoorAngle = FR;
//                                    }
//
//                                    m.reset();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }

//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Log.d("SERIAL INPUT", string);
//
//                                    if (string.contains("DoorStatus:")) {
//                                        try {
//
//                                            final Matcher m = doorStatusPattern.matcher(string);
//                                            Log.d("Match", String.valueOf(m.matches()));
//                                            if (m.matches()) {
//                                                int FL = Integer.parseInt(m.group(1));
//                                                int FR = Integer.parseInt(m.group(2));
//                                                Log.d("Front Left Door", String.valueOf(FL));
//                                                Log.d("Front Right Door", String.valueOf(FR));
//                                                driverDoorAngle = FL;
//                                                passengerDoorAngle = FR;
//                                            }
//
//                                            m.reset();
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//
//                                    }
//
////                                    final String string2 = "FL=20,FR=21";
//
//                                }
//                            });
                        }
                    }
                    catch (IOException e) {
                        stopListenThread = true;
                    }
//
                }
            }
        });

        thread.start();
    }

    public void setCarView() {
        carBaseView.setImageResource(carViewResId);

        if (driverDoorOpen && !passengerDoorOpen) {
            carViewResId = R.drawable.driver_door_up;
            carView.setImageResource(carViewResId);
            carView.startAnimation(fadeIn);
        }
        else if (driverDoorOpen && passengerDoorOpen) {
            carViewResId = R.drawable.both_doors_up;
            carView.setImageResource(carViewResId);
            carView.startAnimation(fadeIn);
        }
        else if (!driverDoorOpen && !passengerDoorOpen) {
            carViewResId = R.drawable.both_doors_down;
            carView.setImageResource(carViewResId);
            carView.startAnimation(fadeIn);
        }
        else if (!driverDoorOpen && passengerDoorOpen) {
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

//    public void getDoorStatus() {
//        if (!isDeviceConnected()) return;
//        btWrite(getString(R.string.doorStatus));
//    }

    public void setDoorAngles(int driverDoor, int passengerDoor) {
        if (driverDoor >= 0 && driverDoor != driverDoorAngle) {
            driverDoorAngle = driverDoor;
            driverDoorOpen = (driverDoor > OPEN_THRESHOLD);
        }

        if (passengerDoor >= 0 && passengerDoor != passengerDoorAngle) {
            passengerDoorAngle = passengerDoor;
            passengerDoorOpen = (passengerDoor > OPEN_THRESHOLD);
        }
        updateDoorStatus();
    }

    public void updateDoorStatus() {
        Button autoOpenDriverButton = findViewById(R.id.autoOpenDriverButton);
        Button autoOpenPassengerButton = findViewById(R.id.autoOpenPassengerButton);

        if (autoOpenDriverButton != null) {
            autoOpenDriverButton.setText(driverDoorOpen ? "Close Driver" : "Open Driver");
        }

        if (autoOpenPassengerButton != null) {
            autoOpenPassengerButton.setText(passengerDoorOpen ? "Close Passenger" : "Open Passenger");
        }

        setCarView();
    }

    public void autoOpenDriver(View v) {
        if (!isDeviceConnected()) return;
        btWrite(getString(driverDoorOpen ? R.string.autoCloseDriver : R.string.autoOpenDriver ));
    }

    public void autoOpenPassenger(View v) {
        if (!isDeviceConnected()) return;
        btWrite(getString(passengerDoorOpen ? R.string.autoClosePassenger : R.string.autoOpenPassenger ));
    }

    public void autoOpenBoth(View v) {
        if (!isDeviceConnected()) return;
        btWrite(getString(R.string.autoOpenBoth));
    }

    public void autoCloseBoth(View v) {
        if (!isDeviceConnected()) return;
        btWrite(getString(R.string.autoCloseBoth));
    }

    // FOR DRIVER SIDE

    public void manualOpenDriverPress() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual driver open", Toast.LENGTH_SHORT).show();
        btWrite(getString(R.string.manualOpenDriver));
    }

    public void manualOpenDriverRelease() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual driver stop", Toast.LENGTH_SHORT).show();
        btWrite(getString(R.string.manualStopDriver));
    }

    public void manualCloseDriverPress() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual driver close", Toast.LENGTH_SHORT).show();
        btWrite(getString(R.string.manualCloseDriver));
    }

    public void manualCloseDriverRelease() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual driver stop", Toast.LENGTH_SHORT).show();
        btWrite(getString(R.string.manualStopDriver));
    }
    
    // FOR PASSENGER SIDE

    public void manualOpenPassengerPress() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual passenger open", Toast.LENGTH_SHORT).show();
        btWrite(getString(R.string.manualOpenPassenger));
    }

    public void manualOpenPassengerRelease() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual passenger stop", Toast.LENGTH_SHORT).show();
        btWrite(getString(R.string.manualStopPassenger));
    }

    public void manualClosePassengerPress() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual passenger close", Toast.LENGTH_SHORT).show();
        btWrite(getString(R.string.manualClosePassenger));
    }

    public void manualClosePassengerRelease() {
        if (!isDeviceConnected()) return;
//        Toast.makeText(getApplicationContext(), "Manual passenger stop", Toast.LENGTH_SHORT).show();
        btWrite(getString(R.string.manualStopPassenger));
    }

    public void hydraulicUp(View v) {
        if (!isDeviceConnected()) return;
        btWrite(getString(R.string.hydraulicUp));
    }

    public void hydraulicDown(View v) {
        if (!isDeviceConnected()) return;
        btWrite(getString(R.string.hydraulicDown));
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