package examples.baku.io.permissions.examples;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import examples.baku.io.permissions.R;
import examples.baku.io.permissions.util.EventFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class InboxFragment extends EventFragment {

    public InboxFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inbox_list, container, false);


        return view;
    }


}
