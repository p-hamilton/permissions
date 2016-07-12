package examples.baku.io.permissions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import examples.baku.io.permissions.discovery.DeviceData;
import examples.baku.io.permissions.discovery.DevicePickerActivity;
import examples.baku.io.permissions.examples.ComposeActivity;
import examples.baku.io.permissions.examples.EmailActivity;
import examples.baku.io.permissions.messenger.Messenger;
import examples.baku.io.permissions.messenger.Message;

public class PermissionService extends Service {

    private static final String TAG = PermissionService.class.getSimpleName();

    static void l(String msg) {
        Log.e(TAG, msg);
    }

    private static boolean mRunning;

    public static boolean isRunning() {
        return mRunning;
    }


    static final int FOREGROUND_NOTIFICATION_ID = -345;

    static final String KEY_BLESSINGS = PermissionManager.KEY_BLESSINGS;

    NotificationManager mNotificationManager;

    FirebaseDatabase mFirebaseDB;
    DatabaseReference mDevicesReference;
    DatabaseReference mRequestsReference;


    DatabaseReference mMessengerReference;
    Messenger mMessenger;

    DatabaseReference mPermissionsReference;
    PermissionManager mPermissionManager;
    Blessing mDeviceBlessing;

    DatabaseReference mLocalDeviceReference;

    private String mDeviceId;

    private IBinder mBinder = new PermissionServiceBinder();


    private String tempTarget;
    private Map<String, DeviceData> mDiscovered = new HashMap<>();
    private Map<String, Integer> mDiscoveredNotifications = new HashMap<>();

    private HashSet<DiscoveryListener> mDiscoveryListener = new HashSet<>();

    public interface DiscoveryListener {
        void onChange(Map<String, DeviceData> devices);

        void onDisassociate(String deviceId);
    }

    public class PermissionServiceBinder extends Binder {
        public PermissionService getInstance() {
            return PermissionService.this;
        }
    }

    public PermissionService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        l("Creating Permission service");

        mDeviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);


        mFirebaseDB = FirebaseDatabase.getInstance();
        mDevicesReference = mFirebaseDB.getReference("_devices");
        mRequestsReference = mFirebaseDB.getReference("requests");

        mPermissionsReference = mFirebaseDB.getReference("permissions");
        mPermissionManager = new PermissionManager(mFirebaseDB.getReference(), mDeviceId);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initForegroundNotification();

        registerDevice();
        initDeviceBlessing();
        initMessenger();
        initDiscovery();

        mRunning = true;
    }

    public Messenger getMessenger() {
        return mMessenger;
    }

    public String getFocus() {
        return tempTarget;
    }

    static final int FOCUS_NOTIFICATION = 1243254;

    public void setFocus(String dId) {
        tempTarget = dId;
        if (!mDiscovered.containsKey(dId)) return;

        DeviceData device = mDiscovered.get(dId);
        String title = device.getName();
        String subtitle = device.getId();   //default
        if (device.getStatus() != null && device.getStatus().containsKey("description")) {
            subtitle = device.getStatus().get("description");
        }
        int icon = R.drawable.ic_phone_android_black_24dp;

        Intent dismissIntent = new Intent(this, PermissionService.class);
        dismissIntent.putExtra("type", "dismiss");
        dismissIntent.putExtra("deviceId", dId);
        PendingIntent dismissPending = PendingIntent.getService(this, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setSmallIcon(icon)
                .setVibrate(new long[]{100})
                .setPriority(Notification.PRIORITY_MAX)
                .setDeleteIntent(dismissPending)
                .build();
        mNotificationManager.notify(FOCUS_NOTIFICATION, notification);
    }


    public PermissionManager getPermissionManager() {
        return mPermissionManager;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public Map<String, DeviceData> getDiscovered() {
        return mDiscovered;
    }

    public FirebaseDatabase getFirebaseDB() {
        return mFirebaseDB;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PermissionServiceBinder();
    }

    void initForegroundNotification() {

        Intent contentIntent = new Intent(this, PermissionService.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent discoverIntent = new Intent(getApplicationContext(), PermissionService.class);
        discoverIntent.putExtra("type", "discover");
        PendingIntent discoverPendingIntent = PendingIntent.getService(this, 1, discoverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(getApplicationContext(), PermissionService.class);
        closeIntent.putExtra("type", "close");
        PendingIntent closePendingIntent = PendingIntent.getService(this, 2, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_vpn_key_black_24dp)
                .setContentTitle("Permission service running")
                .addAction(new Notification.Action.Builder(R.drawable.ic_zoom_in_black_24dp, "Discover", discoverPendingIntent).build())
                .addAction(new Notification.Action.Builder(R.drawable.ic_close_black_24dp, "Stop", closePendingIntent).build())
                .build();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    void refreshForegroundNotification(Notification notification) {
        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
    }


    public void initDeviceBlessing() {

        mPermissionManager.addPermissionEventListener("documents/" + mDeviceId, new PermissionManager.OnPermissionChangeListener() {
            @Override
            public void onPermissionChange(int current) {
                l("jajaja " + current);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mPermissionManager.addPermissionEventListener("documents", new PermissionManager.OnPermissionChangeListener() {
            @Override
            public void onPermissionChange(int current) {
                l("lalala " + current);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mPermissionManager.addPermissionEventListener("documents/" + mDeviceId +"/snake", new PermissionManager.OnPermissionChangeListener() {
            @Override
            public void onPermissionChange(int current) {
                l("sasasa " + current);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final DatabaseReference deviceBlessingRef = mFirebaseDB.getReference(KEY_BLESSINGS).child(mDeviceId);
        deviceBlessingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mDeviceBlessing = new Blessing(dataSnapshot);
                } else {
                    mDeviceBlessing = new Blessing(mDeviceId, null, deviceBlessingRef);
                }

                //give device access its own document directory
                mDeviceBlessing.setPermissions("documents/" + mDeviceId, PermissionManager.FLAG_WRITE | PermissionManager.FLAG_READ);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Blessing getDeviceBlessing() {
        return mDeviceBlessing;
    }

    public void initMessenger() {
        mMessengerReference = mFirebaseDB.getReference("messages");
        mMessenger = new Messenger(mDeviceId, mMessengerReference);

        mMessenger.on("start", new Messenger.Listener() {
            @Override
            public void call(String args, Messenger.Ack callback) {
                Intent emailIntent = new Intent(PermissionService.this, EmailActivity.class);
                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(emailIntent);
            }
        });

        mMessenger.on("disassociate", new Messenger.Listener() {
            @Override
            public void call(String args, Messenger.Ack callback) {
//                if(request.getSource() != null){
//                    String dId = request.getSource();
//                    for (Iterator<DiscoveryListener> iterator = mDiscoveryListener.iterator(); iterator.hasNext(); ) {
//                        DiscoveryListener listener  = iterator.next();
//                        listener.onDisassociate(dId);
//                    }
//                }
            }
        });

        mMessenger.on("cast", new Messenger.Listener() {
            @Override
            public void call(String args, Messenger.Ack callback) {
                if(args != null){
                    try {
                        JSONObject jsonArgs = new JSONObject(args);
                        if(jsonArgs.has("activity")){
                            if(ComposeActivity.class.getSimpleName().equals(jsonArgs.getString("activity"))){
                                String path = jsonArgs.getString(ComposeActivity.EXTRA_MESSAGE_PATH);
                                Intent emailIntent = new Intent(PermissionService.this, ComposeActivity.class);
                                emailIntent.putExtra(ComposeActivity.EXTRA_MESSAGE_PATH, path);
                                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(emailIntent);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mMessenger.on("request", new Messenger.Listener() {
            @Override
            public void call(String args, Messenger.Ack callback) {

                try {
                    JSONObject arguments = new JSONObject(args);
                    String dId = arguments.getString("deviceId");
                    String messageId = arguments.getString("messageId");
                    String description = arguments.getString("description");
                    if (!mDiscovered.containsKey(dId)) return;

                    DeviceData device = mDiscovered.get(dId);
                    String title = device.getName();
                    String subtitle = description;   //default
                    if (device.getStatus() != null && device.getStatus().containsKey("description")) {
                        subtitle = device.getStatus().get("description");
                    }

                    int icon = R.drawable.ic_phone_android_black_24dp;

                    Intent reviewIntent = new Intent(PermissionService.this, ComposeActivity.class);
                    reviewIntent.putExtra("review", "");
                    reviewIntent.putExtra(ComposeActivity.EXTRA_MESSAGE_ID, messageId);

                    Intent dismissIntent = new Intent(PermissionService.this, PermissionService.class);
                    dismissIntent.putExtra("type", "dismiss");
                    dismissIntent.putExtra("deviceId", dId);
                    PendingIntent dismissPending = PendingIntent.getService(PermissionService.this, 320, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);


                    PendingIntent pi = PendingIntent.getActivity(PermissionService.this, 5, reviewIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                    Notification notification = new Notification.Builder(PermissionService.this)
                            .setContentTitle(title)
                            .setContentText(subtitle)
                            .setSmallIcon(icon)
                            .setVibrate(new long[]{100})
                            .setPriority(Notification.PRIORITY_MAX)
                            .setContentIntent(pi)
                            .setDeleteIntent(dismissPending)
                            .addAction(new Notification.Action.Builder(R.drawable.ic_check_black_24dp, "Accept", pi).build())
                            .addAction(new Notification.Action.Builder(R.drawable.ic_close_black_24dp, "Reject", pi).build())
                            .build();
                    mNotificationManager.notify(/*FOCUS_NOTIFICATION*/help++, notification);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


    }

    int help = 123;

//    public void sendRequest(Message request){
//        if(request == null) return;//throw new IllegalArgumentException("null request");
//        if(request.getTarget() == null){
//            if(tempTarget == null){
//                new RuntimeException("Invalid request").printStackTrace();
//            }
//            request.setTarget(tempTarget);
//        }
//        request.setSource(mDeviceId);
//        mRequestsRef.child(request.getId()).setValue(request);
//    }

    int notificationIndex = 1111;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("type")) {
            String type = intent.getStringExtra("type");
            l("start command " + type);
            if ("sendRequest".equals(type)) {
                if (intent.hasExtra("request")) {
                    Message request = intent.getParcelableExtra("request");
//                    sendRequest(request);
                }

            } else if ("discover".equals(type)) {
                if (mDiscovered != null) {

                    Intent discoveryIntent = new Intent(this, DevicePickerActivity.class);
                    discoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(discoveryIntent);
                }
            } else if ("dismiss".equals(type)) {
                Message request = new Message("disassociate");
                request.setTarget(tempTarget);
//                sendRequest(request);
                setFocus(null);


            } else if ("close".equals(type)) {
                stopSelf();
            } else if ("focus".equals(type)) {
                if (intent.hasExtra("deviceId")) {
                    String dId = intent.getStringExtra("deviceId");
                    l("targetting " + dId);
                    if (mDiscovered.containsKey(dId)) {
                        tempTarget = dId;
                        DeviceData target = mDiscovered.get(dId);

                        String title = "Targetting device: " + target.getName();

                        Intent contentIntent = new Intent(this, EmailActivity.class);

                        Intent discoverIntent = new Intent(this, PermissionService.class);
                        discoverIntent.putExtra("type", "discover");
                        PendingIntent discoverPendingIntent = PendingIntent.getService(this, 0, discoverIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                        Intent castIntent = new Intent(this, PermissionService.class);
                        castIntent.putExtra("type", "sendRequest");
                        castIntent.putExtra("request", new Message("start"));
                        PendingIntent castPendingIntent = PendingIntent.getService(this, 0, castIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                        Notification notification = new Notification.Builder(this)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setVibrate(new long[]{100})
                                .setContentIntent(PendingIntent.getActivity(this, 0, contentIntent, 0))
                                .setSmallIcon(R.drawable.ic_vpn_key_black_24dp)
                                .setContentTitle(title)
                                .addAction(new Notification.Action.Builder(R.drawable.ic_cast_black_24dp, "Cast", castPendingIntent).build())
                                .addAction(new Notification.Action.Builder(R.drawable.ic_zoom_in_black_24dp, "Discover", discoverPendingIntent).build())
                                .build();

                        refreshForegroundNotification(notification);

                    }
                    for (Iterator<Integer> iterator = mDiscoveredNotifications.values().iterator(); iterator.hasNext(); ) {
                        int notId = iterator.next();
                        mNotificationManager.cancel(notId);
                    }
                    mDiscoveredNotifications.clear();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    void registerDevice() {

        mLocalDeviceReference = mDevicesReference.child(mDeviceId);
        mLocalDeviceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    resetLocalDevice();
                } else {
                    try {
                        mLocalDevice = dataSnapshot.getValue(DeviceData.class);

                    } catch (DatabaseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private DeviceData mLocalDevice;

    void resetLocalDevice() {
        final String deviceName = android.os.Build.MODEL;
        mLocalDevice = new DeviceData(mDeviceId, deviceName);
        mLocalDevice.setActive(true);
        mLocalDeviceReference.setValue(mLocalDevice);
    }

    void initDiscovery() {
        mDevicesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                updateDevice(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updateDevice(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addDiscoveryListener(DiscoveryListener listener) {
        mDiscoveryListener.add(listener);
    }

    private void updateDevice(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {
            String key = dataSnapshot.getKey();
            if (!mDeviceId.equals(key)) {
                try {
                    DeviceData device = dataSnapshot.getValue(DeviceData.class);
                    if (device != null) {
                        mDiscovered.put(key, device);
                        for (Iterator<DiscoveryListener> iterator = mDiscoveryListener.iterator(); iterator.hasNext(); ) {
                            DiscoveryListener listener = iterator.next();
                            listener.onChange(mDiscovered);
                        }
                    }
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void start(Context context) {
        context.startService(new Intent(context, PermissionService.class));
    }

    //convenience class for when context implements ServiceConnection
    //throws cast exception
    public static void bind(Context context) {
        ServiceConnection connection = (ServiceConnection) context;
        bind(context, connection);
    }

    public static void bind(Context context, ServiceConnection connection) {
        context.bindService(new Intent(context, PermissionService.class), connection, BIND_AUTO_CREATE);
    }
}
