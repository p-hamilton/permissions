package examples.baku.io.permissions;


import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
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
    static final int FLAG_REQUEST = 1 << 2;
    static final int FLAG_POINT = 1 << 3;

    final Map<String, ResourceReference> resources = new HashMap<>();
    final Map<String, Set<String>> groupMembership = new HashMap<>();

    //TODO: replace string id with Auth
    public PermissionManager(DatabaseReference databaseReference, String owner){
        this.mDatabaseReference = databaseReference;
    }



//    public RequestBuilder request(String path){
//        return new RequestBuilder();
//    }

    public boolean point(String path){
        return false;
    }

    public ResourceReference defineResourceRules(String path){
        return new ResourceReference(path);
    }

    public void setRule(){

    }


    public class Rule{
        int flags;
    }

    public class ResourceReference {
        private String id;
        private Rule defaultRule;
        private OnRequestListener onRequestListener;
        private OnPointListener onPointListener;

        private final Map<String, Rule> rules = new HashMap<>();    //key is group id


        public ResourceReference(String id) {
            this.id = id;
            this.defaultRule = new Rule();
            this.rules.put(null, defaultRule);
        }

        public void setOnRequestListener(OnRequestListener onRequestListener) {
            this.onRequestListener = onRequestListener;
        }

        public void setOnPointListener(OnPointListener onPointListener) {
            this.onPointListener = onPointListener;
        }

        public void setDefaultRule(Rule defaultRule) {
            this.defaultRule = defaultRule;
        }

        public Rule getDefaultRule() {
            return defaultRule;
        }

        public Rule getRule(String group){
            return rules.get(group);
        }
    }

    public interface OnRequestListener{
        void onRequest(PermissionRequest request, ResourceReference resource);
    }

    public interface OnPointListener{
        void onPoint();
    }

    public interface ResourcePermissionWrapper{
        void onChange(int current, int previous);
    }
}
