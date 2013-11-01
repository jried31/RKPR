package com.example.ridekeeper;

import java.util.ArrayList;

import com.example.ridekeeper.R;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MyVehicleListFragment extends android.app.ListFragment {
	private VehicleArrayAdapter adapter;
	Vehicle[] values;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		//JERRID: GRAB THE VEHICLE VALUES FROM THE DB
		
		ArrayList<Vehicle> values = new ArrayList<Vehicle>();
		
		/*
		values.add(new Vehicle());
		
		values.get(0).setLicense("dfds-dfdss");
		values.get(0).setMake("Honda");
		values.get(0).setModel("Accord");
		values.get(0).setYear(1998);
		values.get(0).setStatus("Armed");
		values.get(0).setPhotoURI(getString(R.string.photo_filename));
		*/
		
		adapter = new VehicleArrayAdapter(getActivity(), values);
		
	    setListAdapter(adapter);
	    
		/* listView = getListView();
	    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	    listView.setOnItemLongClickListener(new OnItemLongClickListener() {
	        @Override
	        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

	        	//SHOW THE Vehicle Profile Edit Menu
	        	Toast.makeText(getActivity(), "LIST ITEM CLICKED @ Position "+position ,4000).show();
        		
	          
	          // start the CAB using the ActionMode.Callback defined above
	          //mActionMode = MyListActivityActionbar.this.startActionMode(mActionModeCallback);
	          //view.setSelected(true);
	          return true;
	        }
	      });*/
	    this.setMenuVisibility(true);
	    registerForContextMenu(getListView());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		//Creates the Edit menu for the specific option
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.menu_vehicle, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	 
	    switch (item.getItemId()) {
	    case R.id.edit_item:
	    	//JERRID: OPEN THE VEHICLE EDIT IMENU
	    	return true;
	    case R.id.remove_item:
	    	//JERRID: REMOVE VEHICLE RECORD
	        //((VehicleArrayAdapter)getListAdapter()).remove(values[info.position]).notifyDataSetChanged();
	    	adapter.remove( adapter.getItem(info.position) );
	    	adapter.notifyDataSetChanged();
	        return true;
	    }
	    return false;
	}
	
}
