package com.stp.sendtophone;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// Features: Send from device

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 808;
    private Button loginButton;

    private static final String TAG = "firebaseMsgService";
    private static final String JOB_GROUP_NAME = "firestoreRequests";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userUid;
    private String instanceId;
    private DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSignIn();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isSignedIn()) {
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        } else {
            loginButton.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
        }
    }

    private void registerDevice(){
        userUid = FirebaseAuth.getInstance().getUid();
        instanceId = FirebaseInstanceId.getInstance().getId();
        docRef = db.collection("users").document(userUid).
                collection("devices").document(instanceId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                        String deviceName;
                                        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
                                        if (myDevice != null && myDevice.getName() != ""){
                                            deviceName = myDevice.getName();
                                        }
                                        else {
                                            deviceName = Build.MANUFACTURER + " " + Build.MODEL;
                                        }
                                        Map<String, Object> newDevice = new HashMap<>();
                                        newDevice.put(getString(R.string.messages_key), new ArrayList<String>());
                                        newDevice.put(getString(R.string.device_name_key), deviceName);
                                        String token = task.getResult().getToken();
                                        newDevice.put(getString(R.string.fcm_token_key), token);

                                        docRef.set(newDevice).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error writing document", e);
                                            }
                                        });
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "messageDocumentFailure", task.getException());
                }
            }
        });
    }

    private void handleSignInResponse(int resultCode, @Nullable Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == RESULT_OK) {
            registerDevice();
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        } else {
            // Sign in failed
            if (response == null) {
                return;
            }
            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }
            showSnackbar(R.string.unknown_error);
        }
    }

    public void startSignIn() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                        Arrays.asList(
                                new AuthUI.IdpConfig.GoogleBuilder().build()
                        )
                ).build(), RC_SIGN_IN);
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void showSnackbar(@StringRes int snackMessage) {
        Snackbar.make(findViewById(android.R.id.content), snackMessage, Snackbar.LENGTH_LONG).show();
    }
}