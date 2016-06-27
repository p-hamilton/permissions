package examples.baku.io.permissions;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import examples.baku.io.permissions.util.EventFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class DevicePickerActivityFragment extends EventFragment {

    public DevicePickerActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_picker, container, false);
    }
}
