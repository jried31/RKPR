package com.example.ridekeeper.qb.chat;

import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ridekeeper.DBGlobals;
import com.example.ridekeeper.R;

public class ChatAdapter extends BaseAdapter {

    private static final String DATE_FORMAT = 
    		"MMM d, yyyy " + DBGlobals.UNICODE_BULLET + " h:mm a";

    private final List<ChatMessage> chatMessages;
    private OnClickListener mResizeImageListener;
    private OnLongClickListener mSaveImageListener;
    private Context context;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages,
    		OnClickListener resizeListener, OnLongClickListener saveImageListener) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.mResizeImageListener = resizeListener;
        this.mSaveImageListener = saveImageListener;
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public ChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatMessage chatMessage = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.chat_list_item_message, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        setAlignment(holder, chatMessage.isIncoming());

        if (chatMessage.isImage()) {
        	holder.imageView.setImageBitmap(chatMessage.getBitmap());
            holder.imageView.setOnClickListener(mResizeImageListener);
            holder.imageView.setOnLongClickListener(mSaveImageListener);
            holder.txtMessage.setVisibility(View.GONE);
        } else {
            holder.txtMessage.setVisibility(View.VISIBLE);
            holder.txtMessage.setText(chatMessage.getText());
            holder.imageView.setImageDrawable(null);
        }

        if (chatMessage.getSender() != null) {
            holder.txtInfo.setText(
            		chatMessage.getSender() + " " + DBGlobals.UNICODE_BULLET + " " + 
                    getTimeText(chatMessage.getTime()));
        } else {
            holder.txtInfo.setText(getTimeText(chatMessage.getTime()));
        }

        return convertView;
    }

    public void add(ChatMessage message) {
        chatMessages.add(message);
    }

    public void add(List<ChatMessage> messages) {
        chatMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isIncoming) {
        if (isIncoming) {
            holder.contentWithBG.setBackgroundResource(R.drawable.left_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.contentWithBG.setLayoutParams(layoutParams);
            //holder.contentWithBG.setBackgroundColor(context.getResources().getColor(R.color.green));

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtMessage.setLayoutParams(layoutParams);

            holder.imageView.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            holder.txtInfo.setLayoutParams(layoutParams);
            holder.txtInfo.setPadding(10, 0, 0, 0);
        } else {
            holder.contentWithBG.setBackgroundResource(R.drawable.right_message_bg);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.contentWithBG.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.content.setLayoutParams(lp);
            layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtMessage.setLayoutParams(layoutParams);

            holder.imageView.setLayoutParams(layoutParams);

            layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            holder.txtInfo.setLayoutParams(layoutParams);
            holder.txtInfo.setPadding(0, 0, 10, 0);
        }
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        holder.imageView = (ImageView) v.findViewById(R.id.chatImage);
        return holder;
    }

    private String getTimeText(Date date) {
        return DateFormat.format(DATE_FORMAT, date).toString();
    }

    private static class ViewHolder {
        public TextView txtMessage;
        public TextView txtInfo;
        public LinearLayout content;
        public LinearLayout contentWithBG;
        public ImageView imageView;
    }
}
