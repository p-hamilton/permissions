package examples.baku.io.permissions.examples;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import examples.baku.io.permissions.PermissionManager;
import examples.baku.io.permissions.discovery.DeviceData;
import examples.baku.io.permissions.PermissionService;
import examples.baku.io.permissions.R;
import examples.baku.io.permissions.discovery.DevicePickerActivity;
import examples.baku.io.permissions.synchronization.SyncText;

public class ComposeActivity extends AppCompatActivity implements ServiceConnection {

    public final static String EXTRA_MESSAGE_ID = "messageId";

    private String mOwner;
    private String mDeviceId;
    private String mId;
    private PermissionService mPermissionService;
    private DatabaseReference mMessageRef;
    private DatabaseReference mSyncedMessageRef;

    String sourceId;


    EditText mToText;
    EditText mFrom;
    EditText mSubject;
    EditText mMessage;

    TextInputLayout mToLayout;
    TextInputLayout mFromLayout;
    TextInputLayout mSubjectLayout;
    TextInputLayout mMessageLayout;


    Map<String, Integer> permissions = new HashMap<>();

    HashMap<String, SyncText> syncTexts = new HashMap<>();

    HashMap<String, ValueEventListener> listeners = new HashMap<>();
    HashMap<String, DataSnapshot> mSnapshots = new HashMap<>();
    DataSnapshot currentSnapshot;

