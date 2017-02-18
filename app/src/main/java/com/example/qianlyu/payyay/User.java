package com.example.qianlyu.payyay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

/**
 * Created by Qian Lyu on 2/15/2017.
 */

public class User {
    private String m_userID;
    private String m_emailAddress;
    private String m_preferredName;

    private Date m_createdTime;
    private Date m_lastSignIn;

    private List<String> m_friends = new ArrayList<>();

    private List<String> m_groups = new ArrayList<>();
    private List<String> m_billBorrowed = new ArrayList<>();
    private List<String> m_billLoaned = new ArrayList<>();

    public User(String userID, String emailAddress){
        m_userID = userID;
        m_emailAddress = emailAddress;
    }

    public void setPreferredName(String preferredName){
        m_preferredName = preferredName;
    }

    public void setCreatedTime(Date createdTime){
        m_createdTime = createdTime;
    }

    public void setLastSignIn(Date lastSignIn){
        m_lastSignIn = lastSignIn;
    }

    public void addFriend(String userID){
        m_friends.add(userID);
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

    public Date getM_lastSignIn(){
        return m_lastSignIn;
    }

    public List<String> getAllFriends(){
        return m_friends;
    }
}
