package com.stp.sendtophone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class firebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "firebaseMsgService";
    private static final String JOB_GROUP_NAME = "firestoreRequests";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userUid;
    private String instanceId;
    private DocumentReference docRef;

    /**
     * Called when message is received.
     * If entire message was received in the remoteMessage (less than 4KB),
     * a preview is generated and message is saved.
     * If not, a worker thread is queued to download from database.
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
                if (message.length() < 40) sendNotification(message);
                else if (message.length() > 40 && message.length() < 447)
                    sendNotification(message.substring(0, 40));
                else
                    sendLargeNotification(message.substring(0, 37) + "...", message.substring(0, 447) + "...");
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
     * Worker threads will be completed sequentially in this implementation.
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
     * Saves a received message from FCM
     *
     * @param message
     */
    private void handleNow(String message) {
        Message newMessage = new Message(message);

        SharedPrefHelper.saveNewMessage(this, newMessage, 0);
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Saves supplied FCM token to existing account
     * FCM token may be automatically updated
     *
     * @param token The new token.
     */

    private void sendRegistrationToServer(String token) {
        userUid = FirebaseAuth.getInstance().getUid();
        instanceId = FirebaseInstanceId.getInstance().getId();
        docRef = db.collection("users").document(userUid).
                collection("devices").document(instanceId);

        docRef.update(getString(R.string.fcm_token_key), token);
    }

    /**
     * Create and show a large notification containing a preview of the FCM message.
     * Used for previews larger than 40 characters
     *
     * @param messagePreview generated or received from FCM message body.
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

    /**
     * Create and show a normal notification containing a preview of the FCM message.
     * Used when message is less than 40 characters.
     *
     * @param messagePreview generated or received from FCM message body.
     */

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