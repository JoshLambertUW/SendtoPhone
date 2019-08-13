package com.stp.sendtophone;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefHelper {
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static Gson gson = new Gson();

    public static List<String> getMsgArrayList(Context context, String type) {
        String prefKey = (type == "inbox") ? context.getString(R.string.preference_messages_key) :
                context.getString(R.string.preference_drafts_key);
        sharedPreferences = context.getSharedPreferences(prefKey,
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
