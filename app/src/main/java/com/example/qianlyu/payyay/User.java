package com.example.qianlyu.payyay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

/**
 * Created by Qian Lyu on 2/15/2017.
 */

public class User {
    private String m_userID;
    private String m_emailAddress;
    private String m_preferredName;
    private String m_figurePath;

    private Date m_createdTime;
    private Date m_lastSignIn;

    public static DateFormat df = new SimpleDateFormat("MM/dd/yy");

    private HashMap<String, String> m_friendsIDToName = new HashMap<>();
    private HashMap<String, String> m_friendNameToID = new HashMap<>();
    private HashMap<String, String> m_friendsIDToEmail = new HashMap<>();
    private HashMap<String, String> m_friendEmailToID = new HashMap<>();
    private HashMap<String, String> m_friendIDToFigure = new HashMap<>();

    private HashMap<String, Bill> m_bills = new HashMap<>();
    private HashMap<String, Event> m_events = new HashMap<>();

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser user = auth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public final static String DISPALY_NAME = "preferredName";
    public final static String BILLS = "bills";
    public final static String CREATE_TIME = "createdTime";
    public final static String EMAIL_ADDRESS = "emailAddress";
    public final static String FRIENDS = "friends";
    public final static String GROUPS = "groups";
    public final static String LASR_SIGN_IN = "lastSignIn";
    public final static String FIGURE_PATH = "figure";
    public final static String EVENTS = "events";


    public final static String defaultFigure = "logo1.jpg";

    public static User curr_user;

    public User(){
    }

    public User(String userID, String emailAddress){
        m_userID = userID;
        m_emailAddress = emailAddress;
    }

    public void setEmailAddress(String email_address){
        m_emailAddress = email_address;
    }

    public void setPreferredName(String preferredName){
        m_preferredName = preferredName;
    }

    public void setFigurePath(String figurePath){
        m_figurePath = figurePath;
    }

    public void setCreatedTime(Date createdTime){
        m_createdTime = createdTime;
    }

    public void setLastSignIn(Date lastSignIn){
        m_lastSignIn = lastSignIn;
    }

    public void addFriend(String userID, String name, String email_address){
        if(!m_friendsIDToName.containsKey(userID)) {
            m_friendsIDToName.put(userID, name);
            m_friendNameToID.put(name, userID);
            m_friendsIDToEmail.put(userID, email_address);
            m_friendEmailToID.put(email_address, userID);
//            System.out.println("set up ID and name");
//            getFriendByName(name);
//            getFriendByID(userID);
        }
    }

    public void addFriendFigure(String userID, String uri){
        if(!m_friendsIDToEmail.containsKey(userID))
            m_friendIDToFigure.put(userID, uri);
    }

    public void addBill(Bill bill, String billID){
        if(!m_bills.containsKey(billID)) {
            m_bills.put(billID, bill);
        }
    }

    public void addEvent(Event event, String eventID){
        if(!m_events.containsKey(eventID)){
            m_events.put(eventID, event);
        }
    }

    public Bill getOneBill(String billID){
        return m_bills.get(billID);
    }

    public List<Bill> getAllBills(){
        List<Bill> result = new ArrayList<>();
        for(String key:m_bills.keySet()) {
            result.add(m_bills.get(key));
        }
        return result;
    }

    public Event getOneEvent(String eventID){
        return m_events.get(eventID);
    }

    public List<Event> getAllEvents(){
        List<Event> result = new ArrayList<>();
        for(String key:m_events.keySet()){
            result.add(m_events.get(key));
        }
        return result;
    }

    public String getEmailAddress(){
        return m_emailAddress;
    }

    public String getUserID(){
        return m_userID;
    }

    public String getPreferredName(){
        return m_preferredName;
    }

    public Date getCreatedTime(){
        return m_createdTime;
    }

    public Date getLastSignIn(){
        return m_lastSignIn;
    }

    public String getFigurePath(){
        return m_figurePath;
    }

    public String getFriendByID(String friendID){
        System.out.println("m_friendsIDToName.size(): " + m_friendsIDToName.size());
        System.out.println("friendID: " + friendID + " friendName: " + m_friendsIDToName.get(friendID));

        if(!m_friendsIDToName.containsKey(friendID)){
            return null;
        }else{
            return m_friendsIDToName.get(friendID);
        }
    }

    public String getFriendByName(String friendName){
        System.out.println("m_friendNameToID.size(): " + m_friendNameToID.size());
        System.out.println("friendName: " + friendName + " friendID: " + m_friendNameToID.get(friendName));

        if(!m_friendNameToID.containsKey(friendName)){
            return null;
        }else{
            return m_friendNameToID.get(friendName);
        }
    }

    public HashMap<String, String> getAllFriendIDbyName(){
        return m_friendsIDToName;
    }

    public String getFriendFigureURI(String userID){
        return m_friendIDToFigure.get(userID);
    }

    public int getFriendNumber(){
        return m_friendsIDToEmail.size();
    }

    public HashMap<String, Double> calculateBalance(){
        HashMap<String, Double> result = new HashMap<>();
        List<Bill> allBills = getAllBills();
        List<Event> allEvents = getAllEvents();
        for(Event event:allEvents){
            allBills.addAll(event.getAllBills());
        }
        for(Bill bill:allBills){
            result.putAll(bill.getBillDetails());
        }

        return result;
    }

    @Override
    public String toString(){
        StringBuffer result = new StringBuffer();
        result.append("prefer Name: " + m_preferredName + "\n");
        result.append("figure Path: " + m_figurePath + "\n");
        for(String friendID:m_friendsIDToName.keySet())
            result.append("friendID: " + friendID + "  friend Name: " + m_friendsIDToName.get(friendID) + "\n");
        for(String friendName:m_friendNameToID.keySet())
            result.append("friend Name: " + friendName + "  friend ID: " + m_friendNameToID.get(friendName) + "\n");
        return result.toString();
    }

    public static float convertPixelsToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }

    public static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }


    public static int convertDpToPx(int dp){
        return Math.round(dp*(Resources.getSystem().getDisplayMetrics().xdpi/DisplayMetrics.DENSITY_DEFAULT));

    }

    public static int convertPxToDp(int px){
        return Math.round(px/(Resources.getSystem().getDisplayMetrics().xdpi/DisplayMetrics.DENSITY_DEFAULT));
    }

    public String billToString(){
        String result = "total Bill: " + m_bills.size() + "\n";
        for(String key:m_bills.keySet()){
            Bill temp = m_bills.get(key);
            result += "description: " + temp.getDescription() + " time: " + temp.getCreatedTime() + "\n";
        }
        return result;
    }

    public String eventToString(){
        String result = "total Event: " + m_events.size() + "\n";
        for(String key:m_events.keySet()){
            Event temp = m_events.get(key);
            result += "description: " + temp.getDescription() + " time: " + temp.getTime() + "\n";
        }
        return result;
    }
}
