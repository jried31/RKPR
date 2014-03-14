package com.example.ridekeeper.util;

import android.app.Fragment;

import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.example.ridekeeper.DBGlobals;
import com.example.ridekeeper.DBGlobals.SelectedFrag;
import com.example.ridekeeper.MainActivity;
import com.example.ridekeeper.MyRideListFragment;
import com.example.ridekeeper.MyVehicleListFragment;
import com.example.ridekeeper.StolenVehicleListFragment;

/**
    * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
    * sections of the app.
    */
public class AppSectionsPagerAdapter extends FragmentStatePagerAdapter {
    private MainActivity mMainActivity;

    public AppSectionsPagerAdapter(FragmentManager fm,
            MainActivity mainActivity) {
        super(fm);
        mMainActivity = mainActivity;
    }

    @Override
    public Fragment getItem(int i) {
    	Fragment fragment;
        switch (i) {
        case DBGlobals.TAB_IDX_STOLEN_VEHICLES:
            fragment = new StolenVehicleListFragment();
            mMainActivity.setSelectedFrag(SelectedFrag.STOLEN_VEHICLE);
            break;
        case DBGlobals.TAB_IDX_MY_VEHICLES:
            fragment = new MyVehicleListFragment();
            mMainActivity.setSelectedFrag(SelectedFrag.MY_VEHICLES);
            break;
        case DBGlobals.TAB_IDX_MY_RIDES:
            fragment = new MyRideListFragment();
            mMainActivity.setSelectedFrag(SelectedFrag.MY_RIDE);
            break;
        default:
            fragment = new MyVehicleListFragment();
            mMainActivity.setSelectedFrag(SelectedFrag.MY_PROFILE);
            break;
        }
        mMainActivity.invalidateOptionsMenu();
		return fragment;
    }

    @Override
    public int getCount() {
        //return mMainActivity.mDrawerMenuTitles.length;
    	return 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //return mMainActivity.mDrawerMenuTitles[position];
        return null;
    }
}
