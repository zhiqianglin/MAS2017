package com.example.qianlyu.payyay;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SecondFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;

    private List<Bill> curr_bills;

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_second_fragment, container, false);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.activity_second_fragment);
        curr_bills = User.curr_user.getAllBills();
        System.out.println("Second fragment start! " + curr_bills.size());

        for(final Bill bill:curr_bills) {
            String description = bill.getDescription();
            String time = bill.getCreatedTime();
            Button button = setEventButton(bill.getBillID(), time);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ViewBillActivity.class);
                    intent.putExtra("eventID", "null");
                    intent.putExtra("billID", bill.getBillID());
                    startActivity(intent);
                }
            });

            linearLayout.addView(button);
        }

        return view;
    }

    public Button setEventButton(String description, String time){
        Button button = new Button(getActivity());
        String text = description + "\n";
        text += time;
        System.out.println("text: " + text);
        button.setText(text.substring(0, text.length()-1));
        button.setBackgroundColor(Color.parseColor("#FFFFFF"));
        button.setAllCaps(false);
        button.setGravity(Gravity.LEFT);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(30, 6, 30, 6);

        return button;
    }
}
