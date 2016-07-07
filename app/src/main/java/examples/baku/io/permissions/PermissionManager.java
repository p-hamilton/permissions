package examples.baku.io.permissions;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by phamilton on 6/28/16.
 *
 *
 */
public class PermissionManager {

    DatabaseReference mDatabaseReference;

    public static final int FLAG_WRITE = 1 << 0;
    public static final int FLAG_READ = 1 << 1;
    public static final int FLAG_REQUEST = 1 << 2;     //2-way
    public static final int FLAG_REFER = 1 << 3;       //1-way

    static final String KEY_RULES = "_rules";
    static final String KEY_PERMISSIONS = "_permissions";
    static final String KEY_DEFAULT = "_default";
    static final String KEY_REQUESTS = "_requests";

    private String mId;
    final Map<String, PermissionReference> resources = new HashMap<>();

    final Map<String, PermissionRequest> mRequests = new HashMap<>();
//    final Map<String, ChildEventListener> mPermissionDatabaseListeners = new HashMap<>();
//    final Map<String, Set<OnPermissionChangeListener>> permissionListeners = new HashMap<>();
//    final Map<String, Set<OnRequestListener>> requestListeners = new HashMap<>();
//    final Map<String, Set<OnReferralListener>> referralListeners = new HashMap<>();


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




    PermissionReference getResource(String path){
        return resources.get(path);
    }


    public class PermissionReference {
        private String path;
        private Integer defaultPermissions;
        private OnRequestListener onRequestListener;
        private OnReferralListener onReferralListener;
        private OnPermissionChangeListener onPermissionChangeListener;

        private DatabaseReference mValueReference;
        private DatabaseReference mPermissionReference;

        private final Map<String, Integer> permissions = new HashMap<>();    //key is group path

//        final Map<String, ChildEventListener> mPermissionDatabaseListeners = new HashMap<>();
        final Set<OnPermissionChangeListener> permissionListeners = new HashSet<>();
        final Set<OnRequestListener> requestListeners = new HashSet<>();
        final Set<OnReferralListener> referralListeners = new HashSet<>();

        int currentPermissions = 0;

        public PermissionReference(String path) {
            this.path = path;
            this.permissions.put(null, defaultPermissions);

            mValueReference = mDatabaseReference.child(path);
            mPermissionReference = mDatabaseReference.child(KEY_RULES).child(path).child(KEY_PERMISSIONS);

        }

        public void setPermission(String group, int permission){
            mPermissionReference.child(group).setValue(defaultPermissions);
        }

        public void removeOnRequestListener(OnRequestListener requestListener) {
                requestListeners.remove(requestListener);
        }

        public OnRequestListener addOnRequestListener(OnRequestListener requestListener) {
            requestListeners.add(requestListener);
            return requestListener;
        }

        public void removeOnReferralListener(OnReferralListener referralListener) {
            referralListeners.remove(referralListener);
        }

        public OnReferralListener addOnReferralListener(String path, OnReferralListener referralListener) {
            referralListeners.add(referralListener);
            return referralListener;
        }

        public void removeOnPermissionChangeListener( OnPermissionChangeListener permissionChangeListener) {
            permissionListeners.remove(permissionChangeListener);
            if(permissionListeners.size() == 0){
                mDatabaseReference.removeEventListener(mPermissionDatabaseListener);
            }
        }

        public OnPermissionChangeListener addOnPermissionChangeListener( OnPermissionChangeListener permissionChangeListener) {
            permissionListeners.add(permissionChangeListener);
            mDatabaseReference.addChildEventListener(mPermissionDatabaseListener);

            return permissionChangeListener;
        }

        private ChildEventListener mPermissionDatabaseListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                permissions.put(s, dataSnapshot.getValue(Integer.class));
                refreshPermissions();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                permissions.put(s, dataSnapshot.getValue(Integer.class));
                refreshPermissions();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                permissions.remove(dataSnapshot.getKey());
                refreshPermissions();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        private void refreshPermissions(){
            if(permissionListeners.size() > 0){
                int highestPermissions = 0;
                for (Iterator<Integer> iterator = permissions.values().iterator(); iterator.hasNext(); ) {
                    int perms = iterator.next();
                    highestPermissions |= perms;
                }
                if(currentPermissions != highestPermissions){
                    currentPermissions = highestPermissions;
                    for (OnPermissionChangeListener listener : permissionListeners) {
                        listener.onPermissionChange(highestPermissions);
                    }
                }
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
    }


    public interface OnRequestListener{
        void onRequest(PermissionRequest request);
    }

    public interface OnReferralListener {
        void onReferral();
    }

    public interface OnPermissionChangeListener {
        void onPermissionChange(int current);
        void onCancelled(DatabaseError databaseError);
    }
}
