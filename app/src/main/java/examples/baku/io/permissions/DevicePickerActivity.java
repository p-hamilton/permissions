package examples.baku.io.permissions;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.LinkedHashMap;
import java.util.Map;

import examples.baku.io.permissions.util.EventFragment;

public class DevicePickerActivity extends AppCompatActivity implements EventFragment.EventFragmentListener, ServiceConnection {

    private PermissionService mPermissionService;
    private Map<String, DeviceData> mDevices;
    private DevicePickerActivityFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));  //close notification tray

        setContentView(R.layout.content_device_picker);
        PermissionService.bind(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        mFragment = (DevicePickerActivityFragment)fragment;
        mFragment.setDevices(mDevices);
    }

    @Override
    public boolean onFragmentEvent(int action, Bundle args, EventFragment fragment) {
        switch(action){
            case DevicePickerActivityFragment.EVENT_ITEMCLICKED:
                String dId = args.getString(DevicePickerActivityFragment.ARG_DEVICE_ID);
                if(dId != null){
                    mPermissionService.setFocus(dId);
                    finish();
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mPermissionService = ((PermissionService.PermissionServiceBinder)service).getInstance();
        mDevices = mPermissionService.getDiscovered();
        if(mFragment != null){
            mFragment.setDevices(mDevices);
        }
        mPermissionService.addDiscoveryListener(new PermissionService.DiscoveryListener() {
            @Override
            public void onChange(Map<String, DeviceData> devices) {
                if(mFragment != null){
                    mFragment.setDevices(mDevices);
                }
            }

            @Override
            public void onDisassociate(String deviceId) {

            }
        });
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }
}
