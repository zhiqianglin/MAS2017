package com.example.qianlyu.payyay;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.w3c.dom.Text;

import android.net.Uri;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class FriendListActivity extends AppCompatActivity {

    private TableLayout friend_list_layout;

    private AlertDialog.Builder builder;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseUser user;
    private DatabaseReference m_ref;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private final static int HEIGHT_OF_FIGURE = 40;

    private GoogleApiClient client;

    private Drawable icon;
    private PopupWindow popUppercentages;
    // private Button imgButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();
        m_ref = database.getReference().child("users").child(user.getUid());

        FriendListActivity.this.setTitle("Friend List");

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

        // set the image button to represent friend and do delete on long press
        friend_list_layout = (TableLayout) findViewById(R.id.friend_list_table);
        if (User.curr_user.getFriendNumber() == 0) {
            TextView mText = new TextView(FriendListActivity.this);
            mText.setText("You have no friend in list");
            mText.setTextSize(20);
            ((TableLayout) friend_list_layout).addView(mText);
        } else {
            System.out.println("Friend List Activity start-----------------");
            for (final String friendID : User.curr_user.getAllFriendIDbyName().keySet()) {
                Uri uri = Uri.parse(User.curr_user.getFriendFigureURI(friendID));
                Picasso.with(this)
                        .load(uri.toString())
                        .into(new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                Button imgButton = new Button(FriendListActivity.this);
                                imgButton.setText(User.curr_user.getFriendByID(friendID));

                                TableLayout.LayoutParams params = new TableLayout.LayoutParams(
                                        TableLayout.LayoutParams.WRAP_CONTENT,
                                        TableLayout.LayoutParams.WRAP_CONTENT
                                );
                                params.setMargins(0, 16, 0, 16);

                                // get new drawable.
                                imgButton.setBackgroundColor(Color.parseColor("#F5DEB3"));
                                imgButton.setLayoutParams(params);

                                icon = new BitmapDrawable(getResources(),
                                        Bitmap.createScaledBitmap(bitmap, User.convertDpToPx(30), User.convertDpToPx(30), true));
                                imgButton.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                                imgButton.setPadding(User.convertDpToPx(30), 0, 0, 0);
                                imgButton.setOnLongClickListener(new View.OnLongClickListener(){
                                    @Override
                                    public boolean onLongClick(View v) {
                                        builder=new AlertDialog.Builder(FriendListActivity.this);
                                        builder.setIcon(R.mipmap.ic_launcher);
                                        //builder.setTitle(R.string.simple_list_dialog);

                                        /**
                                         * 设置内容区域为简单列表项
                                         */
                                        final String[] Items={"Delete"};
                                        builder.setItems(Items, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Toast.makeText(getApplicationContext(), "You clicked "+Items[i], Toast.LENGTH_SHORT).show();
                                                if(i == 0){
                                                    database.getReference().child("users").child(User.curr_user.getUserID())
                                                            .child(User.FRIENDS).child(friendID).removeValue();
                                                    startActivity(new Intent(FriendListActivity.this, MainActivity.class));
                                                }
                                            }
                                        });
                                        builder.setCancelable(true);
                                        AlertDialog dialog=builder.create();
                                        dialog.show();
                                        return true;
//                                        LinearLayout containerLayout = new LinearLayout(FriendListActivity.this);
//                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
//                                                (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                                        containerLayout.setOrientation(LinearLayout.VERTICAL);
//
//                                        Button deleteButton = new Button(FriendListActivity.this);
//                                        deleteButton.setText("Delete");
//                                        deleteButton.setOnClickListener(new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View v) {
//                                                database.getReference().child("users").child(User.curr_user.getUserID())
//                                                        .child(User.FRIENDS).child(friendID).removeValue();
//                                                recreateActivityCompat(FriendListActivity.this);
//                                                popUppercentages.dismiss();
//                                            }
//                                        });
//                                        containerLayout.addView(deleteButton);
//
//                                        popUppercentages = new PopupWindow(FriendListActivity.this);
//                                        popUppercentages.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
//
//                                        popUppercentages.setContentView(containerLayout);
//
//                                        popUppercentages.setOutsideTouchable(true);
//                                        popUppercentages.setFocusable(true);
//
//                                        System.out.println(popUppercentages.getHeight() + "\n width: " + popUppercentages.getWidth());
//                                        popUppercentages.showAtLocation(new LinearLayout(FriendListActivity.this), Gravity.CENTER, 0, 0);
//                                        return false;
                                    }
                                });

                                ((TableLayout) friend_list_layout).addView(imgButton);


                            }
                            @Override
                            public void onBitmapFailed(Drawable errorDrawable) {
                            }
                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                            }
                        });

            }
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friend_list_menu, menu);

        return (super.onCreateOptionsMenu(menu));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_friends:
                Intent intent = new Intent(FriendListActivity.this, AddFriendActivity.class);
                this.startActivity(intent);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        String m_uri;
        Drawable m_image;

        public MyAsyncTask(String uri){
            m_uri = uri;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            doInBackground();   //your methods
            return null;
        }

        protected void doInBackground() {
            String urldisplay = m_uri;
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            m_image = new  BitmapDrawable(getResources(), mIcon11);
        }

        public Drawable getImage(){
            return m_image;
        }
    }

    public Drawable drawableFromUrl(String url) throws IOException {
        String urldisplay = url;
        Bitmap mIcon11 = null;

        try {
            InputStream in = new URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            // Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
        return new BitmapDrawable(getResources(), mIcon11);
    }

    public static void recreateActivityCompat(final Activity a) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            a.recreate();
        } else {
            final Intent intent = a.getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            a.finish();
            a.overridePendingTransition(0, 0);
            a.startActivity(intent);
            a.overridePendingTransition(0, 0);
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("FriendList Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
