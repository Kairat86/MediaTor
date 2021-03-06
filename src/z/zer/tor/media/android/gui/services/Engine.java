package z.zer.tor.media.android.gui.services;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

import z.zer.tor.media.R;
import z.zer.tor.media.android.core.ConfigurationManager;
import z.zer.tor.media.android.core.Constants;
import z.zer.tor.media.android.core.player.CoreMediaPlayer;
import z.zer.tor.media.android.gui.MainApplication;
import z.zer.tor.media.android.gui.services.EngineService.EngineServiceBinder;
import z.zer.tor.media.android.gui.util.UIUtils;
import z.zer.tor.media.util.Ref;

import static z.zer.tor.media.android.util.Asyncs.async;

public final class Engine implements IEngineService {

    private static final ExecutorService MAIN_THREAD_POOL = new EngineThreadPool();
    private static final String TAG = Engine.class.getSimpleName();

    private EngineService service;
    private ServiceConnection connection;
    private EngineBroadcastReceiver receiver;

    private FWVibrator vibrator;

    // the startServices call is a special call that can be made
    // to early (relatively speaking) during the application startup
    // the creation of the service is not (and can't be) synchronized
    // with the main activity resume.
    private boolean pendingStartServices = false;
    private boolean wasShutdown;

    private Engine() {
    }

    public boolean wasShutdown() {
        return wasShutdown;
    }

    private static class Loader {
        static final Engine INSTANCE = new Engine();
    }

    public static Engine instance() {
        return Engine.Loader.INSTANCE;
    }

    /**
     * Don't call this method directly, it's called by {@link MainApplication#onCreate()}.
     * See {@link Application#onCreate()} documentation for general restrictions on the
     * type of operations that are suitable to run here.
     *
     * @param application the application object
     */
    public void onApplicationCreate(Application application) {
        async(new EngineApplicationRefsHolder(this, application), Engine::engineServiceStarter);
    }

    @Override
    public CoreMediaPlayer getMediaPlayer() {
        return service != null ? service.getMediaPlayer() : null;
    }

    public byte getState() {
        return service != null ? service.getState() : IEngineService.STATE_INVALID;
    }

    public boolean isStarted() {
        return service != null && service.isStarted();
    }

    public boolean isStarting() {
        return service != null && service.isStarting();
    }

    public boolean isStopped() {
        return service != null && service.isStopped();
    }

    public boolean isStopping() {
        return service != null && service.isStopping();
    }

    public boolean isDisconnected() {
        return service != null && service.isDisconnected();
    }

    public void startServices(Application application) {
        Log.i(TAG, "startServices; wasShutDown=>" + wasShutdown + "; service is null=>" + (service == null));
        if (service != null || wasShutdown) {
            if (service != null) {
                service.startServices(wasShutdown);
            }
            if (wasShutdown) {
                async(new EngineApplicationRefsHolder(this, getApplication()), Engine::engineServiceStarter);
            }
            wasShutdown = false;
        } else {
            // save pending startServices call
            pendingStartServices = true;
            async(new EngineApplicationRefsHolder(this, application), Engine::engineServiceStarter);
        }
    }

    public void startServices() {
        Log.i(TAG, "startServices; wasShutDown=>" + wasShutdown + "; service is null=>" + (service == null));
        if (service != null || wasShutdown) {
            if (service != null) {
                service.startServices(wasShutdown);
            }
            if (wasShutdown) {
                async(new EngineApplicationRefsHolder(this, getApplication()), Engine::engineServiceStarter);
            }
            wasShutdown = false;
        } else {
            // save pending startServices call
            pendingStartServices = true;
        }
    }

    public void stopServices(boolean disconnected) {
        if (service != null) {
            service.stopServices(disconnected);
        }
    }

    public ExecutorService getThreadPool() {
        return MAIN_THREAD_POOL;
    }

    public void notifyDownloadFinished(String displayName, File file, String optionalInfoHash) {
        if (service != null) {
            service.notifyDownloadFinished(displayName, file, optionalInfoHash);
        }
    }

    public void notifyDownloadFinished(String displayName, File file) {
        notifyDownloadFinished(displayName, file, null);
    }

