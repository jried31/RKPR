/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ridekeeper;

import android.R.anim;
import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ridekeeper.DBGlobals.SelectedFrag;
import com.example.ridekeeper.qb.MyQBUser;
import com.example.ridekeeper.util.LocationMgr;
import com.example.ridekeeper.util.LocationUtils;
import com.example.ridekeeper.util.SystemChecker;
import com.example.ridekeeper.vehicles.EditVehicleFragment;
import com.example.ridekeeper.vehicles.StolenVehicleListFragment;
import com.example.ridekeeper.vehicles.VehicleTabsFragment;
import com.parse.ParseUser;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.chat.smack.SmackAndroid;

public class MainActivity extends FragmentActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

    public static final String APP_ID = "5815";
    private static final String AUTH_KEY = "8htqAuedCPgyW2z";
    private static final String AUTH_SECRET = "6whwzbRPrYSSbmg";

	public static Handler locationTimerHandler = new Handler();
	public static Runnable locationTimerRunnable;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mTitle;
	private String[] mDrawerMenuTitles;
	private MenuListAdapter mMenuAdapter;
	private int[] mIcons;

	private SmackAndroid mSmackAndroid;

	private SelectedFrag mSelectedFrag;

	public static LocationMgr mLocationMgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		App.isMainActivityRunning = true;
		//App.bReceiver.setRepeatingAlarm(this);
		mLocationMgr = new LocationMgr(this);
		ParseFunctions.init(mLocationMgr);

		initLocationUpdateTimer(this);

		mIcons = new int[] {
				R.drawable.ic_action_view_as_list,
				R.drawable.ic_action_person,
				R.drawable.ic_action_settings
		};

		mTitle = getTitle();
		mDrawerMenuTitles = getResources().getStringArray(R.array.drawer_menu_title_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mMenuAdapter = new MenuListAdapter(
                MainActivity.this, mDrawerMenuTitles, mIcons);

		mDrawerList.setAdapter(mMenuAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description for accessibility */
				R.string.drawer_close  /* "close drawer" description for accessibility */
				) {
			public void onDrawerClosed(View view) {
				actionBar.setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				actionBar.setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		initQuickblox();

		// How does this code work? When is savedInstanceState not null?
		if (savedInstanceState == null) {
			
            Log.d(TAG, "savedInstanceState null");

			if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()){
				selectItem(DBGlobals.DRAWER_IDX_VEHICLES); //Select VBS List Fragment as default if user is authenticated

                Log.d(TAG, "Parse user authenticated");
				
			} else {
				selectItem(DBGlobals.DRAWER_IDX_PROFILE); //Otherwise, Select My Profile Fragment so that user can login
                Log.d(TAG, "No authenticated parse user");
			}
		}
		SystemChecker.enableLocationProviders(this);
	}

	private void initQuickblox() {
    	// Register with QuickBlox server
        mSmackAndroid = SmackAndroid.init(this);

    	MyQBUser.initContext(getApplicationContext());
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
    	
		QBAuth.createSession(new QBCallback() {
			@Override
			public void onComplete(Result result) {
		        if (result.isSuccess()) {
		        	MyQBUser.sessionCreated = true;

                    // Login to Quickblox
                    if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()){
                        MyQBUser.signin(ParseUser.getCurrentUser().getUsername(), MyQBUser.DUMMY_PASSWORD);
                    }

		        } else {
		        	Toast.makeText(getApplicationContext(), "Error: " + result.getErrors(), Toast.LENGTH_SHORT).show();
		        }
			}
			@Override
			public void onComplete(Result result, Object object) {
			}
		});
	}
	
	public void setDrawerTitle(SelectedFrag frag) {
        setTitle(mDrawerMenuTitles[frag.ordinal()]);
	}

	public static void initLocationUpdateTimer(final Context context) {
		locationTimerRunnable = new Runnable() {
            @Override
            public void run() {
                //Periodically update phone's location to Parse server
                ParseFunctions.updateLocToParse(context); 
                locationTimerHandler.postDelayed(this, LocationUtils.LOCATION_UPDATE_RATE);
            }
        };

		locationTimerHandler.postDelayed(locationTimerRunnable, LocationUtils.LOCATION_UPDATE_RATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mSmackAndroid.onDestroy();

		mLocationMgr.stopPeriodicUpdates();
		mLocationMgr.disconnect();
		
		// Remove the runnable to stop updating location to Parse
		locationTimerHandler.removeCallbacks(locationTimerRunnable);
	}

	@Override
	protected void onStart() {
		super.onStart();

	    // The activity is either being restarted or started for the first time
	    // so this is where we should make sure that GPS is enabled
		if (!mLocationMgr.isConnected()) {
            mLocationMgr.connect();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

		menu.findItem(R.id.action_addvehicle).setVisible(false);
		menu.findItem(R.id.action_refreshvbslist).setVisible(false);
		
		if (mSelectedFrag == SelectedFrag.MY_VEHICLES) {
			menu.findItem(R.id.action_addvehicle).setVisible(!drawerOpen);
			Log.d(TAG, "SelectedFrag: MY_VEHICLES");
		} else if (mSelectedFrag == SelectedFrag.STOLEN_VEHICLE) {
			menu.findItem(R.id.action_refreshvbslist).setVisible(!drawerOpen);
			Log.d(TAG, "SelectedFrag: STOLEN_VEHICLE");
		}
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch(item.getItemId()) {
		
		case R.id.action_refreshvbslist:
			StolenVehicleListFragment.refreshList();
			return true;
		
		case R.id.action_addvehicle:
			EditVehicleFragment.addVehicle(getSupportFragmentManager());
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {

		// update the main content by replacing fragments
		Fragment fragment = null;

		switch (position) {
		case DBGlobals.DRAWER_IDX_VEHICLES:
			fragment = new VehicleTabsFragment();
			break;
		case DBGlobals.DRAWER_IDX_PROFILE:
			fragment = new MyProfileFragment();
			mSelectedFrag = SelectedFrag.MY_PROFILE;
			break;
		case DBGlobals.DRAWER_IDX_SETTINGS:
			fragment = new SettingsFragment();
			mSelectedFrag = SelectedFrag.SETTINGS;
			break;
		default:
			fragment = new MyProfileFragment();
			mSelectedFrag = SelectedFrag.MY_PROFILE;
			break;
		}

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerMenuTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}
	
	public void setSelectedFrag(SelectedFrag frag) {
		mSelectedFrag = frag;
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override()
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();

		//Stop alarm tone and vibration
		NotificationMgr.stopAlarmTone();
		NotificationMgr.stopVibration();
		
		//Start updating phone's location
		mLocationMgr.startPeriodicUpdates();
	}


	@Override
	protected void onPause() {
		App.isMainActivityRunning = false;
		super.onPause();
	}

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        Log.d(LocationUtils.GOOGLE_SERVICE, getString(R.string.resolved));
                    break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(LocationUtils.GOOGLE_SERVICE, getString(R.string.no_resolution));
                    break;
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
               Log.d(LocationUtils.GOOGLE_SERVICE,
                       getString(R.string.unknown_activity_request_code, requestCode));
               super.onActivityResult(requestCode, resultCode, intent);

               break;
        }
    }

	public void test(View v){
		Toast.makeText(getApplicationContext(), "START", Toast.LENGTH_SHORT).show();
		
		//LocationMgr.updateLocationInBackground(this, new LocationMgr.GetLocCallback() {
		//	@Override
		//	public void done() {
		//		Toast.makeText(getApplicationContext(), "GOT LOC", Toast.LENGTH_SHORT).show();
		//	}
		//});
		Location location = mLocationMgr.getLastGoodLocation();
		Toast.makeText(getApplicationContext(), 
				"GOT LOC lat/long: " + location.getLatitude() + "/" + location.getLongitude(), 
				Toast.LENGTH_SHORT).show();
		
	}
}