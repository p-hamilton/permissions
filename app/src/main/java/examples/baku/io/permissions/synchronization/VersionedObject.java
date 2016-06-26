package examples.baku.io.permissions.synchronization;

/**
 * Created by phamilton on 6/24/16.
 */
public class VersionedObject <T> {
    private T value;
    private int version;

    public VersionedObject() {
    }

    public VersionedObject(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void update(T value){
        this.value = value;
        this.version++;
    }
}
