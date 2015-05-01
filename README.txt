1. Fazer que o servidor sempre envie um parâmetro extra na mensagem push que define o seu tipo ("type":"...")

2. Incluir a biblioteca Google Play Services no projeto. Seguir os passos descritos em: https://developer.android.com/google/play-services/setup.html

3. Codificar classes que implementam a interface PushManager, sendo que cada uma retorna um identifier correspondente ao type de uma mensagem enviada pelo servidor.

4. Incluir no arquivo AndroidManifest.xml. Substituir <package> pelo pacote da aplicação.


No topo do manifest:

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
<permission android:name="<package>.permission.C2D_MESSAGE"
        android:protectionLevel="signature" />
<uses-permission android:name="<package>.permission.C2D_MESSAGE" />


Aninhados dentro do <application>

<!--  Google Cloud Messaging -->
<receiver
    android:name="br.com.estudio89.push_messaging.extras.GcmBroadcastReceiver"
    android:permission="com.google.android.c2dm.permission.SEND" >

    <intent-filter>
        <action android:name="com.google.android.c2dm.intent.RECEIVE" />
        <category android:name="<package>" />
    </intent-filter>

</receiver>
<service android:name="br.com.estudio89.push_messaging.PushCentral" />

5. Dentro do arquivo de configuração, criar atributo "pushMessaging" listando os PushManagers e demais configurações:
{
	...
	"pushMessaging":{
		"pushManagers":[
			"br.com.estudio89.rhmais.push.SyncPushManager"
		],
		"gcmSenderId":"123456789012",
		"serverRegistrationUrl":"http://api.estudio89.com.br/push_messaging/register-device/"
	},
	...
}


6. Dentro da activity de login, conferir se o usuário possui Google Play Services:

	@Override
	protected void onResume() {
		// Checando se o dispositivo possui Google Play Services
		PushConfig pushConfig = PushConfig.getInstance();
		pushConfig.checkPlayServices(this);
		super.onResume();
	}

7. Dentro da classe da aplicação, no método onCreate, inicializar o plugin e registrar o device caso necessário.

	PushInjection.init(this, "config.json");
	PushConfig.getInstance().performRegistrationIfNeeded();

