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

    public PermissionReference(DatabaseReference root, String path) {
        this.mPermissionReference = root.child(path).child(PermissionManager.KEY_PERMISSIONS);
    }

    public void setPermission(int permission) {
        mPermissionReference.setValue(permission);
    }

    public void clearPermission() {
        mPermissionReference.removeValue();
    }

}