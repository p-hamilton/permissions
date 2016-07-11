package examples.baku.io.permissions;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
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
    Query mReceivedRef;
    final Set<String> mReceivedBlessings = new HashSet<>();

    final Map<String, ValueEventListener> mPermissionValueEventListeners = new HashMap<>();


    //<targetId, blessingId>
    //TODO: allow for multiple granted blessings per target
    final Map<String,String> mGrantedBlessings = new HashMap<>();

    //TODO: replace string ownerId with Auth
    public PermissionManager(final DatabaseReference databaseReference, String owner) {
        this.mDatabaseReference = databaseReference;
        this.mRulesReference = databaseReference.child(KEY_RULES);
        this.mRequestsReference = databaseReference.child(KEY_REQUESTS);
        this.mId = owner;

        mReceivedRef = mDatabaseReference.child("_blessings").orderByChild("target").equalTo(mId);
        mReceivedRef.addChildEventListener(blessingListener);
        this.mReceivedRef.addChildEventListener(blessingListener);
    }

    void onBlessingUpdated(DataSnapshot snapshot){
        if(!snapshot.exists()){
            throw new IllegalArgumentException("snapshot value doesn't exist");
        }
        String key = snapshot.getKey();
        if(mBlessings.containsKey(key)){
            mBlessings.get(key).update(snapshot);
        }else{
            mBlessings.put(key, Blessing.fromSnapshot(snapshot));
        }
    }

    void onBlessingRemoved(DataSnapshot snapshot){
        Blessing removedBlessing = mBlessings.remove(snapshot.getKey());
    }

    public Blessing getGrantedBlessing(String target){
        if(mGrantedBlessings.containsKey(target)){
            i
            mGrantedBlessings.get(target)
        }
        return null;

    }

    private void createBlessing(){

    }

    //return a blessing interface for granting/revoking permissions
    public Blessing bless(String target){
        Blessing result = getGrantedBlessing(target);
        if(result == null){
            result = new Blessing();
        }

        return result;
    }

    private ChildEventListener blessingListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            onBlessingUpdated(dataSnapshot);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            onBlessingUpdated(dataSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            onBlessingRemoved(dataSnapshot);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void addPermissionEventListener(String path, ValueEventListener listener){
        for(String bId: mReceivedBlessings){
            Blessing blessing = mBlessings.get(bId);
            if(blessing.)
        }
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
