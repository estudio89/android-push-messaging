package br.com.estudio89.push_messaging.injection;

import android.app.Application;
import android.content.Context;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(
		injects = {
				Context.class,
		},
		includes = {
			PushConfigModule.class
		}
)
public class AppContextModule {
	private Application app;
	
	public AppContextModule(Application app) {
		this.app = app;
	}
	
	@Provides @Singleton public Context provideAppContext() {
		return app;
	}
}
