package com.example.qianlyu.payyay;

import com.example.qianlyu.payyay.Bill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Qian Lyu on 4/1/2017.
 */

public class Event {
    private String m_eventID;
    private String m_description;
    private String m_time;

    private List<Bill> m_bills = new ArrayList<>();
    private HashMap<String, String> m_participantsIDtoName = new HashMap<>();
    private HashMap<String, String> m_participantsNametoID = new HashMap<>();

    public static String BILLS = "bills";
    public static String PARTICIPANTS = "participants";

    public Event(String ID){
        m_eventID = ID;
    }

    public void setBill(Bill bill){
        m_bills.add(bill);
    }

    public void setParticipant(String ID, String name){
        m_participantsIDtoName.put(ID, name);
        m_participantsNametoID.put(name, ID);
    }

    public void setDescription(String des){
        m_description = des;
    }

    public void setTime(String time){
        m_time = time;
    }

    public String getEventID(){
        return m_eventID;
    }

    public List<Bill> getAllBills(){
        return m_bills;
    }

    public String getParticipantNamebyID(String ID){
        if(m_participantsIDtoName.containsKey(ID))
            return m_participantsIDtoName.get(ID);
        else
            return null;
    }

    public String getParticipantIDbyName(String name){
        if(m_participantsNametoID.containsKey(name))
            return m_participantsNametoID.get(name);
        else
            return null;
    }

    public String getDescription(){
        return m_description;
    }

    public String getTime(){
        return m_time;
    }
}
