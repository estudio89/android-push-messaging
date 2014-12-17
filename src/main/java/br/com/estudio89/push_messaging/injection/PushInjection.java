package br.com.estudio89.push_messaging.injection;

import android.app.Application;
import android.content.Context;
import br.com.estudio89.push_messaging.PushConfig;
import dagger.ObjectGraph;

import java.util.Arrays;
import java.util.List;

public class PushInjection {
	private static ObjectGraph graph;

	/**
	 * Realiza a injeção de dependência nas classes
	 * do módulo de sincronização.
	 * 
	 * @param application
	 */
	public static void init(Application application, String configFile) {

		graph = ObjectGraph.create(getModules(application).toArray());


		// Kickstarting injection
		Context context = graph.get(Context.class);
		PushConfig pushConfig = graph.get(PushConfig.class);
		pushConfig.setConfigFile(configFile);

	}

	/**
	 * Injeta dependências.
	 * 
	 * @param object
	 */
	public static void inject(Object object) {
		graph.inject(object);
	}
	
	/**
	 * Retorna os módulos responsáveis pelas injeções de
	 * dependência no projeto.
	 * 
	 * @param application
	 * @return
	 */
	private static List<Object> getModules(Application application) {
		return Arrays.<Object>asList(new AppContextModule(application));
	}
	
	/**
	 * Retorna uma classe com suas dependências satisfeitas.
	 * 
	 * @param k classe desejada.
	 * @return
	 */
	public static <E> E get(Class<E> k) {
		return graph.get(k);
	}
}
