package br.com.estudio89.push_messaging;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import br.com.estudio89.grabber.Grabber;
import br.com.estudio89.grabber.annotation.GrabberFactory;
import br.com.estudio89.grabber.annotation.InstantiationListener;
import br.com.estudio89.push_messaging.injection.PushInjection;
import br.com.estudio89.syncing.ServerComm;
import br.com.estudio89.syncing.SyncConfig;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by luccascorrea on 12/6/14.
 */
public class PushConfig {
    private static String TAG = "Push";
    private static String PUSH_PREFERENCES_FILE = "br.com.estudio89.push_messaging.preferences";
    private static String KEY_REGISTRATION_ID = "REGISTRATION_ID";
    private static String KEY_TIMESTAMP = "TIMESTAMP";
    private static String REGISTRATION_URL_SUFFIX = "push_messaging/register-device/";

    Context context;
    private static String gcmSenderId;
    private static String configFile;
    private String baseURL;
    private static String serverRegistrationUrl;
    private static LinkedHashMap<String, PushManager> pushManagersByIdentifier = new LinkedHashMap<String, PushManager>();
    private static String websocketUrl;
    private boolean disableWebsocket;


    public static PushConfig getInstance() {
        return PushInjection.get(PushConfig.class);
    }

    public PushConfig(Context context) {
        this.context = context;
    }

    public void setConfigFile(String configFile, String baseURL) {
        this.configFile = configFile;
        this.baseURL = baseURL;
        if (!baseURL.endsWith("/")) {
            this.baseURL = this.baseURL + "/";
        }
        this.loadSettings();
    }

    public Set<String> getPushManagerIdentifiers() {
        return pushManagersByIdentifier.keySet();
    }
    public Collection<PushManager> getPushManagers() {
        return pushManagersByIdentifier.values();
    }
    public PushManager getPushManager(String identifier) {
        return pushManagersByIdentifier.get(identifier);
    }

    public void setRegistrationId(String registrationId) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                PUSH_PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_REGISTRATION_ID, registrationId);
        editor.commit();
    }

    /**
     * Retorna o registration id ou null caso não tenha sido obtido.
     * @return
     */
    public String getRegistrationId() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                PUSH_PREFERENCES_FILE, Context.MODE_PRIVATE);
        String registrationId = sharedPref.getString(KEY_REGISTRATION_ID,
                null);
        return registrationId;
    }

    public void setTimestamp(long timestamp) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                PUSH_PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(KEY_TIMESTAMP, timestamp);
        editor.commit();
    }

    /**
     * Retorna o registration id ou null caso não tenha sido obtido.
     * @return
     */
    public long getTimestamp() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                PUSH_PREFERENCES_FILE, Context.MODE_PRIVATE);
        long timestamp = sharedPref.getLong(KEY_TIMESTAMP,
                0);
        return timestamp;
    }

    public String getWebsocketUrl() {
        return websocketUrl;
    }

    public void erasePushPreferences() {
        SharedPreferences sharedPref = context.getSharedPreferences(
                PUSH_PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        WebsocketHelper.getInstance().disconnect();
    }

    public void performRegistrationIfNeeded() {
        SyncConfig syncConfig = SyncConfig.getInstance();
        String token = syncConfig.getAuthToken();
        Log.d(TAG, "performRegistrationIfNeeded token = " + token + " isEmpty = " + TextUtils.isEmpty(token));
        if (!TextUtils.isEmpty(token) && !runningRegistration) {
            Log.d(TAG, "INICIANDO REGISTRO DO DEVICE");
            new RegisterDevice().execute();
        }
    }


    public boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        9000).show();
            } else {
                activity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Carrega as configurações a partir do arquivo json syncing-config.json
     */
    private void loadSettings() {
        try {
            InputStream inputStream = context.getAssets().open(configFile);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String jsonString = new String(buffer, "UTF-8");
            JSONObject jsonConfig = new JSONObject(jsonString).getJSONObject("pushMessaging");

            gcmSenderId = jsonConfig.getString("gcmSenderId");
            serverRegistrationUrl = this.baseURL + REGISTRATION_URL_SUFFIX;
            websocketUrl = jsonConfig.optString("websocketUrl", this.baseURL);
            disableWebsocket = jsonConfig.optBoolean("disableWebsocket", false);

            GrabberFactory<PushManager> syncManagerGrabberFactory;
            try {
                syncManagerGrabberFactory = Grabber.getFactory(PushManager.class);
                syncManagerGrabberFactory.listAll(new InstantiationListener<PushManager>() {
                    @Override
                    public void onNewInstance(PushManager pushManager) {
                        String identifier = pushManager.getIdentifier();
                        pushManagersByIdentifier.put(identifier,pushManager);
                    }
                });
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean runningRegistration = false;
    class RegisterDevice extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                runningRegistration = true;
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                String registrationId = gcm.register(gcmSenderId);
                sendRegistrationIdToBackend(registrationId);
            } catch (IOException e) {

            } finally {
                runningRegistration = false;
            }
            return null;
        }

        private void sendRegistrationIdToBackend(String registrationId) {
            try {
                SyncConfig syncConfig = SyncConfig.getInstance();
                String token = syncConfig.getAuthToken();

                if (token == null) {
                    return;
                }

                JSONObject parameters = new JSONObject();
                parameters.put("token", token);
                parameters.put("old_registration_id", getRegistrationId());
                parameters.put("registration_id", registrationId);
                parameters.put("platform", "android");
                try {
                    parameters.put("app_version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
                } catch (PackageManager.NameNotFoundException e) {}

                ServerComm serverComm = ServerComm.getInstance();
                serverComm.post(serverRegistrationUrl, parameters);
                setRegistrationId(registrationId);
                syncConfig.setDeviceId(registrationId);

                // Starting websocket connection
                if (!disableWebsocket) {
                    WebsocketHelper websocketHelper = WebsocketHelper.getInstance();
                    websocketHelper.startSocket();
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {

            }
        }
    }
}
