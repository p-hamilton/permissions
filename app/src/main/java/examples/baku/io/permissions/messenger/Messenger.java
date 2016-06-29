package examples.baku.io.permissions.messenger;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by phamilton on 6/28/16.
 *
 * !!!!!!!!!FOR PROTOTYPING ONLY!!!!!!!
 * Messaging on top of Firebase real-time database.
 * Handles single target messaging only.
 * Stand in for something like socket.io.
 */
public class Messenger implements ChildEventListener {

    static final String KEY_TARGET = "target";

    private String mId;
    private DatabaseReference mReference;

    final private Map<String, Listener> mListeners = new HashMap<>();
    final private Map<String, Ack> mCallbacks = new HashMap<>();

    public Messenger(String id, DatabaseReference reference) {
        this.mId = id;
        this.mReference = reference;
        this.mReference.orderByChild(KEY_TARGET).equalTo(mId).addChildEventListener(this);
    }

    public Emitter to(final String target){
        return new Emitter() {
            @Override
            public void emit(String event, String msg, Ack callback) {
                if(event == null) throw new IllegalArgumentException("event argument can't be null.");

                Message message = new Message(event, msg);
                message.setSource(mId);

                if(callback != null){
                    mCallbacks.put(message.getId(), callback);
                    message.setCallback(true);
                }
                mReference.child(message.getId()).setValue(message);
            }
        };
    }

    public void on(String event, Listener listener){
        if(listener == null){//remove current
            off(event);
        }else{
            mListeners.put(event, listener);
        }
    }

    public void off(String event){
        if(mListeners.containsKey(event)){
            mListeners.remove(event);
        }
    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Message message = null;
        try{
            message = dataSnapshot.getValue(Message.class);

        }catch(DatabaseException e){
            e.printStackTrace();
        }

        if(message!= null){
            handleMessage(message);
        }

        //remove from database.
        dataSnapshot.getRef().removeValue();
    }

    private boolean handleMessage(final Message message) {
        String event = message.getType();
        if(mListeners.containsKey(event)){
            Ack callback = null;
            if(message.isCallback()){
                //route response to sending messenger
                callback = new Ack() {
                    @Override
                    public void call(String args) {
                        to(message.getSource()).emit(message.getId(), args);
                    }
                };
            }
            mListeners.get(event).call(message.getMessage(), callback);
            return true;

        //assume that none of the event listeners match the uuid of a message
        }else if(mCallbacks.containsKey(event)){
            mCallbacks.get(event).call(message.getMessage());
            mCallbacks.remove(event);
            return true;
        }
        return false;
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        databaseError.toException().printStackTrace();
    }

    public void disconnect(){
        mReference.removeEventListener(this);
    }

    public abstract class Emitter{
        public void emit(String event, String msg){
            emit(event, msg, null);
        }

        abstract public void emit(String event, String msg, Ack callback);
    }

    public interface Listener{
        void call(String args, Ack callback);
    }

    public interface Ack{
        void call(String args);
    }
}
