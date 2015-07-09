package br.com.estudio89.push_messaging;

import android.os.Bundle;
import br.com.estudio89.push_messaging.injection.PushInjection;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * Created by luccascorrea on 7/7/15.
 */
public class WebsocketHelper {
    private PushConfig pushConfig;
    private Socket socket;
    private boolean connected;

    public WebsocketHelper(PushConfig pushConfig) {
        this.pushConfig = pushConfig;
    }

    public static WebsocketHelper getInstance() {
        return PushInjection.get(WebsocketHelper.class);
    }

    public void startSocket() {
        String websocketUrl = pushConfig.getWebsocketUrl();
        final String registrationId = pushConfig.getRegistrationId();
        if ("".equals(websocketUrl) || registrationId == null || connected) {
            return;
        }

        try {
            if (socket == null) {
                IO.Options opts = new IO.Options();
                opts.reconnection = true;
                socket = IO.socket(websocketUrl, opts);

                // Connection
                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    public void call(Object... objects) {
                        connected = true;

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("identifier", "android");
                            jsonObject.put("deviceId", registrationId);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        socket.emit("register", jsonObject);
                    }
                });

                // Disconnection
                socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    public void call(Object... objects) {
                        connected = false;
                    }
                });

                // Subscribing to push manager events
                for (final String identifier:pushConfig.getPushManagerIdentifiers()) {
                    socket.on(identifier, new Emitter.Listener() {
                        public void call(Object... objects) {
                            JSONObject pushData = (JSONObject) objects[0];
                            Bundle bundle = jsonObjectToBundle(pushData);
                            PushCentral.processPushMessage(bundle);
                        }
                    });
                }
            }

            socket.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
        }
    }
    public boolean isConnected() {
        return connected;
    }

    private Bundle jsonObjectToBundle(JSONObject pushData) {
        Bundle bundle = new Bundle();
        Iterator<?> keys = pushData.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            try {
                bundle.putString(key, pushData.getString(key));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return bundle;
    }
}
