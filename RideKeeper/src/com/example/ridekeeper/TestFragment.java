package com.example.ridekeeper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class TestFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view =  inflater.inflate(R.layout.fragment_test, container, false);

		Button test = (Button) view.findViewById(R.id.button_test);
		test.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(v.getContext(), "START", Toast.LENGTH_SHORT).show();
				HelperFuncs.updatetLocation_inBackground(getActivity(), new HelperFuncs.GetLocCallback() {
					@Override
					public void done() {
						Toast.makeText(getActivity(), "GOT LOC", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
		
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
	}
}
