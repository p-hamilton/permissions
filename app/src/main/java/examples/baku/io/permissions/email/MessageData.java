package examples.baku.io.permissions.email;

/**
 * Created by phamilton on 6/22/16.
 */
public class MessageData {

    String id;
    String to = "";
    String from = "";
    String subject = "";
    String message = "";

    public MessageData(){}

    public MessageData(String id, String to, String from, String subject, String message) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to != null ? to : "";
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from != null ? from : "";
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject != null ? subject : "";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message != null ? message : "";
    }
}
