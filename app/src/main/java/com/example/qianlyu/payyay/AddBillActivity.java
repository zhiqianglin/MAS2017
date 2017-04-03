package com.example.qianlyu.payyay;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.DateFormat;
import android.icu.text.DecimalFormat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ex.chips.BaseRecipientAdapter;
import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.doodle.android.chips.ChipsView;
import com.doodle.android.chips.model.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AddBillActivity extends AppCompatActivity {

    private Button btnSplitMethod, btnSaveBill;
    private ImageButton btnSearchFriend, btnCurrentType;

    private PopupWindow popUppercentages;

    private RecipientEditTextView phoneRetv;
    private EditText btnBillTotalAmount, etDescription;

    private LinearLayout add_bill_layout;
    private AlertDialog.Builder builder;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private String groupID;
    private String friendID;
    private Set<String> bill_shared_persons = new HashSet<>();
    private HashMap<String, Double> bill_shared_by_unequally = new HashMap<>();
    private HashMap<String, Double> bill_shared_by_percentage = new HashMap<>();
    private HashMap<String, Double> bill_shared_by_shares = new HashMap<>();
    private HashMap<String, Double> bill_shared_by_adjustment = new HashMap<>();
    // the final bill separate amount.
    private HashMap<String, Double> bill_separate = new HashMap<>();

    private HashMap<String, Integer> onePairUserIDToTextViewID = new HashMap<>();
    private HashMap<String, Integer> onePairUserIDToEditViewID = new HashMap<>();

    private boolean checkForFriends = true;
    private double current_ratio = 1;

    private static int SPLIT_EQUALLY = 0;
    private static int SPLIT_UNEQUALLY = 1;
    private static int SPLIT_PERCENTAGE = 2;
    private static int SPLIT_SHARES = 3;
    private static int SPLIT_ADJUSTMENT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // check the user.
        System.out.println("-----------------Add Bill Activity Start--------------");
        // System.out.println(User.curr_user.toString());

        AddBillActivity.this.setTitle("Add Bill");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white_36dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(AddBillActivity.this, MainActivity.class));
                finish();
            }
        });

        phoneRetv = (RecipientEditTextView) findViewById(R.id.search_for_friend);
        phoneRetv.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        BaseRecipientAdapter adapter = new BaseRecipientAdapter(BaseRecipientAdapter.QUERY_TYPE_PHONE, this);
        adapter.setShowMobileOnly(true);
        phoneRetv.setAdapter(adapter);
        phoneRetv.dismissDropDownOnItemSelected(true);

        final ImageButton showAll = (ImageButton) findViewById(R.id.search_for_friend_button);
        showAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneRetv.showAllContacts();
            }
        });

        etDescription = (EditText) findViewById(R.id.description);

        btnCurrentType = (ImageButton) findViewById(R.id.current_type);
        btnCurrentType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder=new AlertDialog.Builder(AddBillActivity.this);
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setTitle("Choose current type");

                final LinearLayout loginDialog= (LinearLayout) new LinearLayout(AddBillActivity.this);
                loginDialog.setOrientation(LinearLayout.VERTICAL);
                for(int i = 0; i < Bill.current_type.length; i ++) {
                    Button img = new Button(AddBillActivity.this);
                    img.setText(Bill.current_type[i]);
                    loginDialog.addView(img);
                    final int index = i;
                    img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            current_ratio = Bill.current_ratio[index];
                            finish();
                        }
                    });
                }
                builder.setView(loginDialog);

                builder.setCancelable(true);
                final AlertDialog dialog=builder.create();
                dialog.show();
            }
        });

        btnBillTotalAmount = (EditText) findViewById(R.id.bill_amount);

        btnSplitMethod = (Button) findViewById(R.id.split_method);
        btnSplitMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Split Method", "clicked");
                //Creating the instance of PopupMenu
                final PopupMenu popup = new PopupMenu(AddBillActivity.this, btnSplitMethod);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        btnSplitMethod.setText(item.getTitle());
                        Toast.makeText(AddBillActivity.this, "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
                        getAllInvolvedPerson();
                        if(item.getTitle().equals("Split equally")){
                            return true;
                        }
                        if(item.getTitle().equals("Split by percentages")){
                            builder=new AlertDialog.Builder(AddBillActivity.this);
                            builder.setIcon(R.mipmap.ic_launcher);
                            builder.setTitle(item.getTitle());

                            /**
                             * 设置内容区域为自定义View
                             */
                            final LinearLayout loginDialog= (LinearLayout) new LinearLayout(AddBillActivity.this);
                            loginDialog.setOrientation(LinearLayout.VERTICAL);
                            int count = 0;
                            for(String userID:bill_shared_persons){
                                addOnePairInput(userID, loginDialog, "10%");
                                System.out.println("userID in split percentage: " + userID);
                                count ++;
                            }
                            Button checkButton = new Button(AddBillActivity.this);
                            checkButton.setText("check");
                            loginDialog.addView(checkButton);

                            builder.setView(loginDialog);

                            builder.setCancelable(true);
                            final AlertDialog dialog=builder.create();
                            checkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    double sum = 0;
                                    for(String userID:bill_shared_persons){
                                        EditText editTemp = (EditText) loginDialog.findViewById(onePairUserIDToEditViewID.get(userID));
                                        double tempPercent = Double.parseDouble(editTemp.getText().toString());
                                        sum += tempPercent;
                                        TextView textTemp = (TextView) loginDialog.findViewById(onePairUserIDToTextViewID.get(userID));;
                                        String userName = textTemp.getText().toString();
                                        if(userName.equals(User.curr_user.getPreferredName()))
                                            bill_shared_by_percentage.put(User.curr_user.getUserID(), tempPercent);
                                        else
                                            bill_shared_by_percentage.put(User.curr_user.getFriendByName(userName), tempPercent);
                                    }
                                    if(sum != 100){
                                        Toast.makeText(AddBillActivity.this, "The percentages added up should be equal to 100!", Toast.LENGTH_SHORT).show();
                                    }else{
                                        dialog.dismiss();
                                    }
                                }
                            });
                            dialog.show();
                            return true;


                        }else if(item.getTitle().equals("Split unequally")){
                            builder=new AlertDialog.Builder(AddBillActivity.this);
                            builder.setIcon(R.mipmap.ic_launcher);
                            builder.setTitle(item.getTitle());

                            /**
                             * 设置内容区域为自定义View
                             */
                            final LinearLayout loginDialog= (LinearLayout) new LinearLayout(AddBillActivity.this);
                            loginDialog.setOrientation(LinearLayout.VERTICAL);
                            int count = 0;
                            for(final String userID:bill_shared_persons){
                                addOnePairInput(userID, loginDialog, "10.00");
                                System.out.println("userID in split percentage: " + userID);
                                count ++;
                                if(count == bill_shared_persons.size()-1){
                                    final EditText edit = (EditText) findViewById(onePairUserIDToEditViewID.get(userID));
                                    edit.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            double remain = Double.parseDouble(btnBillTotalAmount.getText().toString());
                                            for(String tempID:bill_shared_persons){
                                                if(!tempID.equals(userID)) {
                                                    EditText edit = (EditText) findViewById(onePairUserIDToEditViewID.get(tempID));
                                                    remain -= Double.parseDouble(edit.getText().toString());
                                                }
                                            }
                                            edit.setText(String.valueOf(remain));
                                        }
                                    });
                                }
                            }

                            Button checkButton = new Button(AddBillActivity.this);
                            checkButton.setText("check");
                            loginDialog.addView(checkButton);

                            builder.setView(loginDialog);

                            builder.setCancelable(true);
                            final AlertDialog dialog=builder.create();
                            checkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    double sum = 0;
                                    for(String userID:bill_shared_persons){
                                        EditText editTemp = (EditText) loginDialog.findViewById(onePairUserIDToEditViewID.get(userID));
                                        double tempPercent = Double.parseDouble(editTemp.getText().toString());
                                        sum += tempPercent;
                                        TextView textTemp = (TextView) loginDialog.findViewById(onePairUserIDToTextViewID.get(userID));
                                        String userName = textTemp.getText().toString();
                                        if(userName.equals(User.curr_user.getPreferredName()))
                                            bill_shared_by_unequally.put(User.curr_user.getUserID(), tempPercent);
                                        else
                                            bill_shared_by_unequally.put(User.curr_user.getFriendByName(userName), tempPercent);
                                    }
                                    if(sum != Double.parseDouble(btnBillTotalAmount.getText().toString())){
                                        Toast.makeText(AddBillActivity.this, "The total amount added up should be equal to " + btnBillTotalAmount.getText().toString(), Toast.LENGTH_SHORT).show();
                                    }else{
                                        dialog.dismiss();
                                    }
                                }
                            });
                            dialog.show();

                            return true;


                        }else if(item.getTitle().equals("Split by shares")){
                            builder=new AlertDialog.Builder(AddBillActivity.this);
                            builder.setIcon(R.mipmap.ic_launcher);
                            builder.setTitle(item.getTitle());

                            /**
                             * 设置内容区域为自定义View
                             */
                            final LinearLayout loginDialog= (LinearLayout) new LinearLayout(AddBillActivity.this);
                            loginDialog.setOrientation(LinearLayout.VERTICAL);
                            int count = 0;
                            for(String userID:bill_shared_persons){
                                addOnePairInput(userID, loginDialog, "3");
                                System.out.println("userID in split percentage: " + userID);
                                count ++;
                            }

                            Button checkButton = new Button(AddBillActivity.this);
                            checkButton.setText("check");
                            loginDialog.addView(checkButton);

                            builder.setView(loginDialog);

                            builder.setCancelable(true);
                            final AlertDialog dialog=builder.create();
                            checkButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    boolean checkForZero = false;
                                    for(String userID:bill_shared_persons){
                                        EditText editTemp = (EditText) loginDialog.findViewById(onePairUserIDToEditViewID.get(userID));
                                        double tempPercent = Double.parseDouble(editTemp.getText().toString());
                                        if(tempPercent <= 0) checkForZero = true;
                                        TextView textTemp = (TextView) loginDialog.findViewById(onePairUserIDToTextViewID.get(userID));;
                                        String userName = textTemp.getText().toString();
                                        if(userName.equals(User.curr_user.getPreferredName()))
                                            bill_shared_by_shares.put(User.curr_user.getUserID(), tempPercent);
                                        else
                                            bill_shared_by_shares.put(User.curr_user.getFriendByName(userName), tempPercent);
                                    }
                                    if(checkForZero){
                                        Toast.makeText(AddBillActivity.this, "All the shares should be positive integers", Toast.LENGTH_SHORT).show();
                                    }else{
                                        dialog.dismiss();
                                    }
                                }
                            });
                            dialog.show();

                            return true;

                       }
                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
        });

        btnSaveBill = (Button) findViewById(R.id.save_bill);
        btnSaveBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String splitMethod = btnSplitMethod.getText().toString();
                final String description = etDescription.getText().toString();


                if(description == null){
                    Toast.makeText(AddBillActivity.this, "Please input description.", Toast.LENGTH_SHORT).show();
                }else {
                    if(!checkForFriends){
                        if(phoneRetv.getRecipients() == null || phoneRetv.getRecipients().length == 0)
                            Toast.makeText(AddBillActivity.this, "please enter the display name of your friend", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(AddBillActivity.this, "Please add input person as friend First!", Toast.LENGTH_SHORT).show();
                    }else {
                        double totalBillAmount = Double.parseDouble(btnBillTotalAmount.getText().toString());
                        String billID = UUID.randomUUID().toString();
                        Bill newBill = new Bill(billID);
                        splitBillByMethod();
                        System.out.println(bill_separate.size());
                        newBill.setLoaners(user.getUid());
                        for(String userID:bill_shared_persons) {
                            newBill.addBillDetails(userID, bill_separate.get(userID));
                        }
                        HashMap<String, Double> map = newBill.getBillDetails();

                        for(String userID:bill_shared_persons) {
                            database.getReference().child("users").child(userID).child(User.BILLS).child(billID).setValue("true");
                            if(!userID.equals(user.getUid()))
                                database.getReference().child("bills").child(billID).child(Bill.BORROWER).child(userID).setValue(map.get(userID));
                        }

                        database.getReference().child("bills").child(billID).child(Bill.CREATED_TiME).setValue(MainActivity.df.format(new Date()));
                        database.getReference().child("bills").child(billID).child(Bill.REMAINING).setValue(bill_shared_persons.size());
                        database.getReference().child("bills").child(billID).child(Bill.LOANER).child(user.getUid()).setValue(map.get(user.getUid()));
                        database.getReference().child("bills").child(billID).child(Bill.DESCRIPTION).setValue(description);

                        Log.d("btnSaveBill", "start Save Bill.");
                        startActivity(new Intent(AddBillActivity.this, MainActivity.class));
                        finish();
                    }
                }
            }
        });
    }

    private void splitBillByMethod(){
        String split_method = btnSplitMethod.getText().toString();
        int split_method_int = -1;

        if(bill_shared_persons.size() == 0){
            Toast.makeText(AddBillActivity.this, "You need to input persons shared this bill.", Toast.LENGTH_SHORT).show();
        }else{
            bill_shared_persons.add(User.curr_user.getUserID());
        }

        switch (split_method) {
            case "Split equally":
                split_method_int = AddBillActivity.SPLIT_EQUALLY;
                int numbers_of_user = bill_shared_persons.size();
                double amount = Double.parseDouble(btnBillTotalAmount.getText().toString());
                System.out.println("number of user: " + numbers_of_user
                        + "\n" + "total Amount: " + btnBillTotalAmount.getText().toString());
                for(String friendID:bill_shared_persons) {
                    System.out.println("friend Name: " + friendID);
                    System.out.println("basic activity curr_user: " + User.curr_user + "\n" + "friendName: " + friendID);
                    if(friendID.equals(User.curr_user.getUserID())){
                        bill_separate.put(friendID, current_ratio*amount/numbers_of_user*(numbers_of_user-1));
                    }else{
                        bill_separate.put(friendID, -current_ratio*amount/numbers_of_user);
                    }
                }
                break;
            case "Split unequally":
                split_method_int = AddBillActivity.SPLIT_UNEQUALLY;
                double amount2 = Double.parseDouble(btnBillTotalAmount.getText().toString());
                double sum2 = 0;
                for(String userID:bill_shared_persons) {
                    if(!userID.equals(User.curr_user.getUserID())) {
                        bill_separate.put(userID, -current_ratio*bill_shared_by_unequally.get(userID));
                        sum2 += current_ratio*bill_shared_by_unequally.get(userID);
                    }
                    // System.out.println("split by percentage: " + userID + ", " + amount2 * bill_shared_by_percentage.get(userID) / 100.0);
                }
                bill_separate.put(User.curr_user.getUserID(), sum2);
                break;
            case "Split by percentages":
                split_method_int = AddBillActivity.SPLIT_PERCENTAGE;
                double amount1 = Double.parseDouble(btnBillTotalAmount.getText().toString());
                double sum = 0;
                for(String userID:bill_shared_persons) {
                    if(!userID.equals(User.curr_user.getUserID())) {
                        bill_separate.put(userID, -current_ratio*amount1 * bill_shared_by_percentage.get(userID) / 100.0);
                        sum += current_ratio*amount1 * bill_shared_by_percentage.get(userID) / 100.0;
                    }
                    System.out.println("split by percentage: " + userID + ", " + amount1 * bill_shared_by_percentage.get(userID) / 100.0);
                }
                System.out.println("split by percentage: " + User.curr_user.getUserID() + ", " + sum);
                bill_separate.put(User.curr_user.getUserID(), sum);

                break;
            case "Split by shares":
                split_method_int = AddBillActivity.SPLIT_SHARES;
                double amount3 = Double.parseDouble(btnBillTotalAmount.getText().toString());
                double sum3 = 0;
                for(String userID:bill_shared_persons) {
                        sum3 += current_ratio*bill_shared_by_shares.get(userID);
                }
                for(String userID:bill_shared_persons) {
                    if(!userID.equals(User.curr_user.getUserID())) {
                        bill_separate.put(userID, -current_ratio*amount3*bill_shared_by_shares.get(userID)/sum3);
                    }
                }
                bill_separate.put(User.curr_user.getUserID(), current_ratio*amount3-current_ratio*amount3*bill_shared_by_shares.get(User.curr_user.getUserID())/sum3);

                break;
            case "Split by adjustment":
                split_method_int = AddBillActivity.SPLIT_ADJUSTMENT;
                break;
            default:
                Log.d("Error", "can't recognize this split method.");
                break;
        }
    }

    public void addOnePairInput(String userID, LinearLayout containerLayout, String showedHint){
        TextView tempView = new TextView(AddBillActivity.this);
        // System.out.println(User.curr_user.toString());
        String disName = "";
        if(userID.equals(User.curr_user.getUserID())) {
            // System.out.println("userID in one pair: " + userID);
            disName = User.curr_user.getPreferredName();
        }
        else
            disName = User.curr_user.getFriendByID(userID);
        tempView.setText(disName);
        onePairUserIDToTextViewID.put(userID, View.generateViewId());
        tempView.setId(onePairUserIDToTextViewID.get(userID));
        EditText tempButton = new EditText(AddBillActivity.this);
        tempButton.setHint(showedHint);
        tempButton.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        onePairUserIDToEditViewID.put(userID, View.generateViewId());
        tempButton.setId(onePairUserIDToEditViewID.get(userID));
        containerLayout.addView(tempView);
        containerLayout.addView(tempButton);
        System.out.println("tempView id: " + tempView.getId());
    }

    private boolean getAllInvolvedPerson(){
        System.out.println("get all involved person start-----------");
        DrawableRecipientChip[] chips = phoneRetv.getSortedRecipients();
        if(chips == null || chips.length == 0){

            checkForFriends = false;
            return false;
        }else{
            for(DrawableRecipientChip chip:chips){
                String friendName = chip.getEntry().getDisplayName();
                if (User.curr_user.getFriendByName(friendName) == null) {
                    checkForFriends = false;
                    return false;
                }
            }
        }
        checkForFriends = true;
        for(DrawableRecipientChip chip:chips){
            bill_shared_persons.add(User.curr_user.getFriendByName(chip.getEntry().getDisplayName()));
            System.out.println("item: " + chip.getEntry().getDisplayName());

        }
        bill_shared_persons.add(user.getUid());
        System.out.println("get all involved person end--------------");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    public boolean onOptionsItemSelected(MenuItem item){
//        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
//        startActivityForResult(myIntent, 0);
//        return true;
//
//    }
}

