package com.example.qianlyu.payyay;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class SetupGroupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference m_ref ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_group);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        m_ref = database.getReference();

        final EditText searchFriend = (EditText) findViewById(R.id.search_friendG);
        Button submitSF = (Button) findViewById(R.id.submit_search_friendG);

        submitSF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchFriend.getText() != null){
                    String emailAddress = searchFriend.getText().toString();
                    m_ref.child("users_email").child(emailAddress.replace(".", ",")).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String groupID = UUID.randomUUID().toString();
                            String addUserID = dataSnapshot.getValue(String.class);
                            m_ref.child("groups").child(groupID).child("userGroup").child(addUserID).setValue(0);
                            m_ref.child("groups").child(groupID).child("userGroup").child(currentUser.getUid()).setValue(0);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

    }
}
