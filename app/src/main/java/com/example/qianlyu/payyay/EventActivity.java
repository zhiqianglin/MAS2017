package com.example.qianlyu.payyay;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static com.example.qianlyu.payyay.R.id.tabLayout;

public class EventActivity extends AppCompatActivity {

    private AlertDialog.Builder builder;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseUser user;
    private DatabaseReference m_ref;

    private GoogleApiClient client;

    private Event curr_event;
    private FloatingActionButton btn_add_float;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();
        m_ref = database.getReference().child("users").child(user.getUid());

        String eventID = getIntent().getStringExtra("eventID");
        curr_event = User.curr_user.getOneEvent(eventID);

        EventActivity.this.setTitle(curr_event.getDescription());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_36dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // startActivity(new Intent(FriendListActivity.this, MainActivity.class));
                finish();
            }
        });

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.event_inner);

        List<Bill> allBills = curr_event.getAllBills();
        System.out.println("event bills: " + allBills);
        for(final Bill bill:allBills){
            LinearLayout linear = new LinearLayout(EventActivity.this);
            linear.setPadding(30,10,30,10);
            linear.setOrientation(LinearLayout.VERTICAL);
            linear.setBackgroundColor(Color.parseColor("#FFFFFF"));
            TextView textview1 = new TextView(EventActivity.this);
            textview1.setText(bill.getDescription());
            textview1.setTextSize(30);
            textview1.setTextColor(Color.parseColor("#000000"));
            TextView textview2 = new TextView(EventActivity.this);
            textview2.setText("Paid by " + curr_event.getParticipantNamebyID(bill.getLoaner()));
            textview2.setTextSize(20);
            textview2.setTextColor(Color.parseColor("#000000"));
            linear.addView(textview1); linear.addView(textview2);
            linear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EventActivity.this, ViewBillActivity.class);
                    intent.putExtra("billID", bill.getBillID());
                    intent.putExtra("eventID", curr_event.getEventID());
                    startActivity(intent);
                }
            });
            System.out.println("description: " + bill.getDescription() + " time: " + bill.getCreatedTime());
            linearLayout.addView(linear);
        }

        btn_add_float = (FloatingActionButton) findViewById(R.id.new_bill);

        btn_add_float.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent for the activity to open when user selects the notification
                Intent addbillIntent = new Intent(EventActivity.this, ViewBillActivity.class);
                addbillIntent.putExtra("billID", "null");
                addbillIntent.putExtra("eventID", curr_event.getEventID());
                startActivity(addbillIntent);
                finish();
            }
        });
    }

    public Button setEventButton(String description, String time){
        Button button = new Button(EventActivity.this);
        String text = description + "\n";
        text += time;
        button.setText(text.substring(0, text.length()-1));
        button.setBackgroundColor(Color.parseColor("#FFFFFF"));
        button.setAllCaps(false);
        button.setGravity(Gravity.LEFT);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(30, 6, 30, 6);

        return button;
    }
}
