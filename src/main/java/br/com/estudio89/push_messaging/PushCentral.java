package br.com.estudio89.push_messaging;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import br.com.estudio89.push_messaging.extras.GcmBroadcastReceiver;
import com.google.android.gms.gcm.GoogleCloudMessaging;


/**
 * Created by luccascorrea on 12/6/14.
 */
public class PushCentral extends IntentService {

    public PushCentral() {
        super("PushCentral");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {

            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {

                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String type = extras.getString("type");
                PushConfig pushConfig = PushConfig.getInstance();

                PushManager manager = pushConfig.getPushManager(type);
                if (manager != null) {
                    manager.processPushMessage(extras);
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

}
