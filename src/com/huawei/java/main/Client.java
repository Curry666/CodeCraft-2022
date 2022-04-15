package com.huawei.java.main;

import java.util.ArrayList;

public class Client {

    //客户节点名称
    public  String name;
    //客户节点序号
    public  int index;

    //联通的边缘节点
    public  ArrayList<String> allServerName;

    public Client(int index, String name) {
        this.name = name;
        this.index = index;
    }

    public  String getName() {
        return this.name;
    }

    public  void setName(String name) {
        this.name = name;
    }

    public  int getIndex() {
        return this.index;
    }

    public  void setIndex(int index) {
        this.index = index;
    }

    public  ArrayList<String> getAllServerName() {
        return this.allServerName;
    }

    public  void setAllServerName(ArrayList<String> allServerName) {
        this.allServerName = allServerName;
    }
}