    @Override
    public void shutdown() {
        if (service != null) {
            if (connection != null) {
                try {
                    getApplication().unbindService(connection);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            if (receiver != null) {
                try {
                    getApplication().unregisterReceiver(receiver);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            service.shutdown();
            wasShutdown = true;
        }
    }

    /**
     * @param context This must be the application context, otherwise there will be a leak.
     */
    private void startEngineService(final Context context) {
        Log.i(TAG, "startEngineService");
        Intent i = new Intent(context, EngineService.class);
        try {
            context.startService(i);
            Log.i(TAG, "engine service started");
            context.bindService(i, connection = new ServiceConnection() {
                public void onServiceDisconnected(ComponentName name) {
                }

                public void onServiceConnected(ComponentName name, IBinder service) {
                    // avoids: java.lang.ClassCastException: android.os.BinderProxy cannot be cast to com.frostwire.android.gui.services.EngineService$EngineServiceBinder
                    Log.i(TAG, "on service connected");
                    if (service instanceof EngineServiceBinder) {
                        Log.i(TAG, "getting service");
                        Engine.this.service = ((EngineServiceBinder) service).getService();
                        registerStatusReceiver(context);
                        if (pendingStartServices) {
                            pendingStartServices = false;
                            Engine.this.service.startServices(getApplication());
                        }
                    }
                }
            }, 0);
        } catch (SecurityException execution) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> UIUtils.showLongMessage(context, R.string.frostwire_start_engine_service_security_exception));
            execution.printStackTrace();
        }
    }

    private void registerStatusReceiver(Context context) {
        receiver = new EngineBroadcastReceiver();

        IntentFilter fileFilter = new IntentFilter();

        fileFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        fileFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        fileFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        fileFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        fileFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        fileFilter.addDataScheme("file");

        IntentFilter connectivityFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter audioFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        IntentFilter telephonyFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);

        context.registerReceiver(receiver, fileFilter);
        context.registerReceiver(receiver, connectivityFilter);
        context.registerReceiver(receiver, audioFilter);
        context.registerReceiver(receiver, telephonyFilter);
    }

    @Override
    public Application getApplication() {
        Application r = null;
        if (service != null) {
            r = service.getApplication();
        }
        return r;
    }

    public void onHapticFeedbackPreferenceChanged() {
        if (vibrator != null) {
            vibrator.onPreferenceChanged();
        }
    }

    public void hapticFeedback() {
        if (vibrator != null) {
            vibrator.hapticFeedback();
        }
    }

    private static class FWVibrator {
        private final Vibrator vibrator;
        private boolean enabled;

        public FWVibrator(Application context) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            enabled = isActive();
        }

        public void hapticFeedback() {
            if (!enabled) return;
            try {
                vibrator.vibrate(50);
            } catch (Throwable ignored) {
            }
        }

        public void onPreferenceChanged() {
            enabled = isActive();
        }

        public boolean isActive() {
            boolean hapticFeedback = false;
            ConfigurationManager cm = ConfigurationManager.instance();
            if (cm != null) {
                hapticFeedback = cm.getBoolean(Constants.PREF_KEY_GUI_HAPTIC_FEEDBACK_ON);
            }
            return vibrator != null && hapticFeedback;
        }
    }

    private static class EngineApplicationRefsHolder {
        WeakReference<Engine> engineRef;
        WeakReference<Application> appRef;

        EngineApplicationRefsHolder(Engine engine, Application application) {
            engineRef = Ref.weak(engine);
            appRef = Ref.weak(application);
        }
    }

    private static void engineServiceStarter(EngineApplicationRefsHolder refsHolder) {
        Log.i(TAG, "engineServiceStarter");
        if (!Ref.alive(refsHolder.engineRef)) {
            Log.i(TAG, "!Ref.alive(refsHolder.engineRef)");
            return;
        }
        if (!Ref.alive(refsHolder.appRef)) {
            Log.i(TAG, "!Ref.alive(refsHolder.appRef)");
            return;
        }
        Engine engine = refsHolder.engineRef.get();
        Application application = refsHolder.appRef.get();
        if (application != null) {
            engine.vibrator = new FWVibrator(application);
            engine.startEngineService(application);
        }
    }
}
