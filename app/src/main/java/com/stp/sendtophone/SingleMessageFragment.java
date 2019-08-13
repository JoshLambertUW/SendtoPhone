package com.stp.sendtophone;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import static android.content.Context.CLIPBOARD_SERVICE;

public class SingleMessageFragment extends Fragment {
    public static String TAG = "MessageFragment";
    private TextView messageTextView;
    SingleMessageDeletionListener callback;

    private static final String MESSAGE_ARG = "selectedmessage";
    private static final String POSITION_ARG = "selectedmessageposition";

    private String selectedMessage;
    private int selectedmessagePosition;

    public SingleMessageFragment() {
    }

    public static SingleMessageFragment newInstance(String selectedMessage, int selectedmessagePosition) {
        SingleMessageFragment fragment = new SingleMessageFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_ARG, selectedMessage);
        args.putInt(POSITION_ARG, selectedmessagePosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            selectedMessage = getArguments().getString(getString(R.string.selected_message_key));
            selectedmessagePosition = getArguments().getInt(getString(R.string.selected_message_position));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_single_message, container, false);
        messageTextView = view.findViewById(R.id.single_message_text_view);
        messageTextView.setText(selectedMessage);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_single_message, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        ShareActionProvider myShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        Intent myShareIntent = new Intent(Intent.ACTION_SEND);
        myShareIntent.setType("text/plain");
        myShareIntent.putExtra(Intent.EXTRA_TEXT, selectedMessage);
        myShareActionProvider.setShareIntent(myShareIntent);
    }

    public void addToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Message", selectedMessage);
        clipboard.setPrimaryClip(clip);
        showSnackbar(R.string.added_to_clipboard);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                callback.singleMessageDeletion("inbox", selectedmessagePosition);
                return true;
            case R.id.action_copy:
                addToClipboard();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setOnMessageDeletionListener(SingleMessageDeletionListener callback) {
        this.callback = callback;
    }

    public interface SingleMessageDeletionListener {
        void singleMessageDeletion(String type, int selectedmessagePosition);
    }

    private void showSnackbar(@StringRes int snackMessage) {
        Snackbar.make(getView().findViewById(android.R.id.content), snackMessage, Snackbar.LENGTH_LONG).show();
    }
}

