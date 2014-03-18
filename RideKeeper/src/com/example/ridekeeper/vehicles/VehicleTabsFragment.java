package com.example.ridekeeper.vehicles;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ridekeeper.DBGlobals;
import com.example.ridekeeper.DBGlobals.SelectedFrag;
import com.example.ridekeeper.MainActivity;
import com.example.ridekeeper.R;
import com.example.ridekeeper.myride.MyRideListFragment;

/**
 * Source: https://gist.github.com/chrisjenx/3405429
 * http://stackoverflow.com/questions/10396321/remove-fragment-page-from-viewpager-in-android
 * 
 * Use runnable and handler to delay the conflict of FragmentTransaction
 * when switching from a drawer Fragment to a tabbed Fragment
 */
public class VehicleTabsFragment extends Fragment implements ActionBar.TabListener {
	private static final String TAG = VehicleTabsFragment.class.getSimpleName();
    private MainActivity mMainActivity;

    // This means fragments 2 away from the current fragment is retained and
    // not destroyed. Default is 1
    private static final int PAGER_OFFSCREEN_LIMIT = 2;

    private ViewPager mViewPager;
	private static String[] mTabTitles;
    private FragmentStatePagerAdapter mAdapter;

    private final Handler mHandler = new Handler();
    private Runnable mRunPager;
    
    private boolean mCreated = false;

    @Override 
    public void onDestroyView() {
    	super.onDestroyView();
    	
    	Log.w(TAG, "Destroying");

    	ActionBar actionBar = mMainActivity.getActionBar();
    	actionBar.removeAllTabs();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        // Inflate the layout for this fragment
    	//Toast.makeText(getActivity(), "BMI onCreateView", Toast.LENGTH_SHORT).show();
        View rootView = inflater.inflate(R.layout.vehicle_tab_fragments, container, false);

		mTabTitles = getResources().getStringArray(R.array.tabs_title_array);

        // Set up the ViewPager, attaching the adapter and setting up a listener for when the
        // user swipes between sections.
        mViewPager = (ViewPager) rootView.findViewById(R.id.vehicles_pager);
        mViewPager.setOffscreenPageLimit(PAGER_OFFSCREEN_LIMIT);

        return rootView;
    }

	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
    	
    	if (mRunPager != null) {
    		mHandler.post(mRunPager);
    	}
        mCreated = true;

    	mMainActivity = (MainActivity) getActivity();

        // Set up the action bar.
        final ActionBar actionBar = mMainActivity.getActionBar();

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Log.w(TAG, "onActivityCreated setting change listener");

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);

                switch (position) {
                    case DBGlobals.TAB_IDX_STOLEN_VEHICLES:
                        mMainActivity.setSelectedFrag(SelectedFrag.STOLEN_VEHICLE);
                        break;
                    case DBGlobals.TAB_IDX_MY_VEHICLES:
                        mMainActivity.setSelectedFrag(SelectedFrag.MY_VEHICLES);
                        break;
                    case DBGlobals.TAB_IDX_MY_RIDES:
                        mMainActivity.setSelectedFrag(SelectedFrag.MY_RIDE);
                        break;
                    default:
                        mMainActivity.setSelectedFrag(SelectedFrag.MY_VEHICLES);
                        break;
                }
                mMainActivity.invalidateOptionsMenu();

                Log.d(TAG, "onPageSelected(): " + position);
            }
        });

        mAdapter = new AppSectionsPagerAdapter(getFragmentManager());

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        setAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunPager);
    }
    
    protected void setAdapter(FragmentStatePagerAdapter adapter) {
        mAdapter = adapter;
        mRunPager = new Runnable() {
 
            @Override
            public void run() {
            	Log.w(TAG, "Runnable is setting adapter");
                mViewPager.setAdapter(mAdapter);

                mViewPager.setCurrentItem(DBGlobals.TAB_IDX_MY_VEHICLES);
                mMainActivity.setSelectedFrag(SelectedFrag.MY_VEHICLES);
                mMainActivity.invalidateOptionsMenu();
            }
        };
        if (mCreated) {
        	mHandler.post(mRunPager);
        }

    }

    /**
    * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
    * sections of the app.
    */
    public static class AppSectionsPagerAdapter extends FragmentStatePagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;

            Log.d(TAG, "getItem(): " + i);

            switch (i) {
            case DBGlobals.TAB_IDX_STOLEN_VEHICLES:
                fragment = new StolenVehicleListFragment();
                break;
            case DBGlobals.TAB_IDX_MY_VEHICLES:
                fragment = new MyVehicleListFragment();
                break;
            case DBGlobals.TAB_IDX_MY_RIDES:
                fragment = new MyRideListFragment();
                break;
            default:
                fragment = new MyVehicleListFragment();
                break;
            }
            return fragment;
        }

        /**
         * When we call mAdapter.notifyDataSetChanged(); 
         * the ViewPager interrogates the adapter to determine what has changed in 
         * terms of positioning. We use this method to say that everything has 
         * changed so reprocess all your view positioning.
         */
        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        }
        
        @Override
        public int getCount() {
            return mTabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }
    }

	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
		
	}

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
	}
}
