package examples.baku.io.permissions;

import com.google.android.gms.internal.zzahp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by phamilton on 7/9/16.
 */
public class PermissionTree {

    String key;
    private int permission;
    private final Map<String, PermissionTree> children = new HashMap<>();

    public PermissionTree(String key, int permission) {
        this.key = key;
        this.permission = permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public void addChild(PermissionTree child){
        children.put(child.key, child);
    }

    public void removeChild(String id){
        children.remove(id);
    }

    public int getPermission(){
        return permission;
    }


    public int getPermissionAt(String path, int acc){
        if(path == null){
            throw new IllegalArgumentException("illegal path value");
        }
        acc |= getPermission();
        String[] pathItems = path.split("/");
        PermissionTree currentNode = this;
        for (int i = 0; i < pathItems.length; i++) {
            if(currentNode.children.containsKey())
        }
        return acc;
    }

    static PermissionTree fromSnapshot(DataSnapshot snapshot){
        if(!snapshot.exists()){
            throw new IllegalArgumentException("empty snapshot");
        }
        PermissionTree result = new PermissionTree(snapshot.getKey(), 0);
        for(DataSnapshot childSnapshot: snapshot.getChildren()){
            if(PermissionManager.KEY_PERMISSIONS.equals(childSnapshot.getKey())){
                result.setPermission(childSnapshot.getValue(Integer.class));
            }else{
                result.addChild(fromSnapshot(childSnapshot));
            }
        }
        return result;
    }
}
