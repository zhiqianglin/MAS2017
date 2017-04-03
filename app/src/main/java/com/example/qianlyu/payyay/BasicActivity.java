package com.example.qianlyu.payyay;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class BasicActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    // public navigaton view for every inside activity.
    private NavigationView navigationView;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
        if(User.curr_user == null || !User.curr_user.getUserID().equals(auth.getCurrentUser().getUid()))
            User.curr_user = new User(user.getUid(), user.getEmail());
    }

    public void set(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(user != null){
            Log.d("User: ", user.getEmail());
            DatabaseReference ref = database.getReference().child("users").child(user.getUid());
            // get all the user profile from database for future use
            ref.addValueEventListener(new ValueEventListener(){
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // the directory under user_id
                    if(dataSnapshot.getValue() == null){
                        User.curr_user.setPreferredName(user.getEmail().split("@")[0]);
                    }else {
                        SimpleDateFormat sdfmt2= new SimpleDateFormat("MM/DD/yy hh:mm:ss");
                        HashMap<String, Object> objectMap = (HashMap<String, Object>)
                                dataSnapshot.getValue();
                        for(String key:objectMap.keySet()){
                            if(key.equals(User.DISPALY_NAME)){
                                // get the user's display name.
                                User.curr_user.setPreferredName(objectMap.get(key).toString());
                                TextView disName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.prefer_name);
                                disName.setText(User.curr_user.getPreferredName());
                            }else if(key.equals(User.CREATE_TIME)){
                                // get the create time of user.
                                try {
                                    User.curr_user.setCreatedTime(sdfmt2.parse(objectMap.get(key).toString()));
                                }catch (ParseException e){
                                    e.printStackTrace();
                                }
                            }else if(key.equals(User.EMAIL_ADDRESS)){
                                // get the email address of the user.
                                User.curr_user.setEmailAddress(objectMap.get(key).toString());
                            }else if(key.equals(User.FIGURE_PATH)){
                                // get the figure image path stored in storage.
                                User.curr_user.setFigurePath(objectMap.get(key).toString());
                                new DownloadImageTask((ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageView))
                                        .execute(User.curr_user.getFigurePath().toString());

                            }else if(key.equals(User.LASR_SIGN_IN)){
                                // get the last sign in time of user.
                                try {
                                    User.curr_user.setLastSignIn(sdfmt2.parse(objectMap.get(key).toString()));
                                }catch (ParseException e){
                                    e.printStackTrace();
                                }
                            }else if(key.equals(User.FRIENDS)){
                                // get all the friends' ID and display_name and figure.
                                if(objectMap.get(key).equals("false")){
                                    ;
                                }else{
                                    HashMap<String, Object> friendList = (HashMap<String, Object>)
                                            objectMap.get(key);
                                    for(final String friendID:friendList.keySet()){
                                        database.getReference().child("users").child(friendID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot Snapshot) {
                                                HashMap<String, Object> dis_figure = (HashMap<String, Object>)
                                                        Snapshot.getValue();
                                                User.curr_user.addFriend(friendID, dis_figure.get(User.DISPALY_NAME).toString()
                                                        , dis_figure.get(User.EMAIL_ADDRESS).toString());
                                                User.curr_user.addFriendFigure(friendID, dis_figure.get(User.FIGURE_PATH).toString());
                                                // System.out.println("Success in add Friend: " + friendID + ", " + dataSnapshot.getValue().toString());
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                            }else if(key.equals(User.BILLS)){
                                // get all the bills this user involved.
                                if(objectMap.get(key).equals("false")){
                                    ;
                                }else {
                                    HashMap<String, Object> mapBills = (HashMap<String, Object>)
                                            objectMap.get(key);
                                    for (final String billID : mapBills.keySet()) {
                                        final Bill tempBill = new Bill(billID);
                                        database.getReference().child("bills").child(billID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                HashMap<String, Object> objectMap = (HashMap<String, Object>)
                                                        dataSnapshot.getValue();

                                                //---------------add the created time---------------
                                                String created_time = (String) objectMap.get(Bill.CREATED_TiME);
                                                tempBill.setCreatedTime(created_time);

                                                //---------------add the description----------------
                                                String description = (String)objectMap.get(Bill.DESCRIPTION);
                                                tempBill.addDescription(description);

                                                //---------------add the amount------------------
                                                // System.out.println("Long: " + objectMap.get(Bill.AMOUNT));
                                                String amount = (String)objectMap.get(Bill.AMOUNT);
                                                tempBill.setAmount(amount);

                                                //---------------add the loaner----------------------
                                                HashMap<String, Object> loanerMap =
                                                        (HashMap<String, Object>)objectMap.get(Bill.LOANER);
                                                for (String loanerID : loanerMap.keySet()) {
                                                    tempBill.setLoaners(loanerID);
                                                    tempBill.addBillDetails(loanerID, Double.parseDouble(loanerMap.get(loanerID).toString()));
                                                }

                                                //--------------add the borrower--------------------
                                                HashMap<String, Object> borrowerMap =
                                                        (HashMap<String, Object>)objectMap.get(Bill.BORROWER);
                                                for (String borrowerID : borrowerMap.keySet()) {
                                                    tempBill.addBillDetails(borrowerID, Double.parseDouble(borrowerMap.get(borrowerID).toString()));
                                                }

                                                System.out.println("Bill ID inside listener for tempBill: " + billID
                                                + "\n" + User.curr_user.billToString());
                                                User.curr_user.addBill(tempBill, tempBill.getBillID());
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                            }else if(key.equals(User.EVENTS)) {
                                // get all the bills this user involved.
                                if(objectMap.get(key).equals("false")){
                                    ;
                                }else {
                                    HashMap<String, Object> mapEvents = (HashMap<String, Object>)
                                            objectMap.get(key);
                                    for (final String eventID : mapEvents.keySet()) {
                                        final Event tempEvent = new Event(eventID);
                                        System.out.println("Bill ID for tempBill: " + eventID);
                                        database.getReference().child("events").child(eventID).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                HashMap<String, Object> objectMap = (HashMap<String, Object>)
                                                        dataSnapshot.getValue();

                                                //---------------add the created time---------------
                                                String created_time = (String) objectMap.get(Event.TIME);
                                                tempEvent.setTime(created_time);

                                                //---------------add the description----------------
                                                String description = (String)objectMap.get(Event.DESCRIPTION);
                                                tempEvent.setDescription(description);

                                                //---------------add the participants----------------------
                                                HashMap<String, Object> participantsMap =
                                                        (HashMap<String, Object>)objectMap.get(Event.PARTICIPANTS);
                                                for (final String participantID : participantsMap.keySet()) {
                                                    database.getReference().child("users").child(participantID).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            HashMap<String, Object> objectMap2 = (HashMap<String, Object>)
                                                                    dataSnapshot.getValue();
                                                            tempEvent.setParticipant(participantID, (String) objectMap2.get(User.DISPALY_NAME));

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                        }
                                                    });
                                                }

                                                //---------------add the bills in events----------------------
                                                if(objectMap.get(Event.BILLS).equals("false")){
                                                    ;
                                                }else{
                                                    HashMap<String, Object> billsMap =
                                                            (HashMap<String, Object>)objectMap.get(Event.BILLS);
                                                    for (final String billID:billsMap.keySet()){
                                                        database.getReference().child("bills").child(billID).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                HashMap<String, Object> objectMap2 = (HashMap<String, Object>)
                                                                        dataSnapshot.getValue();
                                                                Bill tempBill = new Bill(billID);

                                                                //---------------add the created time---------------
                                                                String created_time = (String) objectMap2.get(Bill.CREATED_TiME);
                                                                tempBill.setCreatedTime(created_time);

                                                                //---------------add the description----------------
                                                                String description = (String)objectMap2.get(Bill.DESCRIPTION);
                                                                tempBill.addDescription(description);

                                                                //---------------add the amount------------------
                                                                String amount = (String)objectMap2.get(Bill.AMOUNT);
                                                                tempBill.setAmount(amount);

                                                                //---------------add the loaner----------------------
                                                                HashMap<String, Object> loanerMap =
                                                                        (HashMap<String, Object>)objectMap2.get(Bill.LOANER);
                                                                for (String loanerID : loanerMap.keySet()) {
                                                                    tempBill.setLoaners(loanerID);
                                                                    tempBill.addBillDetails(loanerID, Double.parseDouble(loanerMap.get(loanerID).toString()));
                                                                }

                                                                //--------------add the borrower--------------------
                                                                HashMap<String, Object> borrowerMap =
                                                                        (HashMap<String, Object>)objectMap2.get(Bill.BORROWER);
                                                                for (String borrowerID : borrowerMap.keySet()) {
                                                                    tempBill.addBillDetails(borrowerID, Double.parseDouble(borrowerMap.get(borrowerID).toString()));
                                                                }

                                                                tempEvent.setBill(tempBill);
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {
                                                            }
                                                        });
                                                    }
                                                }


                                                User.curr_user.addEvent(tempEvent, tempEvent.getEventID());
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                }
                            }else{

                            }
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            TextView email = (TextView) navigationView.getHeaderView(0).findViewById(R.id.email_address_nav);
            System.out.println("email: " + email);
            email.setText(user.getEmail());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.basic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            startActivity(new Intent(BasicActivity.this, SettingActivity.class));
            finish();

        } else if (id == R.id.nav_gallery) {
            startActivity(new Intent(BasicActivity.this, ProfileActivity.class));
            finish();

        } else if (id == R.id.nav_slideshow) {
            startActivity(new Intent(BasicActivity.this, MainActivity.class));
            finish();

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {
            startActivity(new Intent(BasicActivity.this, FriendListActivity.class));
            // finish();

        } else if (id == R.id.nav_send) {
            signOut();
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //sign out method
    public void signOut() {
        auth.signOut();
        startActivity(new Intent(BasicActivity.this, LoginActivity.class));
    }


}
