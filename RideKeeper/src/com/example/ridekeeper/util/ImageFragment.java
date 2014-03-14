package com.example.ridekeeper.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.ridekeeper.DBGlobals;
import com.example.ridekeeper.R;
import com.example.ridekeeper.qb.chat.ChatFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Fragment class that handles image selection logic from camera
 * or gallery.
 */
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
	private static final LinearLayout.LayoutParams sImageSmallLayoutParams = 
			new LinearLayout.LayoutParams(170, 170);

	private static final String IMAGE_TIME_FORMAT = "yyyyMMdd_HHmmss";
	private static final SimpleDateFormat sImageTimeFormat=  new SimpleDateFormat(IMAGE_TIME_FORMAT);

	private ImageConsumer mImageConsumer;
	private ViewGroup mContainer;

	private Activity mMainActivity;

	private Uri mImageCaptureUri;

    /**
     * Saves image to device
     * @param context
     * @param photoData
     * @param photoPrefix
     * @param id
     */
	public static void savePhotoLocally (
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
	
	public void setContainer(ViewGroup container) {
		mContainer = container;
	}
	
	public static ImageFragment newInstance(ImageConsumer consumer, ViewGroup container,
			FragmentManager fragManager) {
		ImageFragment imageFragment = new ImageFragment();

		imageFragment.mImageConsumer = consumer;
		imageFragment.mContainer = container;

		fragManager.beginTransaction().add(imageFragment, TAG).commit();
		return imageFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMainActivity = getActivity();
	}

	/**
	 *  Handle data after activity returns.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case REQUEST_CODE_SELECT_FROM_GALLERY:
			mImageCaptureUri = data.getData();
			cropImage();
			break;

		case REQUEST_CODE_TAKE_FROM_CAMERA:
			cropImage();
			break;

		case REQUEST_CODE_CROP_PHOTO:
			Bundle extras = data.getExtras();
			
			// Set the picture image in UI
			if (extras != null) {
				Bitmap bitmap = (Bitmap) extras.getParcelable("data");
				mImageConsumer.processBitmap(bitmap);
			}
			
			// Delete temporary file from camera
            File f = new File(mImageCaptureUri.getPath());
            if (f.exists()) {
                Log.wtf(TAG, "Deleting file: " + mImageCaptureUri.getPath());
                f.delete();
            }

			break;
		}
	}
	
	/**
	 *  Crop and resize the image for profile
	 */
	private void cropImage() {
		// Use existing crop activity.
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(mImageCaptureUri, IMAGE_TYPE_UNSPECIFIED);

		// Specify image size
		//intent.putExtra("outputX", 100);
		//intent.putExtra("outputY", 100);

		//// Specify aspect ratio, 1:1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("crop", true);
		intent.putExtra("return-data", true);
		// REQUEST_CODE_CROP_PHOTO is an integer tag you defined to
		// identify the activity in onActivityResult() when it returns
		startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
	}
	
	/**
	 * For taking picture from camera/gallery
	 * @param item
	 */
	private void onPhotoPickerItemSelected(int item) {
		Intent intent;

		switch(item){
		case ID_PHOTO_PICKER_FROM_CAMERA:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			// Use temporary file to store the camera pic
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

			break;

		case ID_PHOTO_PICKER_FROM_GALLERY:
			intent = new Intent(Intent.ACTION_PICK);

			intent.setType(IMAGE_TYPE_UNSPECIFIED);

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

			break;

		default:
			break;
		}
	}
	
	/**
	 * Display alert dialog to user to choose an option to select
	 * an image.
	 */
	public void showPhotoSelection() {
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
	
	public View.OnClickListener getToggleImageSizeListener(final ChatFragment chatFragment) {
        View.OnClickListener toggleImageSizeListener = new View.OnClickListener() {
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
                        Log.d(TAG, "mContainer null, width: " + containerWidth);
                        
                    } else {
                        containerWidth = mContainer.getWidth();
                        Log.d(TAG, "mContainer width: " + containerWidth);
                    }
                    int h = imageView.getHeight() * (containerWidth / imageView.getWidth());
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(containerWidth, h ));
                } else {
                    // shrink the image
                    imageView.setLayoutParams(sImageSmallLayoutParams);
                }
                //chatFragment.scrollDown();
            }
        };
	
        return toggleImageSizeListener;
	}
	
	private View.OnLongClickListener mSaveImageToGallery = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			ImageView imageView = (ImageView) v;
			Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
			
			// Create image file
            File imageFile = createAlbumImageFile();

			if (storeBitmap(bitmap, imageFile)) {
                // Add file to gallery
                Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");

                mImageCaptureUri = Uri.fromFile(imageFile);
                mediaScanIntent.setData(mImageCaptureUri);

                mMainActivity.sendBroadcast(mediaScanIntent);
                
                Toast.makeText(mMainActivity, "Saved to gallery", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
	};
	
	public View.OnLongClickListener getSaveImageToGalleryListener() {
		return mSaveImageToGallery;
	}
	
	public static boolean storeBitmap(Bitmap bitmap, File imageFile) {
        try {
            imageFile.createNewFile();
            FileOutputStream ostream = new FileOutputStream(imageFile);
            bitmap.compress(CompressFormat.PNG, IMAGE_COMPRESS_QUALITY, ostream);
            Log.d(TAG, "Storing bitmap to file: " + imageFile.getAbsolutePath());
            ostream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
			
		return true;
	}
	
	public static File createAlbumImageFile() {
        String timestamp = sImageTimeFormat.format(new Date()).toString();
        String imageFileName = "IMG" + timestamp;

        File albumDir = getAlbumDir();

        return new File(albumDir + "/" + imageFileName + IMAGE_FILE_SUFFIX);
	}

    private static File getAlbumDir() {
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
            Log.v(TAG, "getAlbumDir(): External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }
    
	
	/**
	 * Create a temporary File object in the Android public storage directory
	 * @return
	 */
	private static File createTmpImageFile() {
		File external = Environment.getExternalStorageDirectory();
		String fileName = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg"; 

		Log.d(TAG, "createTmpImageFile(): " + external.getAbsolutePath() + "/" + fileName);

		return new File(external, fileName);
	}
}
