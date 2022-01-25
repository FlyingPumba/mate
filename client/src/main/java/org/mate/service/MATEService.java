package org.mate.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.mate.IMATEServiceInterface;
import org.mate.IRepresentationLayerInterface;
import org.mate.MATE;
import org.mate.R;

import java.util.Arrays;
import java.util.List;

/**
 * Class in charge of firing up the MATE Client with the appropriate parameters.
 */
public class MATEService extends Service implements IBinder.DeathRecipient {

    private IRepresentationLayerInterface representationLayer;
    private IBinder representationLayerBinder;

    /**
     * Called when our service first comes to existence.
     * Here we make sure to launch a notification so we can then run as a foreground service.
     */
    @Override
    public void onCreate() {
        log("onCreate called");
        Notification notification = createNotification();
        startForeground(1337, notification);
    }

    /**
     * Called when another component requests tha the service be started.
     *
     * In our case, we will make such request using the following ADB command:
     * adb shell am start-foreground-service -n org.mate/.service.MATEService -e packageName
     * [aut-package-name] -e port [mate-server-port] -e algorithm [mate-algorithm]
     *
     * Notice that the "-e" flag indicate extra parameters that will be received in this method.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand called");

        if (intent == null) {
            log("MATE Service starting but intent was not provided");
            return START_NOT_STICKY;
        }

        if (!intent.hasExtra("packageName")) {
            log("MATE Service starting but package name was not provided");
            return START_NOT_STICKY;
        }

        if (!intent.hasExtra("algorithm")) {
            log("MATE Service starting but algorithm was not provided");
            return START_NOT_STICKY;
        }

        if (representationLayer == null) {
            log("Unable to start MATE Service yet. Representation Layer has not been registered");
            return START_NOT_STICKY;
        }

        String packageName = intent.getStringExtra("packageName");
        log(String.format("MATE Service starting for package name: %s", packageName));

        // Replace with appropriate call to MATE Client
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                log("I'M WORKING!");
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(r, 1000);

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    /**
     * Called when a client binds to this service.
     * E.g., when the Representation Layer binds to the MATE Service.
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        log("onBind called");
        return binder;
    }

    /**
     * Called when the service is destroyed.
     */
    @Override
    public void onDestroy() {
        log("onDestroy called");
    }

    /**
     * Creates MATE Service's notification.
     * @return
     */
    private Notification createNotification() {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assert(notificationManager != null);
            notificationManager.createNotificationChannelGroup(new NotificationChannelGroup("org_mate_group", "MATE Channel Group"));
            NotificationChannel notificationChannel = new NotificationChannel("org_mate_channel", "MATE Notifications Channel", NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(false);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "org_mate_channel");

        Intent notificationIntent = new Intent(this, MATE.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        builder.setContentTitle("MATE Service is running")
                .setTicker("MATE Service is running")
                .setContentText("Touch to open")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true);

        return builder.build();
    }

    public void log(String message) {
        Log.i("MATE_SERVICE", message);
    }

    /**
     * Called when the Representation Layer's process is destroyed.
     */
    @Override
    public void binderDied() {
        log("Client just died");
        this.representationLayer = null;
        this.representationLayerBinder.unlinkToDeath(this,0);

        // TODO: process this information somehow
    }

    /**
     * Save representation layer interface stub and add listener in case it dies.
     *
     * @param representationLayer
     * @param binder
     */
    public void setRepresentationLayer(IRepresentationLayerInterface representationLayer, IBinder binder) {
        this.representationLayer = representationLayer;
        this.representationLayerBinder = binder;

        try {
            binder.linkToDeath(this, 0);
            log("Client death callback registered successfully");
        } catch (RemoteException e) {
            log("An error ocurred registering death listener " + e.getMessage());
        }
    }

    private final IMATEServiceInterface.Stub binder = new IMATEServiceInterface.Stub() {
        @Override
        public void registerRepresentationLayer(IRepresentationLayerInterface representationLayer,
                                                IBinder binder) throws RemoteException {
            log("registerRepresentationLayer called");
            setRepresentationLayer(representationLayer, binder);
        }

        @Override
        public void reportAvailableActions(List<String> actions) throws RemoteException {
            log("reportAvailableActions called. Received actions: " + Arrays.toString(actions.toArray()));
        }
    };
}
