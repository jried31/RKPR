package com.example.ridekeeper;

import com.example.ridekeeper.util.ImageConsumer;
import com.example.ridekeeper.util.ImageFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MyVehicleFormFragment extends Fragment implements ImageConsumer {

	private ImageView mImageView;
	private ImageFragment mImageFragment;
	
	private EditText make,
		model,
		year,
		trackerId,
		license;
	
	private Button change, btToggleTrackerId;
	public static final String USER_NAME="user_name";
	public static final String MY_USER_NAME_HACKFORNOW="jried";
	public static final String EMAIL="email";
	public static final String NAME="name";
	public static final String MAKE="make";
	public static final String MODEL="model";
	public static final String YEAR="year";
	public static final String TRACKERID="trackerId";
	public static final String LICENSE="license";
	
	public static final String PHOTO="photo";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
    	mImageFragment = ImageFragment.newInstance(this, null,
    			getFragmentManager());
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_vehicle_form, container, false);
        
        make = (EditText) view.findViewById(R.id.vehicle_item_make);
        model = (EditText) view.findViewById(R.id.vehicle_item_model);
        year = (EditText) view.findViewById(R.id.vehicle_item_year);
        license = (EditText) view.findViewById(R.id.vehicle_liscense);
        trackerId = (EditText) view.findViewById(R.id.trackerId);

        mImageView = (ImageView) view.findViewById(R.id.vehicle_item_photo);
        
        Bundle parameters = this.getArguments();
        if (parameters != null) {
            String makeVal = parameters.getString(MAKE),
                    modelVal = parameters.getString(MODEL),
                    yearVal = parameters.getString(YEAR),
                    licenseVal = parameters.getString(LICENSE),
                    trackerIdVal= parameters.getString(TRACKERID);
            
            byte []photoVal = parameters.getByteArray(PHOTO);
            
            make.setText(makeVal == null ? "Enter value":makeVal);
            model.setText(makeVal == null ? "Enter value":modelVal);
            year.setText(makeVal == null ? "Enter value":yearVal);
            license.setText(makeVal == null ? "Enter value":licenseVal);
            trackerId.setText(makeVal == null ? "Enter value":trackerIdVal);
            
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoVal, 0, photoVal.length);
            mImageView.setImageBitmap(bitmap);
        }
        

        //Setup for Changing Profile Picture
        change = (Button) view.findViewById(R.id.button_change);
        change.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
            	mImageFragment.showPhotoSelection();
            }
        });
        

        btToggleTrackerId = (Button) view.findViewById(R.id.toggleTracker);
        btToggleTrackerId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(trackerId.isEnabled())
                    trackerId.setEnabled(false);
                else 
                    trackerId.setEnabled(true);
            }
        });
        return view;
    }

    public void processBitmap(Bitmap bitmap) {
    	mImageView.setImageBitmap(bitmap);
    }
}
