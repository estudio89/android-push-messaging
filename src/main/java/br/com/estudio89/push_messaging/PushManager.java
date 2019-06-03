package br.com.estudio89.push_messaging;

import java.util.Map;

/**
 * Created by luccascorrea on 12/6/14.
 */
public interface PushManager {
    public String getIdentifier();
    public void processPushMessage(Map<String, String> data);
}
