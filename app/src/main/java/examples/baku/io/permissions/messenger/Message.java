package examples.baku.io.permissions.messenger;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Created by phamilton on 6/19/16.
 */
public class Message implements Parcelable{
    private String id;
    private String parent;
    private String type;
    private String target;
    private String source;
    private String message;
    private boolean callback;

    public Message(){}

    public Message(String type) {
        this(type, null);
    }

    public Message(String type, String message){
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCallback() {
        return callback;
    }

    public void setCallback(boolean callback) {
        this.callback = callback;
    }


    public Message getChildInstance(){
        Message result = new Message();
        result.id = UUID.randomUUID().toString();
        result.parent = this.id;
        result.type = this.type;
        result.target = this.target;
        result.source = this.source;
        result.message = this.message;
        result.callback = this.callback;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(type);
        dest.writeString(target);
        dest.writeString(message);
        dest.writeByte((byte) (callback ? 1 : 0));
    }

    private Message(Parcel in) {
        this.id = in.readString();
        this.type = in.readString();
        this.target = in.readString();
        this.message = in.readString();
        this.callback = in.readByte() != 0;

    }

    public static final Creator<Message> CREATOR
            = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[0];
        }

    };
}
