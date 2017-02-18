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
    private Group m_group;

    private String m_loaner;

    private HashMap<String, Double> m_getMoney = new HashMap<>();
    private HashMap<String, Double> m_returnMoney = new HashMap<>();

    private int m_splitMethod;

    private double m_moneyInvolved;

    public Bill(String billID, int splitMethod, double money){
        m_billID = billID;
        m_splitMethod = splitMethod;
        m_moneyInvolved = money;
    }

    public void setGroupID(Group group){
        m_group = group;
    }

    public void setLoaners(String loaner){
        m_loaner = loaner;
    }

    public String getBillID(){
        return m_billID;
    }

    public Group getGroupID(){
        return m_group;
    }

    public String getLoaner(){
        return m_loaner;
    }

    public void calSolution(){
        if(m_splitMethod == Bill.SPLIT_EQUALLY){
            List<String> members = m_group.getGroupMembers();
            double money_share = m_moneyInvolved / m_group.getGroupSize();
            if(members.contains(m_loaner)){
                for(String userID:members)
                    m_returnMoney.put(userID, money_share);
                m_getMoney.put(m_loaner, m_moneyInvolved - money_share);
            }else{
                m_getMoney.put(m_loaner, m_moneyInvolved);
                for(String userID:members)
                    m_returnMoney.put(userID, money_share);
            }
        } else if (m_splitMethod == Bill.SPLIT_UNEQUALLY) {

        } else if (m_splitMethod == Bill.SPLIT_BY_SHARES) {

        } else if (m_splitMethod == Bill.SPLIT_BY_PERCENTAGES) {

        } else if (m_splitMethod == Bill.SPLIT_BY_ADJUSTMENT){

        } else {
            System.out.println("Unrecognized Split Method!");
        }
    }
}
