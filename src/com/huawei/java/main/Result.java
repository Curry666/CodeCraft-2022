package com.huawei.java.main;

import java.util.HashMap;
import java.util.Map;

public class Result {
    private Map<Integer,Map<String, Map<String, Integer>>> Data = new HashMap<>();
    private int Cost;

    public Map<Integer, Map<String, Map<String, Integer>>> getData() {
        return Data;
    }

    public void setData(Map<Integer, Map<String, Map<String, Integer>>> data) {
        Data = data;
    }

    public int getCost() {
        return Cost;
    }

    public void setCost(int cost) {
        Cost = cost;
    }
}
