package com.murtaza.gram.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.murtaza.gram.R;
import com.murtaza.gram.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends BaseAdapter {

    private List<Message> messages = new ArrayList<>();
    private OnMessageLongClickListener longClickListener;

    public interface OnMessageLongClickListener {
        void onMessageLongClick(Message message, View view);
    }

    public MessageAdapter(OnMessageLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        notifyDataSetChanged();
    }

    public void updateMessage(Message updated) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).messageId == updated.messageId) {
                messages.set(i, updated);
                notifyDataSetChanged();
                return;
            }
        }
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).messageId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        }

        Message msg = messages.get(position);

        LinearLayout bubbleContainer = convertView.findViewById(R.id.bubbleContainer);
        TextView tvSenderName = convertView.findViewById(R.id.tvSenderName);
        TextView tvMessageText = convertView.findViewById(R.id.tvMessageText);
        TextView tvTranslatedText = convertView.findViewById(R.id.tvTranslatedText);
        TextView tvTime = convertView.findViewById(R.id.tvTime);

        if (msg.isOutgoing) {
            bubbleContainer.setBackgroundResource(R.drawable.bubble_out);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bubbleContainer.getLayoutParams();
            params.gravity = Gravity.END;
            bubbleContainer.setLayoutParams(params);
            tvMessageText.setTextColor(parent.getContext().getResources().getColor(R.color.bubble_out_text));
        } else {
            bubbleContainer.setBackgroundResource(R.drawable.bubble_in);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bubbleContainer.getLayoutParams();
            params.gravity = Gravity.START;
            bubbleContainer.setLayoutParams(params);
            tvMessageText.setTextColor(parent.getContext().getResources().getColor(R.color.bubble_in_text));
        }

        if (!msg.isOutgoing && msg.senderName != null && !msg.senderName.isEmpty()) {
            tvSenderName.setVisibility(View.VISIBLE);
            tvSenderName.setText(msg.senderName);
        } else {
            tvSenderName.setVisibility(View.GONE);
        }

        tvMessageText.setText(msg.getDisplayText());
        tvMessageText.setTextIsSelectable(true);

        if (msg.translatedText != null && !msg.translatedText.isEmpty()) {
            tvTranslatedText.setVisibility(View.VISIBLE);
            tvTranslatedText.setText(msg.translatedText);
        } else {
            tvTranslatedText.setVisibility(View.GONE);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvTime.setText(sdf.format(new Date(msg.date * 1000)));

        convertView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onMessageLongClick(msg, v);
                return true;
            }
            return false;
        });

        return convertView;
    }
}
