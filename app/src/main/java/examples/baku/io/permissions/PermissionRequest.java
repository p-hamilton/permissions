package examples.baku.io.permissions;

/**
 * Created by phamilton on 6/28/16.
 */

//TODO: multiple resources (Request groups)
public class PermissionRequest {

    private int granted;
    private String path;


    public void grant(int permissions){
        this.granted |= permissions;
    }
    public void retract(int permissions){
        this.granted &= ~permissions;
    }

    public void deny(){
        cancel();
    }

    public void cancel(){

    }

    public class Builder{
        private PermissionRequest request;

        public Builder(String path){
            this.request = new PermissionRequest();
        }


    }

}
