package com.example.projectx;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class VolunteerDetailsFragmentAdapter extends FragmentStatePagerAdapter {
    public VolunteerDetailsFragmentAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        VolunteerDetailsFragment volunteerDetailsFragment = new VolunteerDetailsFragment();
        Bundle nameBundle = new Bundle();
        Bundle phoneBundle = new Bundle();

        nameBundle.putString("name", "XYZ "+position);
        volunteerDetailsFragment.setArguments(nameBundle);

        phoneBundle.putString("number", "123 "+position);
        volunteerDetailsFragment.setArguments(phoneBundle);

        return volunteerDetailsFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
