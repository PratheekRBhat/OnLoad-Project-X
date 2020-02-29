package com.example.projectx;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class VolunteerDetailsFragment extends Fragment {
    private TextView vName, vNumber;
    private RelativeLayout relativeLayout;
    public VolunteerDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_volunteer_details, relativeLayout, false);
        vName = view.findViewById(R.id.Volunteer_name1);
        vNumber = view.findViewById(R.id.Volunteer_phone1);

        String name = getArguments().getString("name");
        vName.setText(name);

        String number = getArguments().getString("number");
        vNumber.setText(number);

        return view;
    }
}
