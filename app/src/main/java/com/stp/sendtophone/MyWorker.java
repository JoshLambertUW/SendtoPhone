package com.stp.sendtophone;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
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
                        ArrayList<Message> newMessages = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Message newMessage = new Message((String) document.getData().get("message"));
                            newMessages.add(newMessage);
                            document.getReference().delete();
                        }
                        SharedPrefHelper.saveNewMessages(context, newMessages, 0);
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
