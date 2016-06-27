package examples.baku.io.permissions;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import examples.baku.io.permissions.util.EventFragment;

public class DevicePickerActivity extends AppCompatActivity implements EventFragment.EventFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_device_picker);
    }

    @Override
    public boolean onFragmentEvent(int action, Bundle args, EventFragment fragment) {
        return false;
    }
}
