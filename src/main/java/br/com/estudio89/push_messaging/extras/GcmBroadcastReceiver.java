package br.com.estudio89.push_messaging.extras;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import br.com.estudio89.push_messaging.PushCentral;

/**
 * Created by luccascorrea on 12/6/14.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that PushCentral will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                PushCentral.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
