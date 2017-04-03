package com.example.qianlyu.payyay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Qian Lyu on 2/15/2017.
 */

public class Bill {
    // not so sure about their meaning???
    public static int SPLIT_EQUALLY = 0;
    public static int SPLIT_UNEQUALLY = 1;
    public static int SPLIT_BY_PERCENTAGES = 2;
    public static int SPLIT_BY_SHARES = 3;
    public static int SPLIT_BY_ADJUSTMENT = 4;

    private String m_billID;
    private HashMap<String, Double> bill_details = new HashMap<>();

    private String m_loaner;
    private String m_create_time;
    private String m_amount;

    public static String LOANER = "loaner";
    public static String DESCRIPTION = "description";
    public static String BORROWER = "borrower";
    public static String CREATED_TiME = "createdTime";
    public static String REMAINING = "remaining";
    public static String AMOUNT = "amount";
    public static String[] current_type = new String[]{"US Dollor", "Euro", "Japanese Yen"
            , "Great British Pound", "Swiss Franc", "Canadian Dollar"};
    public static double[] current_ratio = new double[]{1, 1.086, 0.0090319, 1.2563, 1.0143, 0.7472};

    public String m_description;

    public Bill(String billID){
        m_billID = billID;
    }

    public void addDescription(String description){
        m_description = description;
    }

    public void addBillDetails(String involved_person, double money){
        bill_details.put(involved_person, money);
    }

    public void setAmount(String amount){
        m_amount = amount;
    }

    public void setLoaners(String loaner){
        m_loaner = loaner;
    }

    public void setCreatedTime(String date){
        m_create_time = date;
    }

    public String getAmount(){
        return m_amount;
    }

    public String getBillID(){
        return m_billID;
    }

    public String getCreatedTime(){
        return m_create_time;
    }

    public HashMap<String, Double> getBillDetails(){
        return bill_details;
    }

    public List<String> getAllParticipants(){
        List<String> result = new ArrayList<>();
        for(String participantID:bill_details.keySet()){
            if(participantID.equals(User.curr_user.getUserID()))
                result.add(User.curr_user.getPreferredName());
            else
                result.add(User.curr_user.getFriendByID(participantID));
        }
        return result;
    }

    public String getLoaner(){
        return m_loaner;
    }

    public String getDescription(){
        return m_description;
    }
}
