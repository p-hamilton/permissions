package examples.baku.io.permissions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import examples.baku.io.permissions.email.ComposeActivity;
import examples.baku.io.permissions.email.EmailActivity;

public class PermissionService extends Service {

    private static final String TAG = PermissionService.class.getSimpleName();
    static void l(String msg){
        Log.e(TAG, msg);
    }

    private static boolean mRunning;

    public static boolean isRunning() {
        return mRunning;
    }


    static final int FOREGROUND_NOTIFICATION_ID = -345;

    NotificationManager mNotificationManager;

    FirebaseDatabase mFirebaseDB;
    DatabaseReference mDevicesReference;
    DatabaseReference mRequestsReference;

    DatabaseReference mLocalDeviceReference;

    private String mDeviceId;

    private IBinder mBinder = new PermissionServiceBinder();


    private String tempTarget;
    private Map<String, DeviceData> mDiscovered = new HashMap<>();
    private Map<String, Integer> mDiscoveredNotifications = new HashMap<>();

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

        mDevicesReference = mFirebaseDB.getReference("devices");
        mRequestsReference = mFirebaseDB.getReference("requests");


        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        initForegroundNotification();

        registerDevice();
        initDiscovery();

        mRunning = true;
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

    void initForegroundNotification(){

        Intent contentIntent = new Intent(this, PermissionService.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent discoverIntent = new Intent(this, PermissionService.class);
        discoverIntent.putExtra("type", "discover");

        PendingIntent discoverPendingIntent = PendingIntent.getService(this, 0, discoverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_vpn_key_black_24dp)
                .setContentTitle("Permission service running")
                .addAction(new Notification.Action.Builder(R.drawable.ic_zoom_in_black_24dp, "Discover", discoverPendingIntent).build())
                .build();
        startForeground(FOREGROUND_NOTIFICATION_ID, notification);
    }

    void refreshForegroundNotification(Notification notification){
        mNotificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification);
    }


    public void sendRequest(PermissionRequest request){
        if(request == null) return;//throw new IllegalArgumentException("null request");
        request.setTarget(tempTarget);
        mRequestsReference.child(request.getId()).setValue(request);
    }

