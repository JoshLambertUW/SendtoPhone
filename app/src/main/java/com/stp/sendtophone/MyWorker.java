package com.stp.sendtophone;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;

public class MyWorker extends Worker {

    private static final String TAG = "MyWorker";
    private String messagesFromDB = "";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userUid = FirebaseAuth.getInstance().getUid();
    private String instanceId = FirebaseInstanceId.getInstance().getId();
    private CollectionReference colRef = db.collection("users").document(userUid).
            collection("devices").document(instanceId).collection("messages");

    private static Context context;

    public MyWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        context = appContext;
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get all new messages, save them into local storage, clear message queue
        try {
            colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        ArrayList<String> newMessages = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            newMessages.add((String) document.getData().get("message"));
                            document.getReference().delete();
                        }
                        SharedPrefHelper.saveNewMessages(context, newMessages);
                    } else {
                        Log.d(TAG, "messageDocumentFailure", task.getException());
                    }
                }
            });
        } catch (Exception e) {
            return Result.failure();
        }
        return Result.success();
    }
}
