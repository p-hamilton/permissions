package examples.baku.io.permissions;

import com.google.android.gms.internal.zzahp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

/**
 * Created by phamilton on 7/9/16.
 */
public class Rules{

    private DatabaseReference reference;

    public ValueEventListener addPermissionEventListener(String path, ValueEventListener listener){
        getRef(path).addPermissionValueEventListener(listener);
        return listener;
    }

    public void setPermissions(String path, int permissions){
        getRef(path).setP
    }

    public PermissionReference getRef(String path){
        return new PermissionReference(reference, path);
    }
}
