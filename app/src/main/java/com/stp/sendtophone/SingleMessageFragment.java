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
    private TextView sentStatusTextView;
    private TextView sentDeviceTextView;
    SingleMessageDeletionListener callback;

    private static final String MESSAGE_ARG = "selectedMessage";
    private static final String POSITION_ARG = "selectedMessagePosition";
    private static final String STATUS_ARG = "sentMessageStatus";
    private static final String DEVICE_NAME_ARG = "deviceName";

    private String selectedMessage;
    private int selectedMessagePosition;
    private String sentMessageStatus;
    private String deviceName;

    public SingleMessageFragment() {
    }

    public static SingleMessageFragment newInstance(String selectedMessage, int selectedMessagePosition) {
        SingleMessageFragment fragment = new SingleMessageFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_ARG, selectedMessage);
        args.putInt(POSITION_ARG, selectedMessagePosition);
        fragment.setArguments(args);
        return fragment;
    }

    public static SingleMessageFragment newInstance(String selectedMessage, int selectedMessagePosition, String sentMessageStatus, String deviceName) {
        SingleMessageFragment fragment = new SingleMessageFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_ARG, selectedMessage);
        args.putInt(POSITION_ARG, selectedMessagePosition);
        args.putString(STATUS_ARG, sentMessageStatus);
        args.putString(DEVICE_NAME_ARG, deviceName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle arguments = getArguments();

        if (arguments != null) {
            selectedMessage = arguments.getString(MESSAGE_ARG);
            selectedMessagePosition = arguments.getInt(POSITION_ARG);
            if (arguments.containsKey(STATUS_ARG)) sentMessageStatus = arguments.getString(STATUS_ARG);
            if (arguments.containsKey(DEVICE_NAME_ARG)) deviceName = arguments.getString(DEVICE_NAME_ARG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_single_message, container, false);
        messageTextView = view.findViewById(R.id.single_message_text_view);
        messageTextView.setText(selectedMessage);
        if (sentMessageStatus != null && sentMessageStatus.length() > 0){
            sentStatusTextView = view.findViewById(R.id.status_text_view);
            sentStatusTextView.setText(sentMessageStatus);
        }
        if (deviceName != null && deviceName.length() > 0){
            sentDeviceTextView = view.findViewById(R.id.device_text_view);
            sentDeviceTextView.setText(deviceName);
        }
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
                callback.singleMessageDeletion(selectedMessagePosition);
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
        void singleMessageDeletion(int selectedMessagePosition);
    }

    private void showSnackbar(@StringRes int snackMessage) {
        Snackbar.make(getView().findViewById(android.R.id.content), snackMessage, Snackbar.LENGTH_LONG).show();
    }
}

