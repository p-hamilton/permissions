package examples.baku.io.permissions;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class EmailActivity extends AppCompatActivity implements ServiceConnection{


    private static final String TAG = PermissionService.class.getSimpleName();
    static void l(String msg){
        Log.e(TAG, msg);
    }   //TODO: real logging

    private PermissionService mPermissionService;
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mMessagesRef;

    private RecyclerView mInboxRecyclerView;
    private mMessagesAdapter mInboxAdapter;
    private LinearLayoutManager mLayoutManager;

    private LinkedHashMap<String, MessageData> mMessages = new LinkedHashMap<>();

    private ArrayList<String> mMessageOrder = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(EmailActivity.this, ComposeActivity.class));

                }
            });
        }

        bindService(new Intent(this, PermissionService.class), this, Service.BIND_AUTO_CREATE);

        mInboxAdapter = new mMessagesAdapter(mMessages);
        mLayoutManager = new LinearLayoutManager(this);
        mInboxRecyclerView = (RecyclerView) findViewById(R.id.inboxRecyclerView);
        mInboxRecyclerView.setLayoutManager(mLayoutManager);
        mInboxRecyclerView.setAdapter(mInboxAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(mInboxRecyclerView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_permission, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from list and notify the RecyclerView
        }


    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        PermissionService.PermissionServiceBinder binder = (PermissionService.PermissionServiceBinder)service;
        mPermissionService = binder.getInstance();
        if(mPermissionService != null){
            mFirebaseDB = mPermissionService.getFirebaseDB();
            mMessagesRef = mFirebaseDB.getReference("emails");
            mMessagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    onMessagesUpdated(dataSnapshot);
                    mInboxAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    void onMessagesUpdated(DataSnapshot snapshot){
        if(snapshot == null) throw new IllegalArgumentException("null snapshot");

        mMessages.clear();
        for (Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator(); iterator.hasNext(); ) {
            DataSnapshot snap =  iterator.next();
            onMessageUpdated(snapshot);
        }
    }

    void onMessageUpdated(DataSnapshot snapshot){
        if(snapshot == null) throw new IllegalArgumentException("null snapshot");

        try{
            MessageData msg = snapshot.getValue(MessageData.class);
            mMessages.put(msg.getId(), msg);
        }catch (DatabaseException e){
            e.printStackTrace();
            mMessages.put(snapshot.getKey(), null);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;
        }
    }
    public class mMessagesAdapter extends RecyclerView.Adapter<ViewHolder> {
        private LinkedHashMap<String, MessageData> mDataset;

        public mMessagesAdapter(LinkedHashMap<String, MessageData> dataset) {
            if(dataset == null) throw new IllegalArgumentException("null dataset");
            mDataset = dataset;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
            // create a new view
            CardView v = (CardView)LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inbox_card_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
//            holder.mCardView.setText(mDataset[position]);

        }

        @Override
        public int getItemCount() {
            return mDataset.size();//mDataset.length;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }
}
