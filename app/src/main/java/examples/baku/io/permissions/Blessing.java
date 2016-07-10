package examples.baku.io.permissions;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by phamilton on 7/9/16.
 */
public class Blessing {

    private String id;
    private String pattern;
    private String source;
    private String target;
    private DatabaseReference reference;

    final private Map<String, PermissionReference> refCache = new HashMap<>();

    public ValueEventListener addPermissionEventListener(String path, ValueEventListener listener){
        getRef(path).addPermissionValueEventListener(listener);
        return listener;
    }

    public void setPermissions(String path, int permissions){
        getRef(path).setPermission(permissions);
    }

    public PermissionReference getRef(String path){
        PermissionReference result = null;
        if(refCache.containsKey(path)){
            result = refCache.get(path);
        }else{
            result = new PermissionReference(reference, path);
            refCache.put(path, result);
        }
        return result;
    }
}
