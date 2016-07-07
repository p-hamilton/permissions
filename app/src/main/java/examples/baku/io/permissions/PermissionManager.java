package examples.baku.io.permissions;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by phamilton on 6/28/16.
 *
 *
 */
public class PermissionManager {

    DatabaseReference mDatabaseReference;

    static final int FLAG_WRITE = 1 << 0;
    static final int FLAG_READ = 1 << 1;
    static final int FLAG_REQUEST = 1 << 2;     //2-way
    static final int FLAG_REFER = 1 << 3;       //1-way

    static final String KEY_RULES = "_rules";
    static final String KEY_PERMISSIONS = "_permissions";
    static final String KEY_DEFAULT = "_default";
    static final String KEY_REQUESTS = "_requests";

    private String mId;
//    final Map<String, PermissionReference> resources = new HashMap<>();

    final Map<String, PermissionRequest> mRequests = new HashMap<>();
    final Map<String, ChildEventListener> mPermissionDatabaseListeners = new HashMap<>();
    final Map<String, Set<OnPermissionChangeListener>> permissionListeners = new HashMap<>();
    final Map<String, Set<OnRequestListener>> requestListeners = new HashMap<>();
    final Map<String, Set<OnReferralListener>> referralListeners = new HashMap<>();


    //TODO: replace string ownerId with Auth
    public PermissionManager(DatabaseReference databaseReference, String owner){
        this.mDatabaseReference = databaseReference;
        this.mId = owner;
    }

    public void setPermission(String path, String group, int permission){
        if(group == null){
            group = KEY_DEFAULT;
        }
        mDatabaseReference.child(KEY_RULES).child(path).child(group).setValue(permission);
    }

    public void removeOnRequestListener(String path, OnRequestListener requestListener) {
        this.requestListeners.get(path);
        Set<OnRequestListener> listeners = this.requestListeners.get(path);
        if(listeners != null) {
            listeners.remove(requestListener);
        }
    }

    public OnRequestListener addOnRequestListener(String path, OnRequestListener requestListener) {
        Set<OnRequestListener> listeners = this.requestListeners.get(path);
        if(listeners == null){
            listeners = new HashSet<>();
            listeners.add(requestListener);
            this.requestListeners.put(path, listeners);
        }else{
            listeners.add(requestListener);
        }
        return requestListener;
    }

    public void removeOnReferralListener(String path, OnReferralListener referralListener) {
        this.referralListeners.get(path);
        Set<OnReferralListener> listeners = this.referralListeners.get(path);
        if(listeners != null) {
            listeners.remove(referralListener);
        }
    }

    public OnReferralListener addOnReferralListener(String path, OnReferralListener referralListener) {
        Set<OnReferralListener> listeners = this.referralListeners.get(path);
        if(listeners == null){
            listeners = new HashSet<>();
            listeners.add(referralListener);
            this.referralListeners.put(path, listeners);
        }else{
            listeners.add(referralListener);
        }
        return referralListener;
    }

    public void removeOnPermissionChangeListener(String path, OnPermissionChangeListener permissionChangeListener) {
        Set<OnPermissionChangeListener> listeners = this.permissionListeners.get(path);
        if(listeners != null) {
            listeners.remove(permissionChangeListener);
            if(listeners.size() == 0){
                mPermissionDatabaseListeners.remove(path);
            }
        }
    }

    public OnPermissionChangeListener addOnPermissionChangeListener(String path, OnPermissionChangeListener permissionChangeListener) {
        Set<OnPermissionChangeListener> listeners = this.permissionListeners.get(path);
        if(listeners == null){
            listeners = new HashSet<>();
            listeners.add(permissionChangeListener);
            this.permissionListeners.put(path, listeners);
        }else{
            listeners.add(permissionChangeListener);
        }

        mPermissionDatabaseListeners.put(path, mDatabaseReference.child(KEY_RULES).child(path).child(KEY_PERMISSIONS).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
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
        }));
        return permissionChangeListener;
    }

    public void what(String path, DataSnapshot snapshot){
        String key = snapshot.getKey();
        if(key.equals(mId)){

        }else(key.equals(KEY_DEFAULT)){

        }
    }

    public boolean refer(PermissionReferral referral){
        return false;
    }

    public boolean request(PermissionRequest request){

        new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    PermissionRequest req = dataSnapshot.getValue(PermissionRequest.class);
                    if(req != null){

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        return false;
    }


//    public PermissionReference getResource(String path){
//        return null;
//    }
//
//    public PermissionReference defineResourceRules(String path){
//        return new PermissionReference(path);
//    }
//
//    public class PermissionReference {
//        private String path;
//        private Integer defaultPermissions;
//        private OnRequestListener onRequestListener;
//        private OnReferralListener onReferralListener;
//        private OnPermissionChangeListener onPermissionChangeListener;
//
//        private DatabaseReference mValueReference;
//        private DatabaseReference mPermissionReference;
//
//        private final Map<String, Integer> permissions = new HashMap<>();    //key is group path
//
//        public PermissionReference(String path) {
//            this.path = path;
//            this.permissions.put(null, defaultPermissions);
//
//            mValueReference = mDatabaseReference.child(path);
//            mPermissionReference = mDatabaseReference.child("_permissions/"+path);
//            mPermissionReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if(dataSnapshot.hasChild(KEY_DEFAULT)){
//                        defaultPermissions = dataSnapshot.getValue(Integer.class);
//                    }else{
//                        setDefaultPermissions(FLAG_READ | FLAG_WRITE);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
//
//        private void refreshPermissions(){
//            int highestPermissions = 0;
//            for (Iterator<Integer> iterator = permissions.values().iterator(); iterator.hasNext(); ) {
//                int perms = iterator.next();
//                highestPermissions |= perms;
//            }
//
//        }
//
//
//
//        public void setPermission(String group, int permission){
//            mPermissionReference.child(group).setValue(defaultPermissions);
//        }
//
//        public void setDefaultPermissions(int defaultPermissions) {
//            this.defaultPermissions = defaultPermissions;
//            setPermission(KEY_DEFAULT, defaultPermissions);
//        }
//
//        public Integer getDefaultPermissions() {
//            return defaultPermissions;
//        }
//
//        public boolean refer(PermissionReferral referral){
//            return false;
//        }
//
//        public boolean request(PermissionRequest request){
//
//            new ValueEventListener(){
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if(dataSnapshot.exists()){
//                        PermissionRequest req = dataSnapshot.getValue(PermissionRequest.class);
//                        if(req != null){
//
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            };
//            return false;
//        }
//    }
//

    public interface OnRequestListener{
        void onRequest(PermissionRequest request);
    }

    public interface OnReferralListener {
        void onReferral();
    }

    public interface OnPermissionChangeListener {
        void onPermissionChange(Integer current);
        void onCancelled(DatabaseError databaseError);
    }
}
