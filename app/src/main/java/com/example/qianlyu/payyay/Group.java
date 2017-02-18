package com.example.qianlyu.payyay;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Qian Lyu on 2/15/2017.
 */

public class Group {
    private String m_groupID;
    // the person who loaned or who borrowed money;
    private List<String> m_userGroup = new ArrayList<>();
    // bills that are concerned with this group;
    private List<String> m_billGroups = new ArrayList<>();

    public Group(String groupID){
        m_groupID = groupID;
    }

    public void addBill(String billID){
        m_billGroups.add(billID);
    }

    public void addAllBills(List<String> billIDs){
        m_billGroups.addAll(billIDs);
    }

    public void addMember(String userID){
        m_userGroup.add(userID);
    }

    public void addAllMembers(List<String> userIDs){
        m_userGroup.addAll(userIDs);
    }

    public List<String> getGroupMembers(){
        return m_userGroup;
    }

    public List<String> getBillGroups(){
        return m_billGroups;
    }

    public int getGroupSize(){
        return m_userGroup.size();
    }
}
