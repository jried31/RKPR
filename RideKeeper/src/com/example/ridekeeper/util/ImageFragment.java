package com.example.ridekeeper.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.ridekeeper.DBGlobals;
import com.example.ridekeeper.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageFragment extends Fragment {
	public static final String TAG = ImageFragment.class.getSimpleName();

	public static final int ID_PHOTO_PICKER_FROM_CAMERA = 0;
	public static final int ID_PHOTO_PICKER_FROM_GALLERY = 1;
	public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 100;
	public static final int REQUEST_CODE_CROP_PHOTO = 101;
	public static final int REQUEST_CODE_SELECT_FROM_GALLERY = 102;

	private static final String IMAGE_FILE_SUFFIX = ".png";
	private static final String IMAGE_TYPE_UNSPECIFIED = "image/*";
	private static final int IMAGE_COMPRESS_QUALITY = 100;

    // Set gravity to center in OnCreateView
	private static final LayoutParams sImageSmallLayoutParams = new LayoutParams(170, 170);

    @SuppressLint("SimpleDateFormat")
	private SimpleDateFormat mImageTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

	private ImageConsumer mImageConsumer;
	private ViewGroup mContainer;

	private Activity mMainActivity;

	private Uri mImageCaptureUri;
	private boolean mIsTakenFromCamera;
	private File mAlbumDir;

    /**
     * Saves image to device
     * @param context
     * @param photoData
     * @param photoPrefix
     * @param id
     */
	public static void savePhotoLocally(
			Context context, 
			byte[] photoData, 
			String photoPrefix,
			String id) {
		if (photoData != null) {
			try {
				Log.i(TAG, "App's files dir: " + context.getFilesDir().getAbsolutePath());

				FileOutputStream fos = context.openFileOutput(
						photoPrefix + id + IMAGE_FILE_SUFFIX, 
						Activity.MODE_PRIVATE);

				fos.write(photoData);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e2){
				e2.printStackTrace();
			}
		}
	}
	
	
	public static ImageFragment newInstance(ImageConsumer consumer, ViewGroup container) {
		ImageFragment imageFragment = new ImageFragment();

		imageFragment.mImageConsumer = consumer;
		imageFragment.mContainer = container;

		return imageFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMainActivity = getActivity();
	}

	// Handle data after activity returns.
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_OK){
			return;
		}

		switch (requestCode) {
		case REQUEST_CODE_SELECT_FROM_GALLERY:
			mImageCaptureUri = data.getData();
			cropImage();
			break;

		case REQUEST_CODE_TAKE_FROM_CAMERA:
			// Send image taken from camera for cropping
			cropImage();
			break;

		case REQUEST_CODE_CROP_PHOTO:
			// Update image view after image crop

			Bundle extras = data.getExtras();
			
			// Set the picture image in UI
			if (extras != null) {
				Bitmap bitmap = (Bitmap) extras.getParcelable("data");
				mImageConsumer.processBitmap(bitmap);
			}

			// Delete temporary image taken by camera after crop.
			// TODO: doesn't gallery create a new temp file also?
			if (mIsTakenFromCamera) {
				File f = new File(mImageCaptureUri.getPath());
				if (f.exists())
					f.delete();
			}
			break;
		}
	}
	
	//get a photo from Camera or gallery
	public void getPhotoIntoImage() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
		DialogInterface.OnClickListener dlistener;
		builder.setTitle(R.string.photo_picker_title);
		dlistener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				onPhotoPickerItemSelected(item);
			}
		};

		builder.setItems(R.array.photo_picker_items, dlistener);
		builder.create().show();
	}
	
	// Crop and resize the image for profile
	private void cropImage() {
		// Use existing crop activity.
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(mImageCaptureUri, IMAGE_TYPE_UNSPECIFIED);

		// Specify image size
		intent.putExtra("outputX", 100);
		intent.putExtra("outputY", 100);

		// Specify aspect ratio, 1:1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		// REQUEST_CODE_CROP_PHOTO is an integer tag you defined to
		// identify the activity in onActivityResult() when it returns
		startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
	}
	
	//For taking picture from camera / gallery:
	private void onPhotoPickerItemSelected(int item) {
		Intent intent;
		mIsTakenFromCamera = false;

		switch(item){
		case ID_PHOTO_PICKER_FROM_CAMERA:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			mImageCaptureUri = Uri.fromFile(createTmpImageFile());

			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
					mImageCaptureUri);
			intent.putExtra("return-data", true);

			try {
				startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
			} catch (ActivityNotFoundException e) {
				// TODO: give error message to user that they have no camera
				// Better yet, don't allow camera option at all if no activity is
				// found (check earlier on view create and don't display option)
				Toast.makeText(mMainActivity, "No camera found", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

			mIsTakenFromCamera = true;
			break;

		case ID_PHOTO_PICKER_FROM_GALLERY:
		// TODO: check that the gallery will also create tmp file, need to delete as well
			intent = new Intent(Intent.ACTION_PICK);

			intent.setType(IMAGE_TYPE_UNSPECIFIED);
			mImageCaptureUri = Uri.fromFile(createTmpImageFile());

			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
					mImageCaptureUri);
			intent.putExtra("return-data", true);

			try {
				startActivityForResult(intent, REQUEST_CODE_SELECT_FROM_GALLERY);
			} catch (ActivityNotFoundException e){
				// TODO: give error message to user that they have no gallery
				// Better yet, don't allow option at all if no activity is
				// found (check earlier on view create and don't display option)
				Toast.makeText(mMainActivity, "No gallery found", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
			mIsTakenFromCamera = false;
			break;

		default:
			break;
		}
	}
	
	public void showPhotoSelection() {
		if (mMainActivity == null) {
			mMainActivity = getActivity();
		}
		final Activity parent = mMainActivity;

		AlertDialog.Builder builder = new AlertDialog.Builder(parent);

		DialogInterface.OnClickListener dlistener;
		builder.setTitle(R.string.photo_picker_title);

		dlistener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				onPhotoPickerItemSelected(item);
			}
		};

		builder.setItems(R.array.photo_picker_items, dlistener);
		builder.create().show();
	}
	
	private View.OnClickListener mToggleImageSizeListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			ImageView imageView = (ImageView) view;
			
			// Enlarge the image
			if (imageView.getLayoutParams() == sImageSmallLayoutParams) {
				int containerWidth;
				if (mContainer == null) {
                    Display display = mMainActivity.getWindowManager().getDefaultDisplay();
                    Point deviceSize = new Point();
                    display.getSize(deviceSize);
                    containerWidth = deviceSize.x;
					
				} else {
					containerWidth = mContainer.getWidth();
				}
				int h = imageView.getHeight() * (containerWidth / imageView.getWidth());
				imageView.setLayoutParams(new LayoutParams(containerWidth, h ));
			} else {
				// shrink the image
				imageView.setLayoutParams(sImageSmallLayoutParams);
			}
		}
	};
	
	public View.OnClickListener getToggleImageSizeListener() {
		return mToggleImageSizeListener;
	}
	
	private View.OnLongClickListener mSaveImageToGallery = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			ImageView iv = (ImageView) v;
			Bitmap bitmap = iv.getDrawingCache(true);
			
			// Create image file
            File imageFile = createAlbumImageFile();

			if (storeBitmap(bitmap, imageFile)) {
                // Add file to gallery
                Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");

                Uri contentUri = Uri.fromFile(imageFile);
                mediaScanIntent.setData(contentUri);

                mMainActivity.sendBroadcast(mediaScanIntent);
                
                Toast.makeText(mMainActivity, "Saved to gallery", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
	};
	
	public View.OnLongClickListener getSaveImageToGalleryListener() {
		return mSaveImageToGallery;
	}
	
	private boolean storeBitmap(Bitmap bitmap, File imageFile) {
        try {
            imageFile.createNewFile();
            FileOutputStream ostream = new FileOutputStream(imageFile);
            bitmap.compress(CompressFormat.PNG, IMAGE_COMPRESS_QUALITY, ostream);
            ostream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
			
		return true;
	}
	
	private File createAlbumImageFile() {
        String timeStamp = mImageTimeFormat.format(new Date());
        String imageFileName = "IMG" + timeStamp + "_";

        if (mAlbumDir == null) {
            mAlbumDir = getAlbumDir();
        }

        return new File(mAlbumDir + "/" + imageFileName + IMAGE_FILE_SUFFIX);
	}
	
	/**
	 * Create a temporary File object in the Android public storage directory
	 * @return
	 */
	private File createTmpImageFile() {
		return new File(
				Environment.getExternalStorageDirectory(), "tmp_" +
                String.valueOf(System.currentTimeMillis()) + ".jpg");
	}

	
    private File getAlbumDir() {
    	File storageDir = new File(
    			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
    			DBGlobals.APP_NAME
    			);

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("RideKeeper", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }
    
}