    String original;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Compose Message");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_MESSAGE_ID)) {
                mId = intent.getStringExtra(EXTRA_MESSAGE_ID);
            }
            if (intent.hasExtra("sourceId")) {
                sourceId = intent.getStringExtra("sourceId");
            }
            if (intent.hasExtra("review")) {
                original = intent.getStringExtra("review");
                Log.e("asdasdasdsa", original);
            }

        }
        if (mId == null) {
            mId = UUID.randomUUID().toString();
        }


        mToText = (EditText) findViewById(R.id.composeTo);
        mToLayout = (TextInputLayout) findViewById(R.id.composeToLayout);

        mFrom = (EditText) findViewById(R.id.composeFrom);
        mFromLayout = (TextInputLayout) findViewById(R.id.composeFromLayout);

        mSubject = (EditText) findViewById(R.id.composeSubject);
        mSubjectLayout = (TextInputLayout) findViewById(R.id.composeSubjectLayout);

        mMessage = (EditText) findViewById(R.id.composeMessage);
        mMessageLayout = (TextInputLayout) findViewById(R.id.composeMessageLayout);

        bindService(new Intent(this, PermissionService.class), this, Service.BIND_AUTO_CREATE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            sendMessage();
        } else if (id == R.id.action_cast) {
            if (mPermissionService != null) {
                String dId = mPermissionService.getFocus();
                if (dId != null) {
                    String focus = mPermissionService.getFocus();

                    //set blessing permissions
                    mPermissionService.getPermissionManager().bless(focus)
                            .setPermissions("emails/messages/" + mId + "/to", 1)
                            .setPermissions("emails/messages/" + mId + "/from", 2);

                    //send launch intent
                    mPermissionService.getMessenger().to(focus).emit("cast", mId);


                } else {
                    Intent requestIntent = new Intent(ComposeActivity.this, DevicePickerActivity.class);
                    requestIntent.putExtra(DevicePickerActivity.EXTRA_REQUEST, DevicePickerActivity.REQUEST_DEVICE_ID);
                    startActivityForResult(requestIntent, DevicePickerActivity.REQUEST_DEVICE_ID);
                }

            }

        } else if (id == R.id.action_settings) {

        }

        return super.onOptionsItemSelected(item);
    }

    void sendMessage() {
        if (mOwner != null && mPermissionService != null) {
            try {
                JSONObject args = new JSONObject();
                args.put("deviceId", mDeviceId);
                args.put("messageId", mId);
                args.put("resourceKey", "send");
                args.put("description", "Attempitng to send message");
                mPermissionService.getMessenger().to(mOwner).emit("request", args.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            Message request = new Message("request");
//            request.getArguments().put("original", syncTexts.get("message").getOriginal());
//            request.getArguments().put("messageId", mId);
//            request.setTarget(mOwner);
//            mPermissionService.sendRequest(request);
        }
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DevicePickerActivity.REQUEST_DEVICE_ID && data != null && data.hasExtra(DevicePickerActivity.EXTRA_DEVICE_ID)) {
            String focus = data.getStringExtra(DevicePickerActivity.EXTRA_DEVICE_ID);
//            mPermissionService.getReference("emails/messages/" + mId + "/to").setPermission(focus, 1);
//            mPermissionService.getReference("emails/messages/" + mId + "/from").setPermission(focus, 2);
//            mPermissionService.getMessenger().to(focus).emit("cast", mId);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        PermissionService.PermissionServiceBinder binder = (PermissionService.PermissionServiceBinder) service;
        mPermissionService = binder.getInstance();

        mDeviceId = mPermissionService.getDeviceId();

        if (mPermissionService != null) {
            mMessageRef = mPermissionService.getFirebaseDB().getReference("emails").child("messages").child(mId);
//            mPermissionService.getPermissionManager().addPermissionEventListener("emails/messages/" + mId, new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if(!dataSnapshot.exists()){ //message doesn't exist, create it
//                        mMessageRef.child("id").setValue(mId);
//                    }else{
//                        finish();
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//
//            wrapTextField(mToLayout, "to");
//            wrapTextField(mFromLayout, "from");
//            wrapTextField(mSubjectLayout, "subject");
//            wrapTextField(mMessageLayout, "message");
//
//
//
//            mSyncedMessageRef = mPermissionService.getFirebaseDB().getReference("emails").child("syncedMessages").child(mId);
//
//
//
//            mPermissionRef = mPermissionService.getReference("emails/messages/"+mId);
//            mPermissionRef.addPermissionValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if(!dataSnapshot.exists()){
//                        mPermissionRef.setPermission(mDeviceId, PermissionManager.FLAG_READ | PermissionManager.FLAG_READ);
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//            PermissionManager.PermissionReference messagePermissionRef = mPermissionService.getReference("emails/messages/"+mId);
//            messagePermissionRef.addOnPermissionChangeListener(new PermissionManager.OnPermissionChangeListener() {
//                @Override
//                public void onPermissionChange(int current) {
//
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });


        }
    }


    private DataSnapshot sharingRules;

    public void setSharingRules(DataSnapshot sharingRules) {
        this.sharingRules = sharingRules;
    }

    int FLAG_CAN_EDIT = 2 << 1;

    int canEdit(String key) {
        if (mOwner.equals(mDeviceId)) {
            return FLAG_CAN_EDIT;
        } else if (sharingRules != null) {
            if (sharingRules.hasChild(mDeviceId) && sharingRules.child(mDeviceId).hasChild(key)) {
                return FLAG_CAN_EDIT;
            }
        }
        return 0;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    void wrapTextField(final TextInputLayout editContainer, final String key) {
        final EditText edit = editContainer.getEditText();

//        mPermissionService.getReference("emails/messages/" + mId + "/" + key).addOnPermissionChangeListener(new PermissionManager.OnPermissionChangeListener() {
//            @Override
//            public void onPermissionChange(int current) {
//                if (current == 0) {
//                    edit.setEnabled(true);
//                    edit.setOnClickListener(null);
//                    edit.setFocusable(true);
//                    edit.setBackgroundColor(Color.TRANSPARENT);
//                } else if (current == 1) {
////            edit.setInputType(EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
//                    edit.setEnabled(false);
//                    edit.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            sendMessage();
//                        }
//                    });
//                } else if (current == (PermissionManager.FLAG_READ | PermissionManager.FLAG_WRITE)) {
////                        edit.setEnabled(false);
//                    edit.setFocusable(false);
//                    edit.setBackgroundColor(Color.BLACK);
//                    edit.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Toast.makeText(ComposeActivity.this, "Requesting permission...", 0).show();
//                            try {
//                                JSONObject args = new JSONObject();
//                                args.put("deviceId", mDeviceId);
//                                args.put("messageId", mId);
//                                args.put("resourceKey", key);
//                                args.put("description", "Requesting access to " + key);
//                                mPermissionService.getMessenger().to(mOwner).emit("request", args.toString());
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    void unlinkTextField(String key) {
        if (syncTexts.containsKey(key)) {
            syncTexts.get(key).unlink();
        }

//        if(mMessageRef != null && listeners.containsKey(key)){
//            mMessageRef.child(key).removeEventListener(listeners.get(key));
//        }
    }

    void linkTextField(final EditText edit, final String key) {


        final SyncText syncText = new SyncText(mSyncedMessageRef.child(key), mMessageRef.child(key));
        syncTexts.put(key, syncText);

        syncText.setOnTextChangeListener(new SyncText.OnTextChangeListener() {
            @Override
            public void onTextChange(final String currentText) {
                final int sel = Math.min(edit.getSelectionStart(), currentText.length());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edit.setText(currentText);
                        if (sel > -1) {
                            edit.setSelection(sel);
                        }
                    }
                });
            }
        });


        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                syncText.update(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unlinkTextField("to");
        unlinkTextField("form");
        unlinkTextField("subject");
        unlinkTextField("message");
        unbindService(this);
    }
}
