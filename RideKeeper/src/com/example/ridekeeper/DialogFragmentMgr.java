package com.example.ridekeeper;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

public class DialogFragmentMgr {
	public static void showDialogFragment(
			FragmentActivity activity, 
			DialogFragment fragment, 
			String dialogName, 
			boolean cancelable,
			Bundle bundle) {

    	FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
    	Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(dialogName);

    	if (prev != null) {
    		ft.remove(prev);
    	}

    	ft.addToBackStack(null);

    	fragment.setCancelable(cancelable);
    	if (bundle != null){
    		fragment.setArguments(bundle);
    	}

    	fragment.show(ft, dialogName);
	}
}
