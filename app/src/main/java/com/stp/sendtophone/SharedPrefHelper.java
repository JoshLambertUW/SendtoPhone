package com.stp.sendtophone;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefHelper {
    private static final String TAG = "SharedPrefHelper";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Gson gson = new Gson();
    private static FirebaseFirestore db;
    private static String userUid;
    private static String instanceId;
    private static CollectionReference colRef;

    //toDO: Add sent history list

    public static List<Device> getDeviceList(Context context){
        String prefKey = context.getString(R.string.preference_device_map_key);
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        String deviceListJson = sharedPreferences.getString(prefKey,"");
        if (deviceListJson.length() == 0) {
            List<Device> emptyList = new ArrayList<>();
            return emptyList;
        }
        List<Device> deviceList = gson.fromJson(deviceListJson,
                new TypeToken<ArrayList<Device>>(){}.getType());
        return deviceList;
    }

    public static List<Device> refreshDeviceList(Context context) {
        db = FirebaseFirestore.getInstance();
        userUid = FirebaseAuth.getInstance().getUid();
        instanceId = FirebaseInstanceId.getInstance().getId();
        colRef = db.collection("users").document(userUid).
                collection("devices");
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        final String deviceListPrefKey = context.getString(R.string.preference_device_map_key);

        try {
            colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Device> updatedDeviceList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Device device = new Device((String) document.getData().get("deviceName"), document.getId());
                            updatedDeviceList.add(device);
                        }

                        String deviceListJson = gson.toJson(updatedDeviceList);
                        editor.putString(deviceListPrefKey, deviceListJson);
                        editor.commit();

                    } else {
                        Log.d(TAG, "messageDocumentFailure", task.getException());
                    }
                }
            });
        } catch (Exception e) {

        }
        return getDeviceList(context);
    }

    public static Device getDefaultDevice(Context context){
        String prefKey = context.getString(R.string.preference_default_device_key);
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        String defaultDeviceJson = sharedPreferences.getString(prefKey,"");

        if (defaultDeviceJson.length() == 0) {
            List<Device> deviceList = refreshDeviceList(context);
            if (deviceList.size() == 0) {
                Device device = new Device();
                return device;
            } else {
                Device device = deviceList.get(0);
                defaultDeviceJson = gson.toJson(device);
                editor.putString(prefKey, defaultDeviceJson);
                editor.commit();
                return device;
            }
        }
        else {
            Device defaultDevice = gson.fromJson(defaultDeviceJson,
                    new TypeToken<Device>(){}.getType());
            return defaultDevice;
        }
    }

    public static void setDefaultDevice(Context context, Device device){
        String prefKey = context.getString(R.string.preference_default_device_key);
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);

        String defaultDeviceJson = gson.toJson(device);
        editor.putString(prefKey, defaultDeviceJson);
        editor.commit();
    }

    public static List<String> getMsgArrayList(Context context, String type) {
        String prefKey = "";
        switch (type){
            case "inbox":
                prefKey = context.getString(R.string.preference_messages_key);
                break;
            case "draft":
                prefKey = context.getString(R.string.preference_drafts_key);
                break;
            case "history":
                prefKey = context.getString(R.string.preference_history_key);
                break;
        }
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        String messageListJson = sharedPreferences.getString(prefKey,"");
        if (messageListJson.isEmpty()) {
            List<String> emptyString = new ArrayList<String>();
            return emptyString;
        }
        List<String> messageList = gson.fromJson(messageListJson,
                new TypeToken<ArrayList<String>>() {
                }.getType());
        return messageList;
    }

    public static void saveNewMessages(Context context, List<String> newMessages, String type) {
        String prefKey = "";
        switch (type){
            case "inbox":
                prefKey = context.getString(R.string.preference_messages_key);
                break;
            case "draft":
                prefKey = context.getString(R.string.preference_drafts_key);
                break;
            case "history":
                prefKey = context.getString(R.string.preference_history_key);
                break;
        }

        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        List<String> messageList = getMsgArrayList(context, type);
        String messageListJson;

        messageList.addAll(newMessages);
        messageListJson = gson.toJson(messageList);
        editor.putString(prefKey, messageListJson);
        editor.commit();
    }

    public static void saveNewMessage(Context context, String newMessage, String type) {
        String prefKey = "";
        switch (type){
            case "inbox":
                prefKey = context.getString(R.string.preference_messages_key);
                break;
            case "draft":
                prefKey = context.getString(R.string.preference_drafts_key);
                break;
            case "history":
                prefKey = context.getString(R.string.preference_history_key);
                break;
        }
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        List<String> messageList = getMsgArrayList(context, type);
        String messageListJson;

        messageList.add(newMessage);
        messageListJson = gson.toJson(messageList);
        editor.putString(prefKey, messageListJson);
        editor.commit();
    }

    public static void editMessage(Context context, String newMessage, String type, int index) {
        String prefKey = "";
        switch (type){
            case "inbox":
                prefKey = context.getString(R.string.preference_messages_key);
                break;
            case "draft":
                prefKey = context.getString(R.string.preference_drafts_key);
                break;
            case "history":
                prefKey = context.getString(R.string.preference_history_key);
                break;
        }
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        List<String> messageList = getMsgArrayList(context, type);
        String messageListJson;

        messageList.set(index, newMessage);
        messageListJson = gson.toJson(messageList);
        editor.putString(prefKey, messageListJson);
        editor.commit();
    }

    public static void clearMessages(Context context, String type) {
        String prefKey = "";
        switch (type){
            case "inbox":
                prefKey = context.getString(R.string.preference_messages_key);
                break;
            case "draft":
                prefKey = context.getString(R.string.preference_drafts_key);
                break;
            case "history":
                prefKey = context.getString(R.string.preference_history_key);
                break;
        }
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.remove(prefKey);
        editor.commit();
    }

    public static void clearMessages(Context context, int position, String type) {
        String prefKey = "";
        switch (type){
            case "inbox":
                prefKey = context.getString(R.string.preference_messages_key);
                break;
            case "draft":
                prefKey = context.getString(R.string.preference_drafts_key);
                break;
            case "history":
                prefKey = context.getString(R.string.preference_history_key);
                break;
        }
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        List<String> messageList = getMsgArrayList(context, type);
        String messageListJson;
        if (position < messageList.size()) {
            messageList.remove(position);
            messageListJson = gson.toJson(messageList);
            editor.putString(prefKey, messageListJson);
            editor.commit();
        }
    }

}
