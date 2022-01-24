package org.mate;

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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class MATEService extends Service {
    IRepresentationLayerInterface representationLayer;

    boolean allowRebind; // indicates whether onRebind should be used
    private NotificationManager notificationManager;

    private DeathCallback clientDeathCallback;

    @Override
    public void onCreate() {
        // android.os.Debug.waitForDebugger();
        // The service is being created
        Log.i("MATE_SERVICE", "onCreate");

        // fire up a notification so the system lets us start the service as foreground
        if (notificationManager == null) {
            notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        }

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

        Notification notification = builder.build();
        startForeground(1337, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MATE_SERVICE", "onStartCommand");

        // The service is starting, due to a call to startService()
        if (intent == null) {
            Log.i("MATE_SERVICE", "MATE Service starting but intent was not provided");
            return START_NOT_STICKY;
        } else if (!intent.hasExtra("packageName")) {
            Log.i("MATE_SERVICE", "MATE Service starting but package name was not provided");
            return START_NOT_STICKY;
        }

        String packageName = intent.getStringExtra("packageName");
        Log.i("MATE_SERVICE", "MATE Service starting for package name: " + packageName);

        // MATE.log_acc("Starting Random Search GA ....");
        //
        // MATE mate = new MATE(packageName, null);
        //
        // final IGeneticAlgorithm<TestCase> randomSearchGA = new GeneticAlgorithmBuilder()
        //         .withAlgorithm(Algorithm.RANDOM_SEARCH)
        //         .withChromosomeFactory(ChromosomeFactory.ANDROID_RANDOM_CHROMOSOME_FACTORY)
        //         // .withFitnessFunction(Properties.FITNESS_FUNCTION())
        //         .withFitnessFunction(FitnessFunction.LINE_COVERAGE)
        //         .withTerminationCondition(TerminationCondition.CONDITIONAL_TERMINATION)
        //         .withMaxNumEvents(Properties.MAX_NUMBER_EVENTS())
        //         .build();
        //
        // mate.testApp(randomSearchGA);

        if (representationLayer == null) {
            Log.i("MATE_SERVICE", "Unable to start MATE Service yet. Representation layer has not been registered");
            return START_NOT_STICKY;
        }

        try {
            representationLayer.getAvailableActions();
            Log.i("MATE_SERVICE", "Called getAvailableACtions on representation layer");
        } catch (RemoteException e) {
            Log.i("MATE_SERVICE", "Error getting available actions: " + e.toString());
        }

        Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                Log.i("MATE_SERVICE", "I'M WORKING!");
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(r, 1000);

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("MATE_SERVICE", "onBind");
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.i("MATE_SERVICE", "service done");
    }

    private final IMATEServiceInterface.Stub binder = new IMATEServiceInterface.Stub() {
        @Override
        public void registerRepresentationLayer(IRepresentationLayerInterface representationLayer, IBinder deathListener) throws RemoteException {
            Log.i("MATE_SERVICE", "registerRepresentationLayer called");
            MATEService.this.representationLayer = representationLayer;
            registerDeathListener(deathListener);
        }

        @Override
        public void reportAvailableActions(List<String> actions) throws RemoteException {
            Log.i("MATE_SERVICE", "reportAvailableActions called. Received actions: " + Arrays.toString(actions.toArray()));
        }
    };

    private void onClientDeath() {
        clientDeathCallback = null;
        Log.i("MATE_SERVICE", "Client just died");
    }

    private void registerDeathListener(IBinder token) {
        try {
            if (clientDeathCallback == null) {
                clientDeathCallback = new DeathCallback(token);
                //This is where the magic happens
                token.linkToDeath(clientDeathCallback, 0);
                Log.i("MATE_SERVICE", "Client death callback registered successfully");
            }
        } catch (RemoteException e) {
            Log.i("MATE_SERVICE", "An error ocurred registering death listener " + e.getMessage());
        }
    }

    private final class DeathCallback implements IBinder.DeathRecipient {
        private IBinder mBinder;

        DeathCallback(IBinder binder) {
            mBinder = binder;
        }

        @Override
        public void binderDied() {
            mBinder.unlinkToDeath(this,0);
            onClientDeath();
        }
    }
}
