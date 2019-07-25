package com.stp.sendtophone;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<String> messageList;
    private LayoutInflater mInflater;
    private Context context;
    private static RecyclerViewClickListener itemListener;

    public interface RecyclerViewClickListener {
        void recyclerViewListClicked(View v, int position);
    }

    public RecyclerViewAdapter(Context context, List<String> data, RecyclerViewClickListener itemListener) {
        this.context = context;
        RecyclerViewAdapter.itemListener = itemListener;
        this.mInflater = LayoutInflater.from(context);
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
        String message = messageList.get(position);
        holder.mTextView.setText(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public String getItem(int id) {
        return messageList.get(id);
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