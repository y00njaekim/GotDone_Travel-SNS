package com.google.maps.android.utils.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {

    private static final int TYPE_MESSAGE_SENT = 0;
    private static final int TYPE_MESSAGE_RECEIVED = 1;
    private static final int TYPE_IMAGE_SENT = 2;
    private static final int TYPE_IMAGE_RECEIVED = 3;

    private LayoutInflater inflater;
    private List<List<String>> messages = new ArrayList<>();

    public MessageAdapter (LayoutInflater inflater) {
        this.inflater = inflater;
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {

        TextView messageTxt;

        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);

            messageTxt = itemView.findViewById(R.id.sentTxt);
        }
    }

    private class SentImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public SentImageHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        TextView nameTxt, messageTxt;

        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);

            nameTxt = itemView.findViewById(R.id.nameTxt);
            messageTxt = itemView.findViewById(R.id.receivedTxt);
        }
    }

    private class ReceivedImageHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView nameTxt;

        public ReceivedImageHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            nameTxt = itemView.findViewById(R.id.nameTxt);

        }
    }

    @Override
    public int getItemViewType(int position) {
        List<String> message = messages.get(position);
        if (message.get(0).equals("send")) {
            if (message.get(1).equals("msg"))
                return TYPE_MESSAGE_SENT;
            else
                return TYPE_IMAGE_SENT;

        } else {
            if (message.get(1).equals("msg"))
                return TYPE_MESSAGE_RECEIVED;
            else
                return TYPE_IMAGE_RECEIVED;

        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case TYPE_MESSAGE_SENT:
                view = inflater.inflate(R.layout.item_sent_message, parent, false);
                return new SentMessageHolder(view);

            case TYPE_MESSAGE_RECEIVED:
                view = inflater.inflate(R.layout.item_received_message, parent, false);
                return new ReceivedMessageHolder(view);

            case TYPE_IMAGE_SENT:
                view = inflater.inflate(R.layout.item_sent_image, parent, false);
                return new SentImageHolder(view);

            case TYPE_IMAGE_RECEIVED:
                view = inflater.inflate(R.layout.item_recieved_photo, parent, false);
                return new ReceivedImageHolder(view);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        List<String> message = messages.get(position);

        if (message.get(0).equals("send")) {

            if (message.get(1).equals("msg")) {

                SentMessageHolder messageHolder = (SentMessageHolder) holder;
                messageHolder.messageTxt.setText(message.get(4));

            } else {

                SentImageHolder imageHolder = (SentImageHolder) holder;
                Bitmap bitmap = getBitmapFromString(message.get(4));

                imageHolder.imageView.setImageBitmap(bitmap);

            }

        } else {

            if (message.get(1).equals("msg")) {

                ReceivedMessageHolder messageHolder = (ReceivedMessageHolder) holder;
                messageHolder.nameTxt.setText(message.get(3));
                messageHolder.messageTxt.setText(message.get(4));

            } else {

                ReceivedImageHolder imageHolder = (ReceivedImageHolder) holder;
                imageHolder.nameTxt.setText(message.get(3));

                Bitmap bitmap = getBitmapFromString(message.get(4));
                imageHolder.imageView.setImageBitmap(bitmap);

            }
        }
    }

    private Bitmap getBitmapFromString(String image) {

        byte[] bytes = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addItem (List<String> list) {
        messages.add(list);
        notifyDataSetChanged();
    }

}