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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
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

import com.parse.ParseUser;

public class MainActivity extends Activity {
	public static Handler locationTimerHandler = new Handler();
	public static Runnable locationTimerRunnable;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mDrawerMenuTitles;

	private enum SelectedFrag{
		STOLENVEHICLE, MYPROFILE, MYVEHICLES, SETTINGS, MYRIDE
	}
	private SelectedFrag selectedFrag;
	
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

		mTitle = mDrawerTitle = getTitle();
		mDrawerMenuTitles = getResources().getStringArray(R.array.drawer_menu_title_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mDrawerMenuTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

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
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			
			if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()){
				selectItem(2); //Select VBS List Fragment as default if user is authenticated
			}else{
				selectItem(1); //Otherwise, Select My Profile Fragment so that user can login
			}
		}

		enableLocationProviders();
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

    /** source: http://developmentality.wordpress.com/2009/10/31/android-dialog-box-tutorial/
     * https://stackoverflow.com/questions/12044552/android-activate-gps-with-alertdialog-how-to-wait-for-the-user-to-take-action
	 * source: https://stackoverflow.com/questions/10311834/android-dev-how-to-check-if-location-services-are-enabled
	 * @return
	 */
	private void enableLocationProviders() {
        boolean gps_enabled = false;
        boolean network_enabled = false;

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex){
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex){
	    	Toast.makeText(this, "NETWORK_PROVIDER exception", Toast.LENGTH_SHORT).show();
        }

        //if(!gps_enabled && !network_enabled){
        if(!gps_enabled && !network_enabled) {
        	buildAlertMessageNoLocationAccess();
        } else if (!gps_enabled) {
            buildAlertMessageNoGps();
	    	Toast.makeText(this, "GPS not enabled.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "GPS enabled.", Toast.LENGTH_SHORT).show();
        }
	}

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final Activity mainActivity = this;
        builder.setMessage("Yout GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						
                        Intent gpsOptionsIntent = new Intent(  
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
                            mainActivity.startActivity(gpsOptionsIntent);
                                }
				});
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
        builder.show();
    }
    
    private void buildAlertMessageNoLocationAccess() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(this.getResources().getString(R.string.gps_network_not_enabled));
        dialog.setCancelable(false);

        final Activity mainActivity = this;
        dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                mainActivity.startActivity(myIntent);
            }
        });
        dialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {

            }
        });
        dialog.show();
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
		//menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		menu.findItem(R.id.action_addvehicle).setVisible(false);
		menu.findItem(R.id.action_refreshvbslist).setVisible(false);
		
		if (selectedFrag == SelectedFrag.MYVEHICLES){
			//menu.findItem(R.id.action_websearch).setVisible(false);
			menu.findItem(R.id.action_addvehicle).setVisible(true & !drawerOpen);
		}else if (selectedFrag == SelectedFrag.STOLENVEHICLE) {
			menu.findItem(R.id.action_refreshvbslist).setVisible(true & !drawerOpen);
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
			EditVehicleFragment.addVehicle(getFragmentManager());
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

		Fragment fragment=null;

		switch(position){
		case DBGlobals.LIST_STOLEN_VEHICLES:
			fragment = new StolenVehicleListFragment();
			selectedFrag = SelectedFrag.STOLENVEHICLE;
			break;
		case DBGlobals.MY_PROFILE:
			fragment = new MyProfileFragment();
			selectedFrag = SelectedFrag.MYPROFILE;
			break;
		case DBGlobals.LIST_MY_VEHICLES:
			fragment = new MyVehicleListFragment();
			selectedFrag = SelectedFrag.MYVEHICLES;
			break;
		case DBGlobals.SETTINGS:
			fragment = new SettingsFragment();
			selectedFrag = SelectedFrag.SETTINGS;
			break;
        case DBGlobals.MY_RIDES:
            fragment = new MyRideListFragment();
            selectedFrag = SelectedFrag.MYRIDE;
            break;
		default:
			fragment = new MyProfileFragment();
		}

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerMenuTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
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