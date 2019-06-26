package com.stp.sendtophone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class firebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "firebaseMsgService";
    private static final String JOB_GROUP_NAME = "firestoreRequests";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userUid = FirebaseAuth.getInstance().getUid();
    private String instanceId = FirebaseInstanceId.getInstance().getId();
    private DocumentReference docRef = db.collection("users").document(userUid).
            collection("devices").document(instanceId);

    Gson gson = new Gson();
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            if (remoteMessage.getData().containsKey("message")) {
                String message = remoteMessage.getData().get("message");
                if (message.length() > 40) sendNotification(message.substring(0,40));
                else sendLargeNotification(message.substring(0,37) + "...", message.substring(0, 447) + "...");
                handleNow(message);
            } else {
                sendLargeNotification(remoteMessage.getData().get("messagePreview"), remoteMessage.getData().get("messagePreviewExtended"));
                scheduleJob();
            }
        }
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        sendRegistrationToServer(token);
    }

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(MyWorker.class)
                        .setConstraints(constraints)
                        .build();
        final WorkManager workManager = WorkManager.getInstance();
        WorkContinuation work = workManager.beginUniqueWork(JOB_GROUP_NAME, ExistingWorkPolicy.APPEND, request);
        work.enqueue();
    }

    /**
     * @param message
     */
    private void handleNow(String message) {
        SharedPrefHelper.saveNewMessage(this, message);
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */

    private void sendRegistrationToServer(String token) {
        String deviceName;
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        if (myDevice != null && myDevice.getName() != ""){
            deviceName = myDevice.getName();
        }
        else {
            deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        }
        Map<String, Object> newDevice = new HashMap<>();
        newDevice.put(getString(R.string.fcm_token_key), token);
        newDevice.put(getString(R.string.messages_key), new ArrayList<String>());
        newDevice.put(getString(R.string.device_name_key), deviceName);

        docRef = db.collection("users").document(userUid).
                collection("devices").document(instanceId);

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

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messagePreview received from FCM message body.
     */

    private void sendLargeNotification(String messagePreview, String messagePreviewExtended) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messagePreview)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messagePreviewExtended));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Send to Phone notification channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }


    private void sendNotification(String messagePreview) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle(getString(R.string.fcm_message))
                        .setContentText(messagePreview)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Send to Phone notification channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}