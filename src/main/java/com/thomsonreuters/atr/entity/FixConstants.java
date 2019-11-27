package com.thomsonreuters.atr.entity;

import java.util.HashMap;
import java.util.Map;



//Fix Constants used for Session Zoom

public class FixConstants {

    public static final Map<String, String> msgTypeMap = new HashMap<>();
    public static final Map<String,String> ordStatMap = new HashMap<>();
    public static final Map<String,String> sideMap = new HashMap<>();

    static {
        msgTypeMap.put("D", "NewOrderSingle");
        msgTypeMap.put("8", "ExecutionReport");
        msgTypeMap.put("E", "NewOrderList");
        msgTypeMap.put("F", "OrderCancelRequest");
        msgTypeMap.put("G", "OrderCancelReplaceRequest");
        msgTypeMap.put("H", "OrderStatusRequest");
        msgTypeMap.put("j", "BusinessMessageReject");
        msgTypeMap.put("9", "OrderCancelReject");
        msgTypeMap.put("L", "ListExecute");
        msgTypeMap.put("K", "ListCancelRequest");
        msgTypeMap.put("Q", "DontKnowTrade");
        ordStatMap.put("0", "New");
        ordStatMap.put("1", "Partial Fill");
        ordStatMap.put("2", "Filled");
        ordStatMap.put("3", "Done for Day");
        ordStatMap.put("4", "Cancelled");
        ordStatMap.put("5", "Replaced");
        ordStatMap.put("6", "Pending Cancel");
        ordStatMap.put("8", "Rejected");
        ordStatMap.put("A", "Pending New");
        ordStatMap.put("E", "Pending Replace");
        sideMap.put("1", "B");
        sideMap.put("2", "S");
        sideMap.put("3", "B-");
        sideMap.put("4", "S+");
        sideMap.put("5", "SS");
        sideMap.put("6", "SS exempt");
        sideMap.put("7", "U");
        sideMap.put("8", "X");
        sideMap.put("9", "X short");
    }
}
