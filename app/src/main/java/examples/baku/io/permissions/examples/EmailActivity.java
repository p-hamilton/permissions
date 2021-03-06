package examples.baku.io.permissions.examples;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import examples.baku.io.permissions.PermissionService;
import examples.baku.io.permissions.R;
import examples.baku.io.permissions.discovery.DevicePickerActivity;

public class EmailActivity extends AppCompatActivity implements ServiceConnection {


    private static final String TAG = PermissionService.class.getSimpleName();

    static void l(String msg) {
        Log.e(TAG, msg);
    }   //TODO: real logging

    public static final String KEY_DOCUMENTS = "documents";
    public static final String KEY_EMAILS = "emails";
    public static final String KEY_MESSAGES = "messages";

    private PermissionService mPermissionService;
    private String mDeviceId;
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mMessagesRef;

    private RecyclerView mInboxRecyclerView;
    private MessagesAdapter mInboxAdapter;
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
//                    mPermissionService.getDeviceBlessing().setPermissions("documents/" + mDeviceId +"/snake", new Random().nextInt());

                }
            });
        }

        PermissionService.start(this);
        PermissionService.bind(this);

        mInboxAdapter = new MessagesAdapter(mMessages);
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
        if (id == R.id.action_cast) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from list and notify the RecyclerView
            int pos = viewHolder.getAdapterPosition();
            MessageData item = mInboxAdapter.getItem(pos);
            if (item != null) {
                mMessagesRef.child(item.getId()).removeValue();
            }
        }
    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        PermissionService.PermissionServiceBinder binder = (PermissionService.PermissionServiceBinder) service;
        mPermissionService = binder.getInstance();
        if (mPermissionService != null) {
            mDeviceId = mPermissionService.getDeviceId();
            mFirebaseDB = mPermissionService.getFirebaseDB();
            mMessagesRef = mFirebaseDB.getReference(KEY_DOCUMENTS).child(mDeviceId).child(KEY_EMAILS).child(KEY_MESSAGES);
            mMessagesRef.addValueEventListener(messagesValueListener);
            mMessagesRef.addChildEventListener(messageChildListener);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }


    private ChildEventListener messageChildListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            onMessageUpdated(dataSnapshot);
            mInboxAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            onMessageUpdated(dataSnapshot);
            mInboxAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            onMessageRemoved(dataSnapshot.getKey());
            mInboxAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener messagesValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            onMessagesUpdated(dataSnapshot);
            mInboxAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    void onMessagesUpdated(DataSnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("null snapshot");

        mMessages.clear();
        for (Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator(); iterator.hasNext(); ) {
            DataSnapshot snap = iterator.next();
            onMessageUpdated(snap);
        }
    }

    void onMessageUpdated(DataSnapshot snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("null snapshot");

        try {
            MessageData msg = snapshot.getValue(MessageData.class);
            String key = msg.getId();
            mMessages.put(key, msg);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    void onMessageRemoved(String id) {
        if (mMessages.containsKey(id)) {
            mMessages.remove(id);
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;

        public ViewHolder(CardView v) {
            super(v);
            mCardView = v;

            mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DevicePickerActivity.REQUEST_DEVICE_ID && data != null && data.hasExtra(DevicePickerActivity.EXTRA_DEVICE_ID)) {
            String focus = data.getStringExtra(DevicePickerActivity.EXTRA_DEVICE_ID);
            String path = data.getStringExtra(DevicePickerActivity.EXTRA_REQUEST_ARGS);

            mPermissionService.getPermissionManager().bless(focus)
                    .setPermissions(path, 3);
            JSONObject castArgs = new JSONObject();
            try {
                castArgs.put("activity", ComposeActivity.class.getSimpleName());
                castArgs.put(ComposeActivity.EXTRA_MESSAGE_PATH, path);
                mPermissionService.getMessenger().to(focus).emit("cast", castArgs.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class MessagesAdapter extends RecyclerView.Adapter<ViewHolder> {
        private LinkedHashMap<String, MessageData> mDataset;

        public MessagesAdapter(LinkedHashMap<String, MessageData> dataset) {
            if (dataset == null) throw new IllegalArgumentException("null dataset");
            setDataset(dataset);
        }

        public void setDataset(LinkedHashMap<String, MessageData> mDataset) {
            this.mDataset = mDataset;
        }

        public MessageData getItem(int position) {
            List<String> order = new ArrayList<>(mDataset.keySet());
            return mDataset.get(order.get(position));
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            // create a new view
            CardView v = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inbox_card_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final MessageData item = getItem(position);

            String title = item.getFrom();
            if (title != null) {
                TextView titleView = (TextView) holder.mCardView.findViewById(R.id.card_title);
                titleView.setText(item.getFrom());
                TextView subtitleView = (TextView) holder.mCardView.findViewById(R.id.card_subtitle);
                subtitleView.setText(item.getSubject());
            }

            ImageView castButton = (ImageView) holder.mCardView.findViewById(R.id.card_trailing);
            castButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //choose device
                    Intent requestIntent = new Intent(EmailActivity.this, DevicePickerActivity.class);
                    String path = EmailActivity.KEY_DOCUMENTS
                            + "/" + mDeviceId
                            + "/" + EmailActivity.KEY_EMAILS
                            + "/" + EmailActivity.KEY_MESSAGES
                            + "/" + item.getId();
                    requestIntent.putExtra(DevicePickerActivity.EXTRA_REQUEST, DevicePickerActivity.REQUEST_DEVICE_ID);
                    requestIntent.putExtra(DevicePickerActivity.EXTRA_REQUEST_ARGS, path);
                    startActivityForResult(requestIntent, DevicePickerActivity.REQUEST_DEVICE_ID);
                }
            });

            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EmailActivity.this, ComposeActivity.class);
                    intent.putExtra(ComposeActivity.EXTRA_MESSAGE_ID, item.getId());
                    startActivityForResult(intent, 0);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }
}
