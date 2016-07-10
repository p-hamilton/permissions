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
 */
public class PermissionManager {

    DatabaseReference mDatabaseReference;
    DatabaseReference mRulesReference;
    DatabaseReference mRequestsReference;

    public static final int FLAG_WRITE = 1 << 0;
    public static final int FLAG_READ = 1 << 1;
    public static final int FLAG_REQUEST = 1 << 2;     //2-way
    public static final int FLAG_REFER = 1 << 3;       //1-way

    static final String KEY_RULES = "_rules";
    static final String KEY_PERMISSIONS = "_permissions";
    static final String KEY_DEFAULT = "_default";
    static final String KEY_REQUESTS = "_requests";

    private String mId;
    final Map<String, PermissionReference> mResources = new HashMap<>();

    final Map<String, PermissionRequest> mRequests = new HashMap<>();
//    final Map<String, ChildEventListener> mPermissionDatabaseListeners = new HashMap<>();
//    final Map<String, Set<OnPermissionChangeListener>> permissionListeners = new HashMap<>();
//    final Map<String, Set<OnRequestListener>> requestListeners = new HashMap<>();
//    final Map<String, Set<OnReferralListener>> referralListeners = new HashMap<>();

    final Map<String, Blessing> mBlessings = new HashMap<>();
    final Set<String> mReceivedBlessings = new HashSet<>();
    final Set<String> mGrantedBlessings = new HashSet<>();

    //TODO: replace string ownerId with Auth
    public PermissionManager(DatabaseReference databaseReference, String owner) {
        this.mDatabaseReference = databaseReference;
        this.mRulesReference = databaseReference.child(KEY_RULES);
        this.mRequestsReference = databaseReference.child(KEY_REQUESTS);
        this.mId = owner;
    }

    public void setPermission(String path, String group, int permission) {
        if (group == null) {
            group = KEY_DEFAULT;
        }
        mDatabaseReference.child(path).child(group).setValue(permission);
    }


    PermissionReference getResource(String path) {
        if (!mResources.containsKey(path)) {
            mResources.put(path, new PermissionReference(path));
        }
        return mResources.get(path);
    }




    public interface OnRequestListener {
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
