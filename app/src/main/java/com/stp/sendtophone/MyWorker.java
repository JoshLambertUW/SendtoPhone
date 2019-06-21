package com.stp.sendtophone;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MyWorker extends Worker {

    private static final String TAG = "MyWorker";
    private String messagesFromDB = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userUid = FirebaseAuth.getInstance().getUid();
    private String instanceId = FirebaseInstanceId.getInstance().getId();
    private DocumentReference docRef = db.collection("users").document(userUid).
            collection("devices").document(instanceId);

    private static Context context;
    Gson gson = new Gson();

    public MyWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        context = appContext;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get all new messages, convert new message array to JSON, add to local storage
        try {
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            final ArrayList<String> newMessages = (ArrayList<String>) document.get(context.getString(R.string.messages_key));
                            if (newMessages.size() > 0){
                                SharedPrefHelper.saveNewMessages(context, newMessages);
                                docRef.update(context.getString(R.string.messages_key), new ArrayList<String>());
                            }
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "messageDocumentFailure", task.getException());
                    }
                }
            });
        } catch (Exception e) {
            return Result.failure();
        }
        /*
        Data outputData = new Data.Builder()
                .putString("messagesFromDB", messagesFromDB)
                .build();
        */
        return Result.success();
    }
}
