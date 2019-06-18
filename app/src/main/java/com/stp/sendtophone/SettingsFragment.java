package com.stp.sendtophone;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

public class SettingsFragment extends PreferenceFragmentCompat {

    private FirebaseFirestore db;
    private String instanceId;
    private String userUid;

    public static String TAG = "SettingsFragment";



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return view;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        //Preference notifications = findPreference("notifications");
        EditTextPreference name = (EditTextPreference)findPreference("name");
        Preference deviceDeletion = findPreference("device_deletion");
        Preference logout = findPreference("logout");

        SharedPreferences app_preferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        String nickName = app_preferences.getString("name", "");
        name.setText(nickName);
        name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                db = FirebaseFirestore.getInstance();
                //instanceId = FirebaseInstanceId.getInstance().getId();
                instanceId = "dzO7A-tiiGA";
                userUid = FirebaseAuth.getInstance().getUid();
                String newName = ((String) newValue);
                DocumentReference docRef = db.collection("users").document(userUid).
                        collection("devices").document(instanceId);
                if (newName != "") {
                    docRef.update(getString(R.string.device_name_key), newName)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Device name successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error deleting document", e);
                                }
                            });
                }
                return true;
            }
        });

        deviceDeletion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                db = FirebaseFirestore.getInstance();
                instanceId = FirebaseInstanceId.getInstance().getId();
                userUid = FirebaseAuth.getInstance().getUid();

                db.collection("users").document(userUid).
                        collection("devices").document(instanceId).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });
                return true;
            }
        });

        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FirebaseAuth.getInstance().signOut();
                return true;
            }
        });
    }
}