package com.example.ridekeeper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * http://stackoverflow.com/questions/20088247/navigation-drawer-with-backword-compatibility-android/20088711#20088711
 *
 */
public class MenuListAdapter extends BaseAdapter {

    // Fields -----------------------------------------------------------------
    private Context mContext;
    private String[] mTitles;
    private int[] mIcons;
    private LayoutInflater mInflater;

    // Constructor ------------------------------------------------------------
    public MenuListAdapter(
            Context context, 
            String[] titles, 
            int[] icons){
        mContext = context;
        this.mTitles = titles;
        this.mIcons = icons;
        mInflater = (LayoutInflater)mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    // Accessors --------------------------------------------------------------
    @Override
    public int getCount(){
        return mTitles.length;
    }
    @Override
    public Object getItem(int position){
        return mTitles[position];
    }
    @Override
    public long getItemId(int position){
        return position;
    }

    // Methods ----------------------------------------------------------------
    public View getView(int position, View convertView, ViewGroup parent){

        ViewHolder viewHolder;

        // Only inflate the view if convertView is null
        if (convertView == null){
            viewHolder = new ViewHolder();
            if(mInflater!=null)
            {
            convertView = mInflater.inflate(
                    R.layout.drawer_list_item, parent, false);
            viewHolder.txtTitle = (TextView)convertView.findViewById(
                    R.id.title);
            viewHolder.imgIcon = (ImageView)convertView.findViewById(
                    R.id.icon);

            // This is the first time this view has been inflated,
            // so store the view holder in its tag fields
            convertView.setTag(viewHolder);
            }
            else
            {
                Log.i("........",""+null);
            }
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // Set the views fields as needed
        viewHolder.txtTitle.setText(mTitles[position]);
        viewHolder.imgIcon.setImageResource(mIcons[position]);

        return convertView;
    }

    // Classes ----------------------------------------------------------------
    static class ViewHolder {
        TextView txtTitle;
        ImageView imgIcon;
    }

}