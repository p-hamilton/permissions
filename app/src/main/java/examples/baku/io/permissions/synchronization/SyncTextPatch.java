package examples.baku.io.permissions.synchronization;

import java.util.concurrent.BlockingQueue;

/**
 * Created by phamilton on 6/24/16.
 */
public class SyncTextPatch{
    private int ver;
    private String patch;

    public SyncTextPatch() {}

//        public SyncTextPatch(int ver, String patch) {
//            this.ver = ver;
//            this.patch = patch;
//        }

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public String getPatch() {
        return patch;
    }

    public void setPatch(String patch) {
        this.patch = patch;
    }


}

