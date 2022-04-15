package com.huawei.java.main;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;


public class Server {

    //边缘节点名称
    public  String name;

    //边缘节点序号
    public  int index;

    //容量上限
    public  int maxLoad;

    //联通的客户节点
    public  ArrayList<String> allClientName;

    //<时间，<客户节点，分配给这个边缘服务器的值>>
    public   HashMap<Integer, HashMap<String, Integer>> timeClientLoad;


    /***
     * 上面这些是固定的  第一次赋值之后就不改变
     *
     * 下面这些再curload改变时需要做出对应改变  已写入函数会自动更改，不必手动操作
     */

    //某时刻当前负载
    public  HashMap<Integer,Integer> curLoad ;

    //某时刻剩余容量
    public  HashMap<Integer,Integer> restLoad;



    //最小的大哥位的负载值
    public  int minBossLoad;

    /***
     * 各种东西随curload改变而改变
     *
     */

    public  void setCurLoad(int TIME, int load) {
        curLoad.put(TIME, load);
        restLoad.put(TIME, getMaxLoad() - getCurLoad(TIME));
    }
    //设置某时刻下某服客户节点分过来的负载
    public  void setTimeClientLoad(int TIME, String clientName, int load) {
        if (!timeClientLoad.containsKey(TIME)) {
            HashMap<String, Integer>  data = new HashMap<>();
            data.put(clientName, load);
            timeClientLoad.put(TIME, data);
        }else {
            HashMap<String, Integer>  data = timeClientLoad.get(TIME);
            data.put(clientName, load);
            timeClientLoad.put(TIME, data);
        }
        setCurLoad(TIME, getAllLoad(TIME));
    }

    //设置某时刻下某服客户节点分过来的负载
    public  void setTimeClientLoad(int TIME) {
        HashMap<String, Integer>  data = new HashMap<>();
        timeClientLoad.put(TIME, data);
    }




    //获取某时刻下某服客户节点分过来的负载
    public  int getTimeClientLoad(int TIME, String clientName) {
        if(getTimeClientLoad().get(TIME).containsKey(clientName))
            return getTimeClientLoad().get(TIME).get(clientName);
        return 0;
    }

    public Server(String name, int index, int maxLoad, ArrayList<String> allClientName) {
        this.name = name;
        this.index = index;
        this.maxLoad = maxLoad;
        this.allClientName = allClientName;
        this.timeClientLoad = new HashMap<>();
        this.curLoad = new HashMap<>();
        this.restLoad = new HashMap<>();
    }

    //获取95分位的时刻
    public  int getTime_95() {
        ArrayList<Integer> allTimeLoad = new ArrayList<>();
        ArrayList<Integer> reference = new ArrayList<>();
        for(int key : timeClientLoad.keySet()){
            allTimeLoad.add(getAllLoad(key));
            reference.add(getAllLoad(key));
        }
        allTimeLoad.sort(Comparator.naturalOrder());


        return reference.indexOf(allTimeLoad.get((int)(allTimeLoad.size()*0.95)));
    }




    //获取95分位值
    public  int getLoad_95() {
        ArrayList<Integer> allTimeLoad = new ArrayList<>();
        for(int key : timeClientLoad.keySet()){
            allTimeLoad.add(getAllLoad(key));
        }
        allTimeLoad.sort(Comparator.naturalOrder());
        return allTimeLoad.get((int)(allTimeLoad.size()*0.95));
    }

    //设置最小大哥位的值
    public  int setMinBossLoad() {
        ArrayList<Integer> allTimeLoad = new ArrayList<>();
        for(int key : timeClientLoad.keySet()){
            allTimeLoad.add(getAllLoad(key));
        }
        if(allTimeLoad.size() == 0)
            return 0;
        allTimeLoad.sort(Comparator.naturalOrder());
        return allTimeLoad.get((int)(allTimeLoad.size()*0.95)+1);
    }


    //获取当前时刻所有负载
    public  int getAllLoad(int TIME) {
        int sumLoad = 0;
        for(String a : timeClientLoad.get(TIME).keySet()){
            sumLoad += timeClientLoad.get(TIME).get(a);
        }
        return sumLoad;
    }

    public  ArrayList<String> getAllClientName() {
        if(allClientName.size() == 0)
            return null;
        return allClientName;
    }


    public  int getMaxLoad() {
        return maxLoad;
    }

    public  void setMaxLoad(int maxLoad) {
        this.maxLoad = maxLoad;
    }


    public  String getName() {
        return name;
    }

    public  void setName(String name) {
        this.name = name;
    }

    public  int getIndex() {
        return index;
    }

    public  void setIndex(int index) {
        this.index = index;
    }

    public  HashMap<Integer, HashMap<String, Integer>> getTimeClientLoad() {
        return timeClientLoad;
    }

    public  void addClientName(String clientName) {
        if(allClientName.contains(clientName))
            return ;
        allClientName.add(clientName);
    }

    public  int getCurLoad(int TIME) {
        if(curLoad.containsKey(TIME))
            return curLoad.get(TIME);
        return 0;
    }

    public  int getRestLoad(int TIME) {
        return restLoad.get(TIME);
    }

    public int getMinBossLoad() {
        return setMinBossLoad();
    }

    public void setMinBossLoad(int minBossLoad) {
        this.minBossLoad = minBossLoad;
    }
}
