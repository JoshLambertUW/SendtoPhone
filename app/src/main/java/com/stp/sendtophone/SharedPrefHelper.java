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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Map<String, String> getDeviceList(Context context){
        String prefKey = context.getString(R.string.preference_device_map_key);
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        String deviceMapJson = sharedPreferences.getString(prefKey,"");
        if (deviceMapJson.length() == 0) {
            Map<String, String> emptyMap = new HashMap<>();
            return emptyMap;
        }
        Map<String, String> deviceList = gson.fromJson(deviceMapJson,
                new TypeToken<Map<String, String>>(){}.getType());
        return deviceList;
    }

    public static Map<String, String> refreshDeviceList(final Context context) {
        db = FirebaseFirestore.getInstance();
        userUid = FirebaseAuth.getInstance().getUid();
        instanceId = FirebaseInstanceId.getInstance().getId();
        colRef = db.collection("users").document(userUid).
                collection("devices");
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        final String defaultIDPrefKey = context.getString(R.string.preference_default_device_id_key);
        final String deviceListPrefKey = context.getString(R.string.preference_device_map_key);

        try {
            colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        Map<String, String> updatedDeviceMap = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            updatedDeviceMap.put(document.getId(), (String) document.getData().get("deviceName"));
                        }

                        String defaultDeviceID = sharedPreferences.getString(defaultIDPrefKey, "");
                        if (defaultDeviceID.length() == 0 || !updatedDeviceMap.containsKey(defaultDeviceID)) {
                            if (updatedDeviceMap.size() > 0)
                                defaultDeviceID = updatedDeviceMap.get(updatedDeviceMap.keySet().toArray()[0]);
                        }

                        String deviceMapJson = gson.toJson(defaultDeviceID);
                        editor.putString(defaultIDPrefKey, defaultDeviceID);
                        editor.putString(deviceListPrefKey, deviceMapJson);
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
        String prefKey = (type == "inbox") ? context.getString(R.string.preference_messages_key) :
                context.getString(R.string.preference_drafts_key);
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
        String prefKey = (type == "inbox") ? context.getString(R.string.preference_messages_key) :
                context.getString(R.string.preference_drafts_key);
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
        String prefKey = (type == "inbox") ? context.getString(R.string.preference_messages_key) :
                context.getString(R.string.preference_drafts_key);
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
        String prefKey = (type == "inbox") ? context.getString(R.string.preference_messages_key) :
                context.getString(R.string.preference_drafts_key);
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.remove(prefKey);
        editor.commit();
    }

    public static void clearMessages(Context context, int position, String type) {
        String prefKey = (type == "inbox") ? context.getString(R.string.preference_messages_key) :
                context.getString(R.string.preference_drafts_key);
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
