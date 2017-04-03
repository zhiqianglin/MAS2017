package com.example.qianlyu.payyay;

import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewBillActivity extends AppCompatActivity {

    private AlertDialog.Builder builder;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseUser user;
    private DatabaseReference m_ref;

    private GoogleApiClient client;

    private Bill curr_bill;
    private Event curr_event;

    private EditText input_title, input_amount, input_date;
    private Button btn_advanced;
    private Spinner spinner;
    private TableLayout tableLayout;
    private HashMap<String, List<Integer>> recorded_tablerow_ids = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bill);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();
        m_ref = database.getReference().child("users").child(user.getUid());

        String billID = getIntent().getStringExtra("billID");
        curr_bill = User.curr_user.getOneBill(billID);
        curr_event = User.curr_user.getOneEvent(getIntent().getStringExtra("eventID"));



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

        input_title = (EditText) findViewById(R.id.description);
        input_amount = (EditText) findViewById(R.id.amount);
        input_date = (EditText) findViewById(R.id.date);
        btn_advanced = (Button) findViewById(R.id.advanced);
        btn_advanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_advanced.getText().equals("Advanced")){
                    btn_advanced.setText("Simplified");
                    for(String participant:recorded_tablerow_ids.keySet()){
                        EditText edit = (EditText) findViewById(recorded_tablerow_ids.get(participant).get(1));
                        edit.setVisibility(View.VISIBLE);
                    }
                }else{
                    btn_advanced.setText("Advanced");
                    for(String participant:recorded_tablerow_ids.keySet()){
                        EditText edit = (EditText) findViewById(recorded_tablerow_ids.get(participant).get(1));
                        edit.setVisibility(View.GONE);
                    }
                }
            }
        });

        if(curr_bill != null){
            ViewBillActivity.this.setTitle(curr_bill.getDescription());
            input_title.setText(curr_bill.getDescription());
            input_amount.setText(curr_bill.getAmount());
            input_date.setText(curr_bill.getCreatedTime());
        }

        List<String> participants;
        participants = curr_event.getAllParticipants();
        System.out.println("participant view bill: " + participants.get(0));
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new ItemSelectedListener());
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, participants);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);

        tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        for (String participant:participants) {
            List<Integer> tablerow_ids = new ArrayList<>();
            /* Create a new row to be added. */
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            /* Create a Button to be the row-content. */
            CheckBox b = new CheckBox(this);
            b.setId(View.generateViewId()); tablerow_ids.add(b.getId());
            b.setText(participant);
            b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(b);

            EditText editText1 = new EditText(this);
            editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText1.setVisibility(View.GONE);
            editText1.setText("1");
            editText1.setId(View.generateViewId()); tablerow_ids.add(editText1.getId());
            tr.addView(editText1);

            EditText editText2 = new EditText(this);
            editText2.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText2.setText(String.valueOf(Double.parseDouble(input_amount.getText().toString()) / participants.size()));
            editText2.setId(View.generateViewId()); tablerow_ids.add(editText2.getId());
            tr.addView(editText2);
            tableLayout.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            recorded_tablerow_ids.put(participant, tablerow_ids);
        }
    }

    public class ItemSelectedListener implements AdapterView.OnItemSelectedListener {

        //get strings of first item
        String firstItem = String.valueOf(spinner.getSelectedItem());

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (firstItem.equals(String.valueOf(spinner.getSelectedItem()))) {
                // ToDo when first item is selected
            } else {
                Toast.makeText(parent.getContext(),
                        "You have selected : " + parent.getItemAtPosition(pos).toString(),
                        Toast.LENGTH_LONG).show();
                // Todo when item is selected by the user
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg) {
        }

    }
}