    int notificationIndex = 1111;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.hasExtra("type")){
            String type = intent.getStringExtra("type");
            l("start command "+type);
            if("sendRequest".equals(type)){
                if(intent.hasExtra("request")){
                    PermissionRequest request = intent.getParcelableExtra("request");
                    sendRequest(request);
                }

            }else if("discover".equals(type)){
                if(mDiscovered != null){
                    int i =0;
                    int max = 5;
                    for (Iterator<DeviceData> iterator = mDiscovered.values().iterator(); iterator.hasNext(); ) {
                        DeviceData device  =  iterator.next();
                        String dId = device.getId();

                        if(mDeviceId.equals(dId)) continue;

                        String title = device.getName();
                        if(title == null) title = "Unknown device";
                        Intent contentIntent = new Intent(this, PermissionService.class);
                        contentIntent.putExtra("type", "focus");
                        contentIntent.putExtra("deviceId", dId);
                        PendingIntent contentPendingIntent = PendingIntent.getService(this, 0, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        Notification notification = new Notification.Builder(this)
                                .setContentTitle(title)
                                .setContentIntent(contentPendingIntent)
                                .setSmallIcon(R.drawable.ic_phone_android_black_24dp)
                                .build();
                        mDiscoveredNotifications.put(dId, notificationIndex);
                        mNotificationManager.notify(notificationIndex++, notification);

                        if(++i > max) break;
                    }
                }

            }else if("focus".equals(type)){
                if(intent.hasExtra("deviceId")){
                    String dId = intent.getStringExtra("deviceId");
                    l("targetting " + dId);
                    if(mDiscovered.containsKey(dId)){
                        tempTarget = dId;
                        DeviceData target = mDiscovered.get(dId);

                        String title = "Targetting device: " + target.getName();

                        Intent contentIntent = new Intent(this, EmailActivity.class);

                        Intent discoverIntent = new Intent(this, PermissionService.class);
                        discoverIntent.putExtra("type", "discover");
                        PendingIntent discoverPendingIntent = PendingIntent.getService(this, 0, discoverIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                        Intent castIntent = new Intent(this, PermissionService.class);
                        castIntent.putExtra("type", "sendRequest");
                        castIntent.putExtra("request", new PermissionRequest("start"));
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
                    for(Iterator<Integer> iterator = mDiscoveredNotifications.values().iterator(); iterator.hasNext();){
                        int notId = iterator.next();
                        mNotificationManager.cancel(notId);
                    }
                    mDiscoveredNotifications.clear();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    void registerDevice(){

        mLocalDeviceReference = mDevicesReference.child(mDeviceId);
        mLocalDeviceReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    resetLocalDevice();
                }
                else{
                    try{
                        mLocalDevice = dataSnapshot.getValue(DeviceData.class);

                    }catch(DatabaseException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //listen for all requests directly targetted at this device
        mRequestsReference.equalTo(mDeviceId).addChildEventListener(requestListener);
    }

    private DeviceData mLocalDevice;
    void resetLocalDevice(){
        final String deviceName = android.os.Build.MODEL;
        mLocalDevice = new DeviceData(mDeviceId, deviceName);
        mLocalDevice.setActive(true);
        mLocalDeviceReference.setValue(mLocalDevice);
    }

    void initDiscovery(){
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

    private void updateDevice(DataSnapshot dataSnapshot){
        if(dataSnapshot.exists()){
            String key = dataSnapshot.getKey();
            if(!mDeviceId.equals(key)){
                try {
                    DeviceData device = dataSnapshot.getValue(DeviceData.class);
                    if (device != null) {
                        mDiscovered.put(key, device);
                    }
                }catch(DatabaseException e){
                    e.printStackTrace();
                }
            }
        }


    }

    ChildEventListener requestListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if(!dataSnapshot.exists()) return;

            try{
                PermissionRequest request = dataSnapshot.getValue(PermissionRequest.class);
                if(request == null) return;
                l("request: "  + request.getType());
                if("launch".equals(request.getType())){
//                    Intent flutterIntent = getPackageManager().getLaunchIntentForPackage(FLUTTER_PACKAGE);
//                    flutterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(flutterIntent);

                }else if("start".equals(request.getType())){
                    Intent emailIntent = new Intent(PermissionService.this, EmailActivity.class);
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(emailIntent);
//                    Intent flutterIntent = getPackageManager().getLaunchIntentForPackage(FLUTTER_PACKAGE);
//                    flutterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(flutterIntent);
//                    mMessenger.send(FLUTTER_PACKAGE, "navigate", request.getArguments().get("uuid"));

                }else if("cast".equals(request.getType())){
                    Intent emailIntent = new Intent(PermissionService.this, ComposeActivity.class);
                    if(request.getArguments().containsKey("messageId")){
                        emailIntent.putExtra(ComposeActivity.EXTRA_MESSAGE_ID, request.getArguments().get("messageId"));
                    }
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(emailIntent);
//                    Intent flutterIntent = getPackageManager().getLaunchIntentForPackage(FLUTTER_PACKAGE);
//                    flutterIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(flutterIntent);
//                    PleaseNotification.updateRequest(PleaseService.this, "Request: send message");

                }else if("request".equals(request.getType())){
                        PendingIntent pi = PendingIntent.getActivity(PermissionService.this ,0, new Intent(PermissionService.this, ComposeActivity.class),0);
                        int icon = R.drawable.ic_phone_android_black_24dp; // context.getApplicationContext().getApplicationInfo().icon;
                        Notification notification = new Notification.Builder(PermissionService.this)
                                .setContentTitle("Review email request")
                                .setSmallIcon(icon)
                                .addAction(new Notification.Action.Builder (R.drawable.ic_check_black_24dp, "Accept", pi).build())
                                .addAction(new Notification.Action.Builder (R.drawable.ic_close_black_24dp, "Reject", pi).build())
                                .build();
                        mNotificationManager.notify(notificationIndex++, notification);
                }
            }catch(DatabaseException e){
                e.printStackTrace();
            } finally {
                dataSnapshot.getRef().removeValue();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
    };

    static int x =11;
    public static void update(Context context, String title){
        int icon = R.drawable.ic_phone_android_black_24dp;
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setSmallIcon(icon)
                .build();
        notificationManager.notify(++x, notification);
    }

    public static void start(Context context){
        context.startService(new Intent(context, PermissionService.class));
    }
}
