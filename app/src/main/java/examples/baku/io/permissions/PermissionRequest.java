package examples.baku.io.permissions;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by phamilton on 6/19/16.
 */
public class PermissionRequest implements Parcelable{
    private String id;
    private String type;
    private String target;
    private Map<String, String> arguments = new HashMap<>();

    public PermissionRequest(){}

    public PermissionRequest(String type){
        this(UUID.randomUUID().toString(), type);
    }
    public PermissionRequest(String id, String type){
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(target);
        dest.writeMap(arguments);
    }

    private PermissionRequest(Parcel in) {
        this.id = in.readString();
        this.type = in.readString();
        this.target = in.readString();
        in.readHashMap(new ClassLoader() {
        });
        this.arguments = in.readHashMap(String.class.getClassLoader());
    }

    public static final Creator<PermissionRequest> CREATOR
            = new Creator<PermissionRequest>() {
        @Override
        public PermissionRequest createFromParcel(Parcel source) {
            PermissionRequest result = new PermissionRequest();
            result.id = source.readString();
            result.type = source.readString();
            result.target = source.readString();
            source.readMap(result.arguments, String.class.getClassLoader());
            return result;
        }

        @Override
        public PermissionRequest[] newArray(int size) {
            return new PermissionRequest[0];
        }

    };
}
