package br.com.estudio89.push_messaging;

import android.os.Bundle;

/**
 * Created by luccascorrea on 12/6/14.
 */
public interface PushManager {
    public String getIdentifier();
    public void processPushMessage(Bundle data);
}
