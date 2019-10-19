package com.example.sc2androidapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Fragment2 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab2, container, false);

        // MANUAL DRIVER DOOR BUTTONS

        Button manualDriverOpenButton = v.findViewById(R.id.manualDriverOpenButton);
        Button manualDriverCloseButton = v.findViewById(R.id.manualDriverCloseButton);

        manualDriverOpenButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((MainActivity)getActivity()).manualDriverOpenPress();
                return true;
            }
        });

        manualDriverOpenButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((MainActivity)getActivity()).manualDriverOpenRelease();
                }
                return false;
            }
        });

        manualDriverCloseButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((MainActivity)getActivity()).manualDriverClosePress();
                return true;
            }
        });

        manualDriverCloseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((MainActivity)getActivity()).manualDriverCloseRelease();
                }
                return false;
            }
        });


        // MANUAL PASSENGER DOOR BUTTONS

        Button manualPassengerOpenButton = v.findViewById(R.id.manualPassengerOpenButton);
        Button manualPassengerCloseButton = v.findViewById(R.id.manualPassengerCloseButton);

        manualPassengerOpenButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((MainActivity)getActivity()).manualPassengerOpenPress();
                return true;
            }
        });

        manualPassengerOpenButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((MainActivity)getActivity()).manualPassengerOpenRelease();
                }
                return false;
            }
        });

        manualPassengerCloseButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((MainActivity)getActivity()).manualPassengerClosePress();
                return true;
            }
        });

        manualPassengerCloseButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ((MainActivity)getActivity()).manualPassengerCloseRelease();
                }
                return false;
            }
        });

        return v;
    }

}
