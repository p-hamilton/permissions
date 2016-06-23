package examples.baku.io.permissions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PermissionService extends Service {

    private static final String TAG = PermissionService.class.getSimpleName();
    static void l(String msg){
        Log.e(TAG, msg);
    }

    private static boolean mRunning;

    public static boolean isRunning() {
        return mRunning;
    }


    static final int FOREGROUND_NOTIFICATION_ID = 345;

    NotificationManager mNotificationManager;

    FirebaseDatabase mFirebaseDB;
    DatabaseReference mDevicesReference;
    DatabaseReference mRequestsReference;

    DatabaseReference mLocalDeviceReference;

    private String mDeviceId;

    private IBinder mBinder = new PermissionServiceBinder();

    class PermissionServiceBinder extends Binder {
        public PermissionService getInstance() {
            return PermissionService.this;
        }
    }

    public PermissionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);


        mFirebaseDB = FirebaseDatabase.getInstance();

        mDevicesReference = mFirebaseDB.getReference("devices");
        mRequestsReference = mFirebaseDB.getReference("requests");

        initForegroundNotification();

        mRunning = true;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public FirebaseDatabase getFirebaseDB() {
        return mFirebaseDB;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PermissionServiceBinder();
    }

    void initForegroundNotification(){

        Intent contentIntent = new Intent(this, EmailActivity.class);

        Notification notification = new Notification.Builder(this)
                .setContentIntent(PendingIntent.getActivity(this, 0, contentIntent, 0))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Please service running")
                .build();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    void refreshForegroundNotification(Notification notification){
        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
    }

}
