package com.example.qianlyu.payyay;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddFriendActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseUser user;
    private DatabaseReference m_ref ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();
        m_ref = database.getReference();

        System.out.println("email: " + currentUser.getEmail()
                + "\ndisplayName: " + currentUser.getDisplayName()
                + "\nprobiderID: " + currentUser.getProviderId()
                + "\nuserID: " + currentUser.getUid());

        final EditText searchFriend = (EditText) findViewById(R.id.search_friend);
        Button submitSF = (Button) findViewById(R.id.submit_search_friend);

        submitSF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchFriend.getText() != null) {
                    String emailKey = searchFriend.getText().toString().replace(".", ",");
                    m_ref.child("users_email").child(emailKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() == null){
                                Toast.makeText(AddFriendActivity.this, "This user does not exists!", Toast.LENGTH_LONG).show();
                            } else {
                                String userID = dataSnapshot.getValue(String.class);
                                System.out.println("userID: " + userID);
                                m_ref.child("users").child(user.getUid()).child(User.FRIENDS)
                                        .child(userID).setValue("true");
                                Toast.makeText(AddFriendActivity.this, "Add friend Success!", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    startActivity(new Intent(AddFriendActivity.this, FriendListActivity.class));
                    finish();
                }
            }
        });


    }

    private String emailToKey(String emailAddress) {
        return emailAddress.replaceAll(".", ",");
    }
}
