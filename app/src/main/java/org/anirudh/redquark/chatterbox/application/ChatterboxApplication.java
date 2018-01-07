package org.anirudh.redquark.chatterbox.application;

import android.app.Application;

import org.anirudh.redquark.chatterbox.receiver.ConnectivityReceiver;

/**
 * Application class respective to this app
 */

public class ChatterboxApplication extends Application {

    private static ChatterboxApplication application;

    public static ChatterboxApplication getApplication() {
        return application;
    }

    public static void setApplication(ChatterboxApplication application) {
        ChatterboxApplication.application = application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static synchronized ChatterboxApplication getInstance() {
        return application;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}
