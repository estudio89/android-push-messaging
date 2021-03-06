package br.com.estudio89.push_messaging.injection;

import android.app.Application;
import android.content.Context;
import br.com.estudio89.push_messaging.PushConfig;
import br.com.estudio89.push_messaging.WebsocketHelper;

import java.util.ArrayList;
import java.util.List;

public class PushInjection {
    private static List<Object> graph = new ArrayList<Object>();

	/**
	 * Realiza a injeção de dependência nas classes
	 * do módulo de sincronização.
	 * 
	 * @param application
	 */
	public static void init(Application application, String configFile, String baseURL) {

		// Kickstarting injection
        executeInjection(application);

		PushConfig pushConfig = get(PushConfig.class);
		pushConfig.setConfigFile(configFile, baseURL);

	}

    private static void executeInjection(Application application) {
        Context context = application;

        PushConfig pushConfig = new PushConfig(context);
        WebsocketHelper websocketHelper = new WebsocketHelper(pushConfig);

        graph.add(context);
        graph.add(pushConfig);
        graph.add(websocketHelper);

    }

	
	/**
	 * Retorna uma classe com suas dependências satisfeitas.
	 * 
	 * @param k classe desejada.
	 * @return
	 */
	public static <E> E get(Class<E> k) {
        for (Object obj:graph) {
            if (k.isAssignableFrom(obj.getClass())) {
                return (E) obj;
            }
        }
        return null;
	}
}
