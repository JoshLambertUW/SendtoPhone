package com.stp.sendtophone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Message> messageList;
    private static RecyclerViewClickListener itemListener;

    public interface RecyclerViewClickListener {
        void recyclerViewListClicked(View v, int position);
    }

    public RecyclerViewAdapter(List<Message> data, RecyclerViewClickListener itemListener) {
        RecyclerViewAdapter.itemListener = itemListener;
        this.messageList = data;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_message, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, int position) {
        String message = messageList.get(position).getBody();
        holder.mTextView.setText(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public Message getItem(int id) {
        Message message = messageList.get(id);
        return message;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mTextView = itemView.findViewById(R.id.tvMessage);
        }

        @Override
        public void onClick(View view) {
            itemListener.recyclerViewListClicked(view, getLayoutPosition());
        }
    }
}