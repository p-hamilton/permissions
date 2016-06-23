package examples.baku.io.permissions;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ComposeActivity extends AppCompatActivity {

    EditText mTo;
    EditText mFrom;
    EditText mSubject;
    EditText mMessage;

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

        mTo = (EditText) findViewById(R.id.composeTo);
        mFrom = (EditText) findViewById(R.id.composeFrom);
        mSubject = (EditText) findViewById(R.id.composeSubject);
        mMessage = (EditText) findViewById(R.id.composeMessage);
    }

    void sendMessage(){
        finish();
    }

}
