package com.murtaza.gram.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.murtaza.gram.R;
import com.murtaza.gram.model.Chat;
import com.murtaza.gram.util.MurtazaGramUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private List<Chat> chats = new ArrayList<>();
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    public void addChat(Chat chat) {
        this.chats.add(0, chat);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return chats.size();
    }

    @Override
    public Chat getItem(int position) {
        return chats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return chats.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        }

        Chat chat = chats.get(position);

        ImageView ivAvatar = convertView.findViewById(R.id.ivAvatar);
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvLastMessage = convertView.findViewById(R.id.tvLastMessage);
        TextView tvTime = convertView.findViewById(R.id.tvTime);
        TextView tvUnreadBadge = convertView.findViewById(R.id.tvUnreadBadge);
        View onlineIndicator = convertView.findViewById(R.id.onlineIndicator);

        tvName.setText(chat.getDisplayName());

        if (chat.lastMessageText != null && !chat.lastMessageText.isEmpty()) {
            tvLastMessage.setText(chat.lastMessageText);
        } else {
            tvLastMessage.setText(chat.getChatType());
        }

        if (chat.lastMessageDate > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            tvTime.setText(sdf.format(new java.util.Date(chat.lastMessageDate * 1000)));
        } else {
            tvTime.setText("");
        }

        if (chat.unreadCount > 0) {
            tvUnreadBadge.setVisibility(View.VISIBLE);
            tvUnreadBadge.setText(String.valueOf(chat.unreadCount));
        } else {
            tvUnreadBadge.setVisibility(View.GONE);
        }

        if (chat.isOnline) {
            onlineIndicator.setVisibility(View.VISIBLE);
        } else {
            onlineIndicator.setVisibility(View.GONE);
        }

        int bgColor = MurtazaGramUtils.getAvatarColor(chat.id);
        ivAvatar.setBackgroundColor(bgColor);

        if (chat.isBot) {
            tvName.setTypeface(null, Typeface.ITALIC);
        } else {
            tvName.setTypeface(null, Typeface.BOLD);
        }

        convertView.setOnClickListener(v -> {
            if (listener != null) listener.onChatClick(chat);
        });

        return convertView;
    }
}
