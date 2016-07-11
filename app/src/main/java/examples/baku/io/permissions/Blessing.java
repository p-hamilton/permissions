package examples.baku.io.permissions;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by phamilton on 7/9/16.
 */
public class Blessing {

    static final String KEY_PERMISSIONS = "_permissions";
    static final String KEY_RULES = "_rules";

    private String id;
    private String pattern;
    private String source;
    private String target;
    private DatabaseReference ref;
    private DatabaseReference rulesRef;
    private DataSnapshot snapshot;

    final private Map<String, PermissionReference> refCache = new HashMap<>();


    public Blessing(){}



    public ValueEventListener addPermissionEventListener(String path, ValueEventListener listener){
        getRef(path).addPermissionValueEventListener(listener);
        return listener;
    }

    public Blessing setPermissions(String path, int permissions){
        getRef(path).setPermission(permissions);
        return this;
    }

    public Blessing clearPermissions(String path){
        getRef(path).clearPermission();
        return this;

    }
    public PermissionReference getRef(String path){
        PermissionReference result = null;
        if(refCache.containsKey(path)){
            result = refCache.get(path);
        }else{
            result = new PermissionReference(rulesRef, path);
            refCache.put(path, result);
        }
        return result;
    }

    public void update(DataSnapshot snapshot){

    }

    public void setRef(DatabaseReference ref) {
        this.ref = ref;
        this.rulesRef = ref.child(KEY_RULES);
    }

    static Blessing fromSnapshot(DataSnapshot snapshot){
        Blessing result = new Blessing();
        return result;
    }

    static Blessing fromTemplate(DatabaseReference reference, DataSnapshot snapshot){
        Blessing result = new Blessing();
        return result;
    }


}
