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
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FirstFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    private HashMap<String, Event> map = new HashMap<>();


    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_first_fragment, container, false);

        List<Event> events = User.curr_user.getAllEvents();
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.activity_first_fragment);

        System.out.println("Event size: " + events.size());
        for(final Event event:events) {
            List<String> participants = event.getAllParticipants();
            Button button = setEventButton(event.getEventID(), participants);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), EventActivity.class);
                    intent.putExtra("eventID", event.getEventID());
                    startActivity(intent);
                }
            });

            linearLayout.addView(button);
        }
        return view;
    }

    public Button setEventButton(String description, List<String> participants){
        Button button = new Button(getActivity());
        String text = description + "\n";
        for(String name:participants)
            text += name + ", ";
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
