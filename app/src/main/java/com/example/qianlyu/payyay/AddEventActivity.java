package com.example.qianlyu.payyay;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class AddEventActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseUser user;

    private Button btn_save;
    private EditText description;
    private List<String> participants = new ArrayList<>();
    private EditText textIn;
    private Button buttonAdd;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_36dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddEventActivity.this, MainActivity.class));
                finish();
            }
        });

        btn_save = (Button) findViewById(R.id.save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String uniqueID = UUID.randomUUID().toString();
                participants.add(User.curr_user.getEmailAddress().replace(".", ","));
                participants = new ArrayList<String>(new HashSet<String>(participants));

                if(participants.size() == 1){
                    Toast.makeText(AddEventActivity.this, "An event should contain more than one person!", Toast.LENGTH_SHORT);
                    return;
                }
                database.getReference().child("events").child(uniqueID).child(Event.TIME).setValue(User.df.format(new Date()));
                database.getReference().child("events").child(uniqueID).child(Event.DESCRIPTION)
                        .setValue(description.getText().toString());
                database.getReference().child("events").child(uniqueID).child(Event.BILLS).setValue("false");
                database.getReference().child("users_email").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, Object> objectMap = (HashMap<String, Object>)
                                dataSnapshot.getValue();

                        for(String email:participants){
                            if(objectMap.containsKey(email)){
                                database.getReference().child("events").child(uniqueID).child(Event.PARTICIPANTS)
                                        .child((String)objectMap.get(email)).setValue("true");
                                database.getReference().child("users").child((String)objectMap.get(email)).child(User.EVENTS).child(uniqueID).setValue("true");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                startActivity(new Intent(AddEventActivity.this, MainActivity.class));
                finish();
            }
        });

        description = (EditText) findViewById(R.id.description);
        textIn = (EditText)findViewById(R.id.textin);
        buttonAdd = (Button)findViewById(R.id.add);
        container = (LinearLayout)findViewById(R.id.container);

        buttonAdd.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                LayoutInflater layoutInflater =
                        (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                String temp = textIn.getText().toString().replace(".", ",");
                participants.add(temp);
                final View addView = layoutInflater.inflate(R.layout.row, null);
                final TextView textOut = (TextView)addView.findViewById(R.id.textout);
                textOut.setText(textIn.getText().toString());
                Button buttonRemove = (Button)addView.findViewById(R.id.remove);
                buttonRemove.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        ((LinearLayout)addView.getParent()).removeView(addView);
                        participants.remove(textOut.getText().toString().replace(".", ","));
                        System.out.println("participant size: " + participants.size());
                    }});

                container.addView(addView);
            }
        });

    }
}
