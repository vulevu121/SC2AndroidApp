package com.example.sc2androidapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Fragment2 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.tab2, container, false);

        ((MainActivity)getActivity()).setFragment2View(v);

//        // MANUAL DRIVER DOOR BUTTONS
//
//        Button manualDriverOpenButton = v.findViewById(R.id.manualDriverOpenButton);
//        Button manualDriverCloseButton = v.findViewById(R.id.manualDriverCloseButton);
//
//
//
//        manualDriverOpenButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                ((MainActivity)getActivity()).manualOpenDriverPress();
//                return true;
//            }
//        });
//
//        manualDriverOpenButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    ((MainActivity)getActivity()).manualOpenDriverRelease();
//                }
//                return false;
//            }
//        });
//
//        manualDriverCloseButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                ((MainActivity)getActivity()).manualCloseDriverPress();
//                return true;
//            }
//        });
//
//        manualDriverCloseButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    ((MainActivity)getActivity()).manualCloseDriverRelease();
//                }
//                return false;
//            }
//        });
//
//
//        // MANUAL PASSENGER DOOR BUTTONS
//
//        Button manualPassengerOpenButton = v.findViewById(R.id.manualPassengerOpenButton);
//        Button manualPassengerCloseButton = v.findViewById(R.id.manualPassengerCloseButton);
//
//        manualPassengerOpenButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                ((MainActivity)getActivity()).manualOpenPassengerPress();
//                return true;
//            }
//        });
//
//        manualPassengerOpenButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    ((MainActivity)getActivity()).manualOpenPassengerRelease();
//                }
//                return false;
//            }
//        });
//
//        manualPassengerCloseButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                ((MainActivity)getActivity()).manualClosePassengerPress();
//                return true;
//            }
//        });
//
//        manualPassengerCloseButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    ((MainActivity)getActivity()).manualClosePassengerRelease();
//                }
//                return false;
//            }
//        });

        return v;
    }

}
