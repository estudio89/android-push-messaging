package br.com.estudio89.push_messaging.injection;

import android.content.Context;
import br.com.estudio89.push_messaging.PushConfig;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;


@Module(
		injects = {
				PushConfig.class
		},
		complete = false
		)
public class PushConfigModule {

	@Provides @Singleton
	public PushConfig providePushConfig(Context context) {
		return new PushConfig(context);
	}
	
}
