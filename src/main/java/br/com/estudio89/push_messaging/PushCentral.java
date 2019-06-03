package br.com.estudio89.push_messaging;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


/**
 * Created by luccascorrea on 12/6/14.
 *
 */
public class PushCentral extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        PushConfig pushConfig = PushConfig.getInstance();
        pushConfig.performRegistrationIfNeeded();
    }

    public static void processPushMessage(Map<String, String> pushData) {
        String type = pushData.get("type");
        PushConfig pushConfig = PushConfig.getInstance();

        try {
            long timestamp = Long.parseLong(pushData.get("timestamp"));
            long storedTimestamp = pushConfig.getTimestamp();

            if (timestamp <= storedTimestamp) {
                // This push message was received already
                return;
            } else {
                pushConfig.setTimestamp(timestamp);
            }
        } catch (NumberFormatException ignore) {}

        PushManager manager = pushConfig.getPushManager(type);
        if (manager != null) {
            manager.processPushMessage(pushData);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        if (!data.isEmpty()) {
            processPushMessage(data);
        }
    }
}
