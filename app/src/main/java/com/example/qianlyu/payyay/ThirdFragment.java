package com.example.qianlyu.payyay;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.echo.holographlibrary.BarGraph.OnBarClickedListener;
import com.echo.holographlibrary.BarStackSegment;

public class ThirdFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_third_fragment, container, false);

        HashMap<String, Double> balance = User.curr_user.calculateBalance();
        ArrayList<Bar> pointsNeg = new ArrayList<Bar>();
        ArrayList<Bar> pointsPos = new ArrayList<Bar>();
        for(String name:balance.keySet()) {
            Bar d = new Bar();
            d.setName(name);
            d.setValue((float)(double)balance.get(name));
            if(balance.get(name) < 0) {
                d.setColor(Color.parseColor("#99CC00"));
                pointsNeg.add(d);
            }
            else {
                d.setColor(Color.parseColor("#FF0000"));
                pointsPos.add(d);
            }
        }

        BarGraph g = (BarGraph) view.findViewById(R.id.graph);
        g.setBars(pointsNeg);
        g.setUnit("$");
        BarGraph gPos = (BarGraph) view.findViewById(R.id.posgraph);
        gPos.setBars(pointsPos);
        gPos.setUnit("$");

        for(String name:balance.keySet()){
            TextView text = new TextView(getActivity());
            String content = "";
            if(balance.get(name) < 0){
                content += name + "owe you \n" + balance.get(name);
            }else{
                content += "You owe " + name + "\n" + balance.get(name);
            }
            text.setText(content);
            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.third_fragment_inner);
            linearLayout.addView(text);
        }

        return view;
    }
}
