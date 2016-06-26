package examples.baku.io.permissions;

/**
 * Created by phamilton on 6/19/16.
 */
public class DeviceData {

    private String id;
    private String name;
    private Boolean active;

    public DeviceData(){}

    public DeviceData(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
