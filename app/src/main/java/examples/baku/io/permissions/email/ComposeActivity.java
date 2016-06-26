package examples.baku.io.permissions.email;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.UUID;

import examples.baku.io.permissions.PermissionRequest;
import examples.baku.io.permissions.PermissionService;
import examples.baku.io.permissions.R;
import examples.baku.io.permissions.synchronization.SyncText;

public class ComposeActivity extends AppCompatActivity implements ServiceConnection{

    public final static String EXTRA_MESSAGE_ID = "messageId";

    private String mId;
    private PermissionService mPermissionService;
    private DatabaseReference mMessageRef;
    private DatabaseReference mSyncedMessageRef;

    String sourceId;

    SyncText mTo;

    EditText mToText;
    EditText mFrom;
    EditText mSubject;
    EditText mMessage;

    TextInputLayout mToLayout;
    TextInputLayout mFromLayout;
    TextInputLayout mSubjectLayout;
    TextInputLayout mMessageLayout;


    HashMap<String,SyncText> syncTexts = new HashMap<>();

    HashMap<String,ValueEventListener> listeners = new HashMap<>();
    HashMap<String,DataSnapshot> mSnapshots = new HashMap<>();
    DataSnapshot currentSnapshot;

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
        if(intent != null){
            if(intent.hasExtra(EXTRA_MESSAGE_ID)){
                mId = intent.getStringExtra(EXTRA_MESSAGE_ID);
            }
            if(intent.hasExtra("sourceId")){
                sourceId = intent.getStringExtra("sourceId");
            }

        }
        if(mId == null){
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
        }else if(id == R.id.action_cast){
            if(mPermissionService != null){
                PermissionRequest request = new PermissionRequest("cast");
                request.getArguments().put("messageId", mId);
                mPermissionService.sendRequest(request);
            }

        }else if(id == R.id.action_settings){

        }

        return super.onOptionsItemSelected(item);
    }

    void sendMessage(){
//        if(mMessageRef != null){
//            mMessageRef.setValue(mMessageData);
//        }
        if(sourceId != null && mPermissionService != null){
            mPermissionService.sendRequest(new PermissionRequest("request"));
        }
        finish();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        PermissionService.PermissionServiceBinder binder = (PermissionService.PermissionServiceBinder)service;
        mPermissionService = binder.getInstance();
        if(mPermissionService != null){
            mMessageRef = mPermissionService.getFirebaseDB().getReference("emails").child("messages").child(mId);
            mSyncedMessageRef = mPermissionService.getFirebaseDB().getReference("emails").child("syncedMessages").child(mId);

            mMessageRef.child("id").setValue(mId);

            linkTextField(mToText, "to");
            linkTextField(mFrom, "from");
            linkTextField(mSubject, "subject");
            linkTextField(mMessage, "message");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    void unlinkTextField(String key){
        if(syncTexts.containsKey(key)){
            syncTexts.get(key).unlink();
        }

//        if(mMessageRef != null && listeners.containsKey(key)){
//            mMessageRef.child(key).removeEventListener(listeners.get(key));
//        }
    }

    void linkTextField(final EditText edit, final String key){

        final SyncText syncText = new SyncText(mSyncedMessageRef.child(key),mMessageRef.child(key));
        syncTexts.put(key, syncText);

        syncText.setOnTextChangeListener(new SyncText.OnTextChangeListener() {
            @Override
            public void onTextChange(final String currentText) {
                Log.e("mistake ",currentText);
                final int sel = Math.min(edit.getSelectionStart(), currentText.length());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        edit.setText(currentText);
                        if(sel > -1){
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
