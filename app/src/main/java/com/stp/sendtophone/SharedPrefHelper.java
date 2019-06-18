package com.stp.sendtophone;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefHelper {
    private static SharedPreferences sharedPreferences ;
    private static SharedPreferences.Editor editor;
    private static Gson gson = new Gson();

    public static List<String> getMsgArrayList(Context context){
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String messageListJson = sharedPreferences.getString(context.getString(R.string.preference_messages_key), "");
        if (messageListJson.isEmpty()){
            List<String> emptyString = new ArrayList<String>();
            return emptyString;
        }
        List<String> messageList = gson.fromJson(messageListJson,
                new TypeToken<ArrayList<String>>(){}.getType());
        return messageList;
    }

    public static void saveNewMessages(Context context, List<String> newMessages){
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        List<String> messageList = getMsgArrayList(context);
        String messageListJson;

        messageList.addAll(newMessages);
        messageListJson = gson.toJson(messageList);
        editor.putString(context.getString(R.string.preference_messages_key), messageListJson);
        editor.commit();
    }

    public static void saveNewMessage(Context context, String newMessage){
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        List<String> messageList = getMsgArrayList(context);
        String messageListJson;

        messageList.add(newMessage);
        messageListJson = gson.toJson(messageList);
        editor.putString(context.getString(R.string.preference_messages_key), messageListJson);
        editor.commit();
    }

    public static void clearMessages(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.remove(context.getString(R.string.preference_messages_key));
        editor.commit();
    }

    public static void clearMessages(Context context, int position) {
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key),
                MODE_PRIVATE);
        editor = sharedPreferences.edit();
        List<String> messageList = getMsgArrayList(context);
        String messageListJson;
        if (position < messageList.size()) {
            messageList.remove(position);
            messageListJson = gson.toJson(messageList);
            editor.putString(context.getString(R.string.preference_messages_key), messageListJson);
            editor.commit();
        }
    }

}
