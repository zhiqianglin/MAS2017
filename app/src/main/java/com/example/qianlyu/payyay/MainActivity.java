package com.example.qianlyu.payyay;

import android.app.ActionBar;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BasicActivity
        implements TabLayout.OnTabSelectedListener{

    private Button no;

    private TableLayout main_bill_table;
    private LinearLayout eventLayout, billLayout, balanceLayout;
    private FloatingActionButton btn_add_float;

    private Pager pagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    private Button btn_retrieve;

    // format of Date data
    public static DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

    private AlertDialog.Builder builder;

    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.pager);

        //Adding the tabs using addTab() method
        tabLayout.addTab(tabLayout.newTab().setText("Event"));
        tabLayout.addTab(tabLayout.newTab().setText("Bill"));
        tabLayout.addTab(tabLayout.newTab().setText("Balance"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        pagerAdapter = new Pager(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setOnTabSelectedListener(this);
        System.out.println("background: " + tabLayout.getBackground());

        // get firebase auth instance
        auth = FirebaseAuth.getInstance();
        // System.out.println("auth: " + auth.toString());
        database = FirebaseDatabase.getInstance();
        // System.out.println("database: " + database.getReference().toString());
        storage = FirebaseStorage.getInstance();
        // System.out.println("storage: " + storage.toString());


        // get current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        // System.out.println("UserID: " + user.getUid() + " check Null: " + mref.child("users").child(user.getUid())==null);
        final DatabaseReference mref = database.getReference().child("users").child(user.getUid());
        mref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue() == null) {
                    //user initilization for fist time login
                    mref.child(User.EMAIL_ADDRESS).setValue(user.getEmail());
                    mref.child(User.CREATE_TIME).setValue(df.format(new Date()));
                    mref.child(User.DISPALY_NAME).setValue(user.getEmail().split("@")[0]);
                    StorageReference m_figure_one = storage.getReference(User.defaultFigure);
                    m_figure_one.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mref.child(User.FIGURE_PATH).setValue(uri.toString());
                        }
                    });
                    mref.child(User.GROUPS).setValue("false");
                    mref.child(User.FRIENDS).setValue("false");
                    mref.child(User.BILLS).setValue("false");
                    String email_Adjust = user.getEmail().replace(".", ",");
                    System.out.println("emailAddress: " + email_Adjust);
                    database.getReference().child("users_email").child(email_Adjust).setValue(user.getUid());
                }else{
                    mref.child(User.LASR_SIGN_IN).setValue(df.format(new Date()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        set();


        // get auth Listener down to detect user's situation
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        btn_add_float = (FloatingActionButton) findViewById(R.id.fab);

        btn_add_float.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent for the activity to open when user selects the notification
                if(tabLayout.getSelectedTabPosition() == 1) {
                    Intent addbillIntent = new Intent(MainActivity.this, AddBillActivity.class);
                    startActivity(addbillIntent);
                    finish();
                }else if(tabLayout.getSelectedTabPosition() == 0){
                    Intent addbillIntent = new Intent(MainActivity.this, AddEventActivity.class);
                    startActivity(addbillIntent);
                    finish();
                }
            }
        });


        mHandler = new Handler();
        mHandler.postDelayed(m_Runnable,5000);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
        if(tab.getPosition() == 2){
            btn_add_float.setVisibility(View.GONE);
        }else{
            btn_add_float.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    //Extending FragmentStatePagerAdapter
    public class Pager extends FragmentStatePagerAdapter {

        //integer to count number of tabs
        int tabCount;

        //Constructor to the class
        public Pager(FragmentManager fm, int tabCount) {
            super(fm);
            //Initializing tab count
            this.tabCount= tabCount;
        }

        //Overriding method getItem
        @Override
        public Fragment getItem(int position) {
            //Returning the current tabs
            switch (position) {
                case 0:
                    FirstFragment tab1 = new FirstFragment();
                    return tab1;
                case 1:
                    SecondFragment tab2 = new SecondFragment();
                    return tab2;
                case 2:
                    ThirdFragment tab3 = new ThirdFragment();
                    return tab3;
                default:
                    return null;
            }
        }

        //Overriden method getCount to get the number of tabs
        @Override
        public int getCount() {
            return tabCount;
        }
    }

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                // another startActivity, this is for item with id "menu_item2"
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            Toast.makeText(MainActivity.this,"in runnable",Toast.LENGTH_SHORT).show();
            MainActivity.this.mHandler.postDelayed(m_Runnable, 5000);
        }

    };//runnable
}
