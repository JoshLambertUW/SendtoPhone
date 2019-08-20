package com.stp.sendtophone;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendFragment extends Fragment {
    public static String TAG = "SendFragment";
    private EditText editText;
    SingleMessageFragment.SingleMessageDeletionListener callback;
    private FirebaseFunctions mFunctions;

    private static final String MESSAGE_ARG = "messagetosend";
    private static final String POSITION_ARG = "messagetosendposition";

    private String messageToSend;
    private Device destination;
    private int messageToSendPosition = -1;
    private static Context context;
    private Button btn;
    private Spinner spinner;

    public SendFragment() {
    }

    public static SendFragment newInstance(String messageToSend, int messageToSendPosition){
        SendFragment fragment = new SendFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_ARG, messageToSend);
        args.putInt(POSITION_ARG, messageToSendPosition);
        fragment.setArguments(args);
        return fragment;
    }

    public static SendFragment newInstance(String messageToSend){
        SendFragment fragment = new SendFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_ARG, messageToSend);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        context = getContext();
        if (getArguments() != null) {
            if (getArguments().containsKey(getString(R.string.outgoing_message_key))) {
                messageToSend = getArguments().getString(getString(R.string.outgoing_message_key));
            }
            if (getArguments().containsKey("messagetosendposition")){
                messageToSendPosition = getArguments().getInt("messagetosendposition");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_send, container, false);
        editText = view.findViewById(R.id.edit_text);
        btn = view.findViewById(R.id.button);
        spinner = view.findViewById(R.id.spinner);
        getDeviceList();
        if (messageToSend != null) editText.setText(messageToSend);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        return view;
    }

    //toDo: Get device list from prefs, implement download/update device list from database
    private void getDeviceList(){
        final List<Device> deviceList = SharedPrefHelper.getDeviceList(context);

        ArrayAdapter<Device> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, deviceList);
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                destination = (Device) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_send_message, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        ShareActionProvider myShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_draft:
                saveDraft();
                return true;
            case R.id.action_delete_draft:
                if (messageToSend != null && messageToSendPosition >= 0){
                    callback.singleMessageDeletion(context.getString(R.string.draft), messageToSendPosition);
                }
                else {
                    editText.getText().clear();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setOnMessageDeletionListener(SingleMessageFragment.SingleMessageDeletionListener callback) {
        this.callback = callback;
    }

    public interface SingleMessageDeletionListener {
        void singleMessageDeletion(int selectedmessagePosition);
    }

    private void saveDraft(){
        String messageDraft = editText.getText().toString().trim();
        if (messageDraft != null && messageDraft.length() > 0) {
            if (messageToSendPosition >= 0) {
                SharedPrefHelper.editMessage(context, messageDraft, context.getString(R.string.draft), messageToSendPosition);
            }
            else {
                SharedPrefHelper.saveNewMessage(context, messageDraft, context.getString(R.string.draft));
            }
            showSnackbar(R.string.save_draft_toast);
        }
        else {
            showSnackbar(R.string.save_draft_error_toast);
        }
    }

    private void sendMessage() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", messageToSend);
        data.put("selectedDevice", destination.getId());

        FirebaseFunctions.getInstance().getHttpsCallable("sendData")
                .call(data)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if (task.isSuccessful()) {
                            Gson g = new Gson();
                            JsonObject result = g.fromJson((String)task.getResult().getData(), JsonObject.class);
                            String error = "";
                            if (result.get("successCount").getAsInt() < 1){
                                error = result.get("results").getAsJsonArray().get(0).getAsJsonObject()
                                        .get("error").getAsJsonObject().get("code").getAsString();
                                if (error.equals("messaging/registration-token-not-registered") ||
                                error.equals("messaging/invalid-registration-token")){
                                    showSnackbar(R.string.device_register_error_toast);
                                }
                                else {
                                    showSnackbar(R.string.unknown_send_error_toast);
                                }
                            }
                            else {
                                showSnackbar(R.string.send_success_toast);
                            }
                            Map<String, Object> data = new HashMap<>();
                            data.put("message", messageToSend);
                            data.put("selectedDevice", destination);
                            data.put("error", error);
                            addToHistory(data);

                        } else {
                            showSnackbar(R.string.unknown_send_error_toast);
                        }
                    }
                });
    }

    private void addToHistory(Map<String, Object> data){
        Gson g = new Gson();
        g.toJson(data);
        SharedPrefHelper.saveNewMessage(context, g.toJson(data), "history");
    }

    private void showSnackbar(@StringRes int snackMessage) {
        Snackbar.make(getView().findViewById(android.R.id.content), snackMessage, Snackbar.LENGTH_LONG).show();
    }
}