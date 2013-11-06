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
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;

public class MainActivity extends Activity implements LocationListener {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mPlanetTitles;

	private enum SelectedFrag{
		VBS, MYPROFILE, MYVEHICLES, SETTINGS
	}
	private SelectedFrag selectedFrag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		App.isMainActivityRunning = true;
		HelperFuncs.bReceiver.setRepeatingAlarm(this);

		mTitle = mDrawerTitle = getTitle();
		mPlanetTitles = getResources().getStringArray(R.array.planets_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mPlanetTitles));
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
			selectItem(0);
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
		//menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		menu.findItem(R.id.action_addvehicle).setVisible(false);
		menu.findItem(R.id.action_refreshvbslist).setVisible(false);
		
		if (selectedFrag == SelectedFrag.MYVEHICLES){
			//menu.findItem(R.id.action_websearch).setVisible(false);
			menu.findItem(R.id.action_addvehicle).setVisible(true & !drawerOpen);
		}else if (selectedFrag == SelectedFrag.VBS) {
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
		/*
		case R.id.action_websearch:
			// create intent to perform web search for this planet
			Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
			intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
			// catch event that there's no activity to handle intent
			if (intent.resolveActivity(getPackageManager()) != null) {
				startActivity(intent);
			} else {
				Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
			}
			return true;
		*/
		case R.id.action_refreshvbslist:
			VBSListFragment.mHandler.post(VBSListFragment.runQueryVBS);
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
		case DBGlobals.VBS_LIST:
			fragment = new VBSListFragment();
			selectedFrag = SelectedFrag.VBS;
			break;
		case DBGlobals.MY_PROFILE:
			fragment = new MyProfileFragment();
			selectedFrag = SelectedFrag.MYPROFILE;
			break;
		case DBGlobals.MY_VEHICLE:
			fragment = new MyVehicleListFragment();
			selectedFrag = SelectedFrag.MYVEHICLES;
			break;
		case DBGlobals.SETTINGS:
			fragment = new TestFragment();
			selectedFrag = SelectedFrag.SETTINGS;
			break;
		default:
			fragment = new MyProfileFragment();
			//Bundle args = new Bundle();
			//args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
			//fragment.setArguments(args);
		}

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mPlanetTitles[position]);
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
		// TODO Auto-generated method stub
		super.onResume();

		//Stop alarm tone and vibration
		HelperFuncs.stopAlarmTone();
		HelperFuncs.stopVibration();

		//stop broadcastReceiver when app is active
		//HelperFuncs.bReceiver.cancelAlarm(this);

		//Start updating phone's location
		HelperFuncs.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}


	@Override
	protected void onPause() {
		//start broadcastReceiver when app is active
		//HelperFuncs.bReceiver.setRepeatingAlarm(this);

		//stop updating phone's location
		HelperFuncs.locationManager.removeUpdates(this);

		App.isMainActivityRunning = false;

		super.onPause();
	}

	@Override
	public void onLocationChanged(Location location) {
		//Toast.makeText(this, "Location updated!", Toast.LENGTH_SHORT).show();
		HelperFuncs.myLocation = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	//Functions for testing:
	// Start/Stop MyBroadcastReceiver.routineCheck();
	public void startBroadcastReceiver(View v){
		//HelperFuncs.startBroadcastReceiver(getApplicationContext());
	}
	public void stopBroadcastReceiver(View v){
		//HelperFuncs.stopBroadcastReceiver(getApplicationContext());
	}

	public void test(View v){
		Toast.makeText(getApplicationContext(), "START", Toast.LENGTH_SHORT).show();
		
		HelperFuncs.updatetLocation_inBackground(this, new HelperFuncs.GetLocCallback() {
			@Override
			public void done() {
				Toast.makeText(getApplicationContext(), "GOT LOC", Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
	/*
	private void action_AddVehicle(){
    	FragmentTransaction ft = getFragmentManager().beginTransaction();
    	Fragment prev = getFragmentManager().findFragmentByTag("Add Vehicle Dialog");
    	if (prev != null) {
    		ft.remove(prev);
    	}
    	ft.addToBackStack(null);
    	
    	DialogFragment editVehicleFrag = new EditVehicleFragment();
    	Bundle args = new Bundle();
    	args.putString("mode", "add");
    	editVehicleFrag.setArguments(args);
    	editVehicleFrag.show(ft, "Add Vehicle Dialog");
	}
	*/
}