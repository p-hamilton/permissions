package examples.baku.io.permissions;

import android.provider.ContactsContract;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionReference {
    private Integer defaultPermissions;
    private PermissionManager.OnRequestListener onRequestListener;
    private PermissionManager.OnReferralListener onReferralListener;
    private PermissionManager.OnPermissionChangeListener onPermissionChangeListener;

    private DatabaseReference mPermissionReference;

    private final Map<String, Integer> permissions = new HashMap<>();    //key is group path

    //        final Map<String, ChildEventListener> mPermissionDatabaseListeners = new HashMap<>();
    final Set<PermissionManager.OnPermissionChangeListener> permissionListeners = new HashSet<>();
    final Set<PermissionManager.OnRequestListener> requestListeners = new HashSet<>();
    final Set<PermissionManager.OnReferralListener> referralListeners = new HashSet<>();

    int currentPermissions = -1;

    public PermissionReference(DatabaseReference root, String path) {
        this.mPermissionReference = root.child(path).child(PermissionManager.KEY_PERMISSIONS);
    }

    public void setPermission(int permission) {
        mPermissionReference.setValue(permission);
    }
    public void clearPermission() {
        mPermissionReference.removeValue();
    }

    public void removeOnRequestListener(PermissionManager.OnRequestListener requestListener) {
        requestListeners.remove(requestListener);
    }

    public PermissionManager.OnRequestListener addOnRequestListener(PermissionManager.OnRequestListener requestListener) {
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

    public void removeOnPermissionChangeListener(OnPermissionChangeListener permissionChangeListener) {
        permissionListeners.remove(permissionChangeListener);
        if (permissionListeners.size() == 0) {
            mDatabaseReference.removeEventListener(mPermissionDatabaseListener);
        }
    }

    public ValueEventListener addPermissionValueEventListener(ValueEventListener listener){
        addCurrentPermissionsListener(mPermissionReference, listener);
        return listener;
    }

    public ValueEventListener addInheritedPermissionValueEventListener(ValueEventListener listener){
        addCurrentPermissionsListener(mPermissionReference, listener);
        return listener;
    }

    public OnPermissionChangeListener addOnPermissionChangeListener(OnPermissionChangeListener permissionChangeListener) {
        permissionListeners.add(permissionChangeListener);
        addCurrentPermissionsListener(mPermissionReference, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //TODO: current method will not detect if a new set of rules is added lower in the rule hierchy
                mPermissionReference = dataSnapshot.getRef();

                if (!dataSnapshot.exists()) {
                    //if no permission rules exist, set default as full permissions
                    int defaultPermissions = FLAG_READ | FLAG_WRITE;
                    mPermissionReference.child(KEY_DEFAULT).setValue(defaultPermissions);
                    permissions.put(KEY_DEFAULT, defaultPermissions);
                } else {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        permissions.put(child.getKey(), child.getValue(Integer.class));
                    }
                }
                mPermissionReference.addChildEventListener(mPermissionDatabaseListener);
                refreshPermissions();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

    private void refreshPermissions() {
        if (permissionListeners.size() > 0) {

            int highestPermissions = 0;
            if (permissions.containsKey(mId)) {
                highestPermissions |= permissions.get(mId);
            }
            //TODO:check groups
            if (permissions.containsKey(KEY_DEFAULT)) {
                highestPermissions |= permissions.get(KEY_DEFAULT);
            }
            currentPermissions = highestPermissions;
            for (OnPermissionChangeListener listener : permissionListeners) {
                listener.onPermissionChange(currentPermissions);
            }
//                }
        }
    }

    public boolean refer(PermissionReferral referral) {
        return false;
    }

    public boolean request(PermissionRequest request) {

        new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    PermissionRequest req = dataSnapshot.getValue(PermissionRequest.class);
                    if (req != null) {

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

    public void addCurrentPermissionsListener(String path, ValueEventListener listener) {
        addCurrentPermissionsListener(mDatabaseReference.child(path), listener);
    }

    //TODO: This logic should be handled on the server. Only on client for prototyping purposes
    //traverses ancestors of reference until a node with _permissions child is reached
    public void addCurrentPermissionsListener(DatabaseReference reference, final ValueEventListener listener) {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(KEY_PERMISSIONS)) {
                    listener.onDataChange(dataSnapshot.child(KEY_PERMISSIONS));
                } else {
                    DatabaseReference ref = dataSnapshot.getRef();
                    if (ref.equals(mDatabaseReference)) {  //if reference root: terminate & return non-existent snapshot at [root]/_permissions
                        listener.onDataChange(dataSnapshot.child(KEY_PERMISSIONS));
                    } else {    //query references parent
                        DatabaseReference parent = ref.getParent();
                        if (parent == null) {
                            throw new IllegalArgumentException("Traversed to DB root without encountering PermissionManager database reference");
                        }
                        addCurrentPermissionsListener(parent, listener);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onCancelled(databaseError);
            }
        });
    }