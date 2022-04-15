package com.huawei.java.main;

import java.io.*;
import java.util.*;


public class CSVUtils {

    //数据文件的路径
    public static String pathBandWidth = "./data/site_bandwidth.csv";
    public static String pathQos = "./data/qos.csv";
    public static String pathDemand = "./data/demand.csv";
    public static String pathConfig = "./data/config.ini";
    public static String pathFolder = "./output";
    public static String pathFile = "./output/solution.txt";

    //每个客户端节点按照时间的带宽需求 List.get(i)是这个时刻i所有客户节点的需求
    //List.get(i).get(0)是i时刻客户节点0的需求
    public static List<ArrayList<Integer>> M;
    //客户节点名称映射的数字
    public static HashMap<String, Integer> Mp_1;
    //每个边缘节点的最大带宽
    public static ArrayList<Integer> N;
    //边缘节点名称映射的数字
    public static HashMap<String, Integer> Mp_2;
    //Qos
    public static List<ArrayList<Integer>> Qos;
    //Qos限制
    public static int Qos_constraint;
    //Qos中的client顺序
    public static ArrayList<String> clientOrder;
    //Qos中的server顺序
    public static ArrayList<String> serverOrder;
    //demand.csv中的client顺序
    public static ArrayList<String> fileClientOrder;



    //时间长度
    public static int timeLength;
    //边缘节点的数量
    public static  int serveNum;
    //95序号
    public static int indexOf95;
    //所有的结果集(结果包括两部分，一部分是这次遍历的数据，一部分是这个结果的总成本)
//    public static List<Result> AllResults = new ArrayList<Result>();
    //每个时刻的大哥节点
    public static Map<Integer, ArrayList<Integer>> serverLoadFull = new HashMap<>();

    //初始化
    public static  void Initialize(Boolean windows){
        if (!windows) {
            pathFile = pathFile.substring(1);
            pathFolder = pathFolder.substring(1);
            pathDemand = pathDemand.substring(1);
            pathQos = pathQos.substring(1);
            pathBandWidth = pathBandWidth.substring(1);
            pathConfig = pathConfig.substring(1);
        }
        read_Qos(pathQos);
        read_M(pathDemand);
        read_N(pathBandWidth);
        Qos_constraint = read_config(pathConfig);
        timeLength = M.size();
        serveNum = CSVUtils.N.size();
        indexOf95 = (int)Math.ceil(timeLength*0.95) - 1;
        serverLoadFull = GetFullServerNum();
    }
    //获取每个客户节点在每个时间节点的
    public static void read_M(String readpath) {
        //想要读取的数据是一个[N,Dim]的二维矩阵
        M = new ArrayList<>();
        fileClientOrder = new ArrayList<>();
        //读取客户节点的节点映射数据
//        CSVUtils.Mp_1 = new HashMap<>();
        boolean flag = true;
        File inFile = new File(readpath);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            while (reader.ready()) {
                //这里没有办法使用矩阵进行操作，因为我们一行一行的读没有办法知道csv中保存数据的长度和宽度
                //但是返回的是一个String，我们可以对这个String进行操作，按照分隔符将其分割，最好能是能够保存到一个完整的结构中
                String line = reader.readLine();
                String[] splitlines = line.split(",");
                if (flag) {
                    //如果是第一行
                    for (int i =1; i < splitlines.length; i++) {
                        fileClientOrder.add(splitlines[i]);
                    }
                }else {
                    ArrayList<Integer> curDemand = new ArrayList<>(clientOrder.size());
                    if (!isListEqual(fileClientOrder, clientOrder)) {
                        for (int i = 0; i < clientOrder.size(); i++) {
                            curDemand.add(0);
                        }
                        for (int i =1; i < splitlines.length; i++) {
                            String curClient = fileClientOrder.get(i-1);
                            int rightIndex = clientOrder.indexOf(curClient);
                            curDemand.set(rightIndex, Integer.valueOf(splitlines[i]));
                        }
                        M.add(curDemand);
                    } else {
                        for (int i =1; i < splitlines.length; i++) {
                            curDemand.add(Integer.valueOf(splitlines[i]));
                        }
                        M.add(curDemand);
                    }
                }
                flag = false;
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //获取每个边缘节点的最大负载
    public static void read_N(String readpath) {
        N=new ArrayList<>();
        for (int i = 0; i < serverOrder.size(); i++) {
            N.add(0);
        }
        boolean flag = true;
        File inFile = new File(readpath);
        HashMap<String, Integer> oldN = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] splitlines = line.split(",");
                if (flag) {
                    flag = false;
                    continue;
                }else {
                    String serverName = splitlines[0];
                    int maxLoad = Integer.valueOf(splitlines[1]);
                    oldN.put(serverName, maxLoad);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String key : oldN.keySet()) {
            int index =  serverOrder.indexOf(key);
            N.set(index, oldN.get(key));
        }
    }
    //获取Qos限制
    public static void read_Qos(String readpath){
        //想要读取的数据是一个[N,Dim]的二维矩阵
        Qos = new ArrayList<>();
        clientOrder = new ArrayList<>();
        serverOrder = new ArrayList<>();
        Mp_1 = new HashMap<>();
        Mp_2 = new HashMap<>();
        boolean flag = true;
        File inFile = new File(readpath);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inFile));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] splitlines = line.split(",");
                if (flag) {
                    for(int i = 1; i < splitlines.length; i++) {
                        clientOrder.add(splitlines[i]);
                        Mp_1.put(splitlines[i], i-1);
                    }
                } else {
                    serverOrder.add(splitlines[0]);
                    Mp_2.put(splitlines[0], serverOrder.size()-1);
                    ArrayList<Integer>nums = new ArrayList<>();
                    for(int i = 1; i < splitlines.length; i++) {
                        nums.add(Integer.valueOf(splitlines[i]));
                    }
                    Qos.add(nums);
                }
                flag = false;
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //获取配置文件结果
    public static int read_config(String path) {
        File inFile = new File(path);
        boolean flag = true;
        String key = "";
        String value = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(inFile));

            while (reader.ready()) {
                if (flag) {
                    flag = false;
                    continue;
                } else {
                    String line = reader.readLine();
                    if (line.contains("=")) {
                        int delimiterPos = line.indexOf("=");
                        key = line.substring(0, delimiterPos).trim();
                        value = line.substring(delimiterPos + 1, line.length()).trim();
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Integer.parseInt(value);
    }
    //得到该客户节点连通的所有边缘节点
    public static  ArrayList<Integer> GetAllServer(int clientIndex){
        ArrayList<Integer> res= new ArrayList<>();
        for (int i=0 ;i<Qos.size();i++){
            if (Qos.get(i).get(clientIndex) < Qos_constraint){
                res.add(i);
            }
        }
        return  res;
    }
    //得到该边缘节点连通的所有客户节点
    public static  ArrayList<Integer> GetAllClient(int serverIndex){
        ArrayList<Integer> res= new ArrayList<>();
        for (int i=0 ;i<Qos.get(0).size();i++){
            if (Qos.get(serverIndex).get(i) < Qos_constraint){
                res.add(i);
            }
        }
        return  res;
    }
    //查看server和client是否相连接
    public static Boolean Ping(int serverIndex, int clientIndex){
        if (Qos.get(serverIndex).get(clientIndex) < Qos_constraint) {
            return true;
        }
        return false;
    }
    //获取用户节点名称
    public static  ArrayList<String> GetClientName() throws IOException {
        return  clientOrder;
    }
    //获取边缘节点名称
    public static  ArrayList<String> GetServerName() throws IOException {
        return  serverOrder;
    }
    //每一次loop都会得到每一个时刻的分配结果
    public  static Map<String, Map<String, Integer>> loop (ArrayList<Integer> arrayList,int TIME) throws IOException {

        /**
         * 声明所有变量
         */


        //分别获取用户节点和边缘节点
        ArrayList<String> allClientName = GetClientName();
        ArrayList<String> allServerName = GetServerName();
        //边缘节点的数量
        int serverNum = CSVUtils.N.size();
        //该时间点当前所有边缘节点的负载
        int[] curServerLoad = new int[serverNum];
        //当前时刻所有边缘节点剩余的负载（就是最大负载-当前负载）
        int[] curServerRestLoad = new int[serverNum];


        //第一层：保存的是单个时刻内的分配数据 次级每个单位是保存每个客户节点的分配情况
        Map<String, Map<String, Integer>> oneTimeData = new HashMap<String, Map<String, Integer>>();

        //初始化当前所有边缘节点剩余的负载（当前就是最大负载）
        //保存所有边缘节点的上限值
        int[] serverMaxload = new int[serverNum];
        for (int b = 0; b < serverNum; b++){
            curServerRestLoad[b] = CSVUtils.N.get(b);
            serverMaxload[b] = CSVUtils.N.get(b);
        }
        //当前客户节点剩余的需求
        int[] curClientdemand = new int[CSVUtils.M.get(0).size()];
        //客户节点的总需求
        int totalClientDemand = 0;
        //计算总需要的负载
        for (int i=0 ;i<arrayList.size();i++){
            totalClientDemand += arrayList.get(i);
            curClientdemand[i] = arrayList.get(i);
        }
        //目前已经分配的客户节点
//		HashMap<Integer, Boolean> alreadyAssignedClient = new HashMap<>();
        //在所有客户节点都完全平均分配情况下各个边缘节点的期望负载大小
        int[] loadRefrence = new int[serverNum];
        //有多少服务器不会被用到
        int noUseServer = 0;

        //优从需求大/可连通的边缘节点的数量少的客户节点开始遍历（这个数组就是存放优先级）
        int[] clientWeights = new int[curClientdemand.length];
        //这个TreeMap会按照key降序排列，key就是上面这些客户节点的优先级，优先级越大越先分配
        TreeMap<Integer, Double> clientsMappingWeights = new TreeMap<Integer, Double>();


        /***
         *
         * 优化预处理
         *
         *
         *
         */
        ArrayList<Integer> headServerList = serverLoadFull.get(TIME);
        int LOOPTIMES = 0;
        if(!(null == headServerList)) {
            //对每个时刻大哥边缘节点拉满
            for (int headServerIndex : headServerList) {
                //获取边缘节点联通的客户节点
                ArrayList<Integer> serverConnectClient = CSVUtils.GetAllClient(headServerIndex);
                //记录所有联通客户节点的需求
                int allDemand = 0;
                //计算所有需求之和
                for (int clientIndex : serverConnectClient) {
                    allDemand += curClientdemand[clientIndex];
                }
                //如果所有需求之和小于服务器容量直接全部丢给服务器 客户节点需求归零
                if (allDemand <= curServerRestLoad[headServerIndex]) {
                    //所有需求丢给边缘节点
                    curServerLoad[headServerIndex] += allDemand;
                    curServerRestLoad[headServerIndex] = serverMaxload[headServerIndex] - curServerLoad[headServerIndex];
                    //记录入map
                    for (int clientIndex : serverConnectClient) {
                        //客户需求为0则跳过
                        if (curClientdemand[clientIndex] == 0) continue;
                        //保存分配 后续加入最终结果中
                        Map<String, Integer> optDataSer = new HashMap<>();
                        optDataSer.put(allServerName.get(headServerIndex), curClientdemand[clientIndex]);
                        if(oneTimeData.containsKey(allClientName.get(clientIndex))){
                            Set<String> proServerData = oneTimeData.get(allClientName.get(clientIndex)).keySet();
                            //整合数据
                            for(String data : proServerData){
                                optDataSer.put(data,oneTimeData.get(allClientName.get(clientIndex)).get(data));
                            }
                        }
                        oneTimeData.put(allClientName.get(clientIndex), optDataSer);
                        //客户节点需求归零
                        curClientdemand[clientIndex] = 0;
                    }
                } else {// 如果总客户需求大于了当下边缘节点容纳总值 那么按客户需求的权值做比例放入边缘节点


                    int nowLoad = curServerRestLoad[headServerIndex];
                    for (int i = 0; i < serverConnectClient.size(); i++) {
                        int clientIndex = serverConnectClient.get(i);
                        //客户节点需求为0 则跳过
                        if (curClientdemand[clientIndex] == 0) continue;

                        //如果是最后一个客户姐节点了就直接填满边缘节点的负载
                        if (i == serverConnectClient.size() - 1) {
                            //客户节点用来填满边缘节点的负载
                            int gapLoad = curServerRestLoad[headServerIndex];
                            //服务器填满
                            curServerLoad[headServerIndex] += gapLoad;
                            curServerRestLoad[headServerIndex] = serverMaxload[headServerIndex] - curServerLoad[headServerIndex];

                            //保存分配 后续加入最终结果中
                            Map<String, Integer> optDataSer = new HashMap<>();
                            optDataSer.put(allServerName.get(headServerIndex), gapLoad);
                            if(oneTimeData.containsKey(allClientName.get(clientIndex))){
                                Set<String> proServerData = oneTimeData.get(allClientName.get(clientIndex)).keySet();
                                //整合数据
                                for(String data : proServerData){
                                    optDataSer.put(data,oneTimeData.get(allClientName.get(clientIndex)).get(data));
                                }
                            }

                            oneTimeData.put(allClientName.get(clientIndex), optDataSer);
                            //客户节点需求归零
                            curClientdemand[clientIndex] -= gapLoad;

                            continue;

                        }

                        int realLoad = (int) (nowLoad * ((double) curClientdemand[clientIndex] / (double)allDemand)) + 1;
                        //如果这个比例超过了客户节点需求 那么直接全部放进去 并且重新计算总权值
                        if (realLoad >= curClientdemand[clientIndex]) {

                            realLoad = curClientdemand[clientIndex];
                            curServerLoad[headServerIndex] += realLoad;
                            curServerRestLoad[headServerIndex] = serverMaxload[headServerIndex] - curServerLoad[headServerIndex];
                            //保存分配 后续加入最终结果中
                            Map<String, Integer> optDataSer = new HashMap<>();
                            optDataSer.put(allServerName.get(headServerIndex), realLoad);
                            if(oneTimeData.containsKey(allClientName.get(clientIndex))){
                                Set<String> proServerData = oneTimeData.get(allClientName.get(clientIndex)).keySet();
                                //整合数据
                                for(String data : proServerData){
                                    optDataSer.put(data,oneTimeData.get(allClientName.get(clientIndex)).get(data));
                                }
                            }

                            oneTimeData.put(allClientName.get(clientIndex), optDataSer);
                            //客户节点需求归零
                            curClientdemand[clientIndex] -= realLoad;
                            allDemand = 0;
                            //重新计算所有需求之和
                            for (int newClientIndex = i + 1; newClientIndex < serverConnectClient.size(); newClientIndex++) {
                                allDemand += curClientdemand[serverConnectClient.get(newClientIndex)];

                            }
                            nowLoad = curServerRestLoad[headServerIndex];
                        } else { //不超过就按正常放

                            if(realLoad > curServerRestLoad[headServerIndex])
                                realLoad = curServerRestLoad[headServerIndex];
                            curServerLoad[headServerIndex] += realLoad;
                            curServerRestLoad[headServerIndex] = serverMaxload[headServerIndex] - curServerLoad[headServerIndex];
                            //保存分配 后续加入最终结果中
                            Map<String, Integer> optDataSer = new HashMap<>();
                            optDataSer.put(allServerName.get(headServerIndex), realLoad);
                            if(oneTimeData.containsKey(allClientName.get(clientIndex))){
                                Set<String> proServerData = oneTimeData.get(allClientName.get(clientIndex)).keySet();
                                //整合数据
                                for(String data : proServerData){
                                    optDataSer.put(data,oneTimeData.get(allClientName.get(clientIndex)).get(data));
                                }
                            }
                            oneTimeData.put(allClientName.get(clientIndex), optDataSer);
                            //客户节点需求调整
                            curClientdemand[clientIndex] -= realLoad;


                        }
                    }
                }
                LOOPTIMES++;
            }
        }

        /***
         *
         * 结束
         *
         *
         */


        //求出各个节点期望负载
        for(int i = 0;i<arrayList.size(); i++){
            //获取第i个客户节点可达到的边缘节点
            ArrayList<Integer> temp = CSVUtils.GetAllServer(i);
            //每个客户节点给所有可达到边缘节点的平均负载
            int avgOfLoad = arrayList.get(i)/temp.size();
            //让每个可达到边缘节点平均分第i个客户节点的负载需求
            for(int tem : temp){
                //如果是大哥边缘节点则跳过  即参考值为0
                if(curServerLoad[tem] == serverMaxload[tem]) {
                    loadRefrence[tem] = 0;
                    continue;
                }
                loadRefrence[tem] += avgOfLoad;
            }
        }
        //求出所有没有使用到的边缘节点的数量
        for(int i = 0; i < loadRefrence.length; i++) {
            if(loadRefrence[i] == 0){
                noUseServer++;
            }
        }
        //计算每个客户节点的优先级
        for (int i=0 ;i < curClientdemand.length ;i++) {
            ArrayList<Integer> serves = CSVUtils.GetAllServer(i);
            clientWeights[i] = curClientdemand[i]/serves.size();
        }
        //把每个客户节点的优先级和它的序号放到TreeMap里
        for (int j=0 ;j < clientWeights.length ;j++) {
            clientsMappingWeights.put(j, (double)clientWeights[j]);
        }
        //根据value排序这个TreeMap
        List<Map.Entry<Integer, Double>> list = CSVUtils.MapSort(clientsMappingWeights);
        //理想负载
        double ideal = totalClientDemand / (serverNum-noUseServer);

        /**
         * 下面就是遍历分配这个时刻所有客户节点的需求
         */
        for (Map.Entry<Integer, Double> e: list) {
            int i = e.getKey();
            {
                //首先获取这个客户节点能连通的所有边缘节点
                ArrayList<Integer> serves = CSVUtils.GetAllServer(i);
                //初始化权值list
                ArrayList<Double> Weights =new ArrayList<>();
                //总权值
                double totalWeights = 0;
                //第一次要分配的负载
                int firstLoad = curClientdemand[i];
                //当前剩余的负载
                int curLoad = firstLoad;
                //第二层：保存的是每个边缘节点的分配情况
                Map<String, Integer> oneClientData = new HashMap<>();
                //存放当前负载的double数组版本，因为最后要算方差
                double[] doubleCurServerLoad = new double[curServerLoad.length];

                //为每一个边缘节点计算权值，如果这个边缘节点的期望负载为0，那权值就为0.0
                for(int a:serves){
                    if(loadRefrence[a] == 0){
                        Weights.add(0.0);
                        continue;
                    }
                    double serverNowWeight = ideal*ideal/(loadRefrence[a]*(ideal+curServerLoad[a]));


                    Weights.add(serverNowWeight);
                }
                //计算总权值
                for (Double a: Weights){
                    totalWeights +=a;
                }
                // 处理客户无需求的情况 若无需求向oneTimeData中传入一个key ,value为空
                if(firstLoad == 0){
                    //如果前面分配过了就跳过 没有就加入记录再跳过
                    if(oneTimeData.containsKey(allClientName.get(i))){
                        continue;
                    }else {
                        oneTimeData.put(allClientName.get(i), null);
                        continue;
                    }
                }
                //对当前用户节点的负载需求进行分配
                while (curLoad != 0) {
                    int loopLoad = curLoad;
                    for (int nowIndex=0 ;nowIndex< serves.size() ;nowIndex++){
                        //当前边缘节点的序号
                        int serverIndex = serves.get(nowIndex);

                        //负载满了 跳过
                        if(curServerLoad[serverIndex] == serverMaxload[serverIndex]) continue;

                        //该边缘节点的剩余负载
                        int maxLoad = curServerRestLoad[serverIndex];
                        //当前权值占总权值的比重
                        double proportion = Weights.get(nowIndex) /totalWeights;
                        //客户节点i要预分配到边缘节点serverIndex的负载
                        int  load = (int)(loopLoad * proportion);


//						if (headServerList != null) {
//							for (int index = 0; index < headServerList.size(); index++) {
//								if (headServerList.get(index) == serverIndex) {
//									if (maxLoad >= curLoad) {
//										curClientdemand[serverIndex] = 0;
//										curServerLoad[serverIndex] += curLoad;
//										curServerRestLoad[serverIndex] -= curLoad;
//										if (oneClientData.containsKey(allServerName.get(serverIndex))) {
//											int newLoad = oneClientData.get(allServerName.get(serverIndex)) + curLoad;
//											oneClientData.replace(allServerName.get(serverIndex),newLoad);
//											curLoad = 0;
//
//										} else {
//											//如果没有记录就直接put
//											oneClientData.put(allServerName.get(serverIndex), curLoad);
//											curLoad = 0;
//										}
//										break;
//									}
//								}
//							}
//						}


                        //如果出现负载与比率相乘取整为0的情况说明这个curload足够小了，找个能放下他的边缘节点直接放吧就
                        if (load == 0) {
                            //如果当前该边缘节点剩余负载可以放下剩下的需求
                            if(maxLoad >= curLoad) {
                                //更新各种数组和变量
                                curServerLoad[serverIndex] += curLoad;
                                curServerRestLoad[serverIndex] -= curLoad;
                                curClientdemand[i] -= curLoad;

                                //写入文件的那个数据,如果给这个边缘节点分配过，那就给他加上这次分配给他的
                                if (oneClientData.containsKey(allServerName.get(serverIndex))) {
                                    int newLoad = oneClientData.get(allServerName.get(serverIndex)) + curLoad;
                                    oneClientData.replace(allServerName.get(serverIndex),newLoad);
                                    curLoad = 0;

                                } else {
                                    //如果没有记录就直接put
                                    oneClientData.put(allServerName.get(serverIndex), curLoad);
                                    curLoad = 0;
                                }
                                break;
                            }
                        } else {
                            //正常分配部分
                            if (load >= maxLoad) {
                                curServerLoad[serverIndex] += maxLoad;
                                curServerRestLoad[serverIndex] -= maxLoad;
                                curClientdemand[i] -= maxLoad;
                                curLoad -= maxLoad;

                                // 插入对应边缘节点以及分配给他的负载
                                if (oneClientData.containsKey(allServerName.get(serverIndex))) {
                                    int newLoad = oneClientData.get(allServerName.get(serverIndex)) + maxLoad;
                                    oneClientData.replace(allServerName.get(serverIndex),newLoad);
                                } else {
                                    oneClientData.put(allServerName.get(serverIndex), maxLoad);
                                }
                                continue;
                            } else {
                                curServerLoad[serverIndex] += load;
                                curServerRestLoad[serverIndex] -= load;
                                curClientdemand[i] -= load;
                                curLoad -= load;

                                // 插入对应边缘节点以及分配给他的负载
                                if (oneClientData.containsKey(allServerName.get(serverIndex))) {
                                    int newLoad = oneClientData.get(allServerName.get(serverIndex)) + load;
                                    oneClientData.replace(allServerName.get(serverIndex),newLoad);
                                } else {
                                    oneClientData.put(allServerName.get(serverIndex), load);
                                }
                            }
                        }
                        //如果当前分配已经把需求分完了，那就直接break
                        if (curLoad==0){
                            break;
                        }
                    }
                    if (curLoad == loopLoad) {
                        System.out.println("发生了死循环");
                        break;
                    }
                }
                if(oneTimeData.containsKey(allClientName.get(i))){
                    Set<String> proServerData = oneTimeData.get(allClientName.get(i)).keySet();
                    //整合数据
                    for(String data : proServerData){
                        oneClientData.put(data,oneTimeData.get(allClientName.get(i)).get(data));
                    }
                }
                oneTimeData.put(allClientName.get(i), oneClientData);
            }

        }
        return oneTimeData;
    }
    //检查结果是否合理
    public static boolean Check(Map<String, Map<String, Integer>> timeData, int time){
        System.out.println("下面检查的时间点为["+time+"]**********************************************");
        int[] serverSumLoad = new int[N.size()];
        for (Map.Entry<String, Map<String, Integer>> entry : timeData.entrySet()) {
            System.out.println("-----------------------------------------------------------------------------------");
            System.out.println("[当前检查的节点是客户节点名称为"+entry.getKey()+"， 它的序号为"+Mp_1.get(entry.getKey())+", 下面是它能联通的边缘节点]");

            //客户节点的序号
            int clientIndex = Mp_1.get(entry.getKey());
            int clientDmand = M.get(time).get(clientIndex);
            Map<String, Integer> clientDri = entry.getValue();
            int sumServerLoad = 0;
            if (clientDri==null){
                continue;
            }
            for (Map.Entry<String, Integer> entry1 : clientDri.entrySet()) {

                System.out.println("当前检查的节点是边缘节点名称为"+entry1.getKey()+"， 它的序号为"+Mp_2.get(entry1.getKey())
                        +", 它的负载为"+entry1.getValue());

                int load = entry1.getValue();
                int serverIndex = Mp_2.get(entry1.getKey());
                //判断两节点是否可以ping通
                if (!Ping(serverIndex, clientIndex)) {
                    System.out.println("出现节点ping不通的错误，分别为边缘节点"+entry1.getKey()+" 以及客户节点"+entry.getKey());
                    return false;
                }
                serverSumLoad[serverIndex] += load;
                //首先看看这个负载是否大于最后的负载
                if (load > N.get(serverIndex)) {
                    System.out.println("单次分配出现超出最大负载的错误!!!");
                    return false;
                }
                sumServerLoad += load;
            }
            System.out.println("以上为所有边缘节点的分配情况");
            if (sumServerLoad != clientDmand) {
                System.out.println("出现连通边缘节点的分配总和负载不等于该客户节点的需求的错误!!!"
                        +" 当前边缘节点承受总负载为"+sumServerLoad+"当前客户需求为"+clientDmand);
                return false;
            }
            System.out.println("所有边缘节点的总负载为"+sumServerLoad);
            System.out.println("-----------------------------------------------------------------------------------");
            System.out.println();
            System.out.println();
        }
        //检查这个时间点所有分配对于边缘节点的负载
        for (int i =0; i < serverSumLoad.length; i++) {
            if (N.get(i) < serverSumLoad[i]) {
                System.out.println("多次分配出现超出最大负载的错误");
                return false;
            }
        }
        System.out.println("上面检查的时间点为["+time+"]**********************************************");
        System.out.println();
        return true;
    }
    //获取一个TreeMap按value降序排列的版本
    public static List<Map.Entry<Integer, Double>> MapSort(TreeMap<Integer, Double> maps) {
        //自定义比较器
        Comparator<Map.Entry<Integer, Double>> valCmp = new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public  int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                // TODO Auto-generated method stub
                return (int)(o2.getValue() - o1.getValue());  // 降序排序，如果想升序就反过来
            }
        };

        //将map转成List，map的一组key，value对应list一个存储空间
        List<Map.Entry<Integer, Double>> list = new ArrayList<Map.Entry<Integer, Double>>(maps.entrySet()); //传入maps实体
        Collections.sort(list, valCmp); // 注意此处Collections 是java.util包下面的,传入List和自定义的valCmp比较器
        return list;
    }
    //获取一个TreeMap按value降序排列的版本(第二个参数为false就是升序)
    public static List<Map.Entry<Integer, Integer>> MapSort(TreeMap<Integer, Integer> maps, boolean desc) {
        //自定义比较器
        Comparator<Map.Entry<Integer, Integer>> valCmp = new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                // TODO Auto-generated method stub
                if (desc) {
                    return o2.getValue() - o1.getValue();  // 降序排序，如果想升序就反过来
                }
                return o1.getValue() - o2.getValue();
            }
        };

        //将map转成List，map的一组key，value对应list一个存储空间
        List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(maps.entrySet()); //传入maps实体
        Collections.sort(list, valCmp); // 注意此处Collections 是java.util包下面的,传入List和自定义的valCmp比较器
        return list;
    }
    //将结果写入到文件中
    public static Boolean OutPutFile(Map<Integer ,Map<String, Map<String, Integer>>> sortData) {
        try{
            File file1 =new File(pathFolder);
            //当文件不存在时，创建
            if(!file1.exists()){
                file1.mkdir();
            }
            File file = new File(pathFile);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            //设置true是指加入文件尾，不清除原内容
            FileWriter fileWriter = new FileWriter(file,true);
            boolean flag = false;

            for(int i=0;i<sortData.size();i++){
                //获得i时刻的分配结果
                Map<String, Map<String, Integer>> oneTimeData =  sortData.get(i);
                Set<String> oneTimeKeySet = oneTimeData.keySet();
//				fileWriter.write("\n-----------------------------------时间点"+i+"-------------------------\n");
                for(String key : oneTimeKeySet){
                    if(flag){
                        fileWriter.write("\n");
                    }
                    if(null == oneTimeData.get(key)){
                        fileWriter.write(key+ ":" );
                        flag = true;
                        continue;
                    }
                    Map<String, Integer> oneClient = oneTimeData.get(key);
                    Set<String> oneClientKeySet = oneClient.keySet();
                    fileWriter.write(key+ ":" );
                    //二层循环，循环输出每个节点分配的边缘节点情况
                    boolean sign = true;
                    for(String oneServerkey : oneClientKeySet){
                        if(sign) {
                            fileWriter.write("<"+oneServerkey+","+oneClient.get(oneServerkey)+">");
                            sign = false;
                            continue;
                        }
                        fileWriter.write(",<"+oneServerkey+","+oneClient.get(oneServerkey)+">" );
                    }
                    flag = true;
                }
            }
            fileWriter.close();
            System.out.println("Done");
        }catch(IOException e){
            e.printStackTrace();
        }
        return true;
    }
    //获取每个时刻的大哥节点
    public  static Map<Integer, ArrayList<Integer>> GetFullServerNum(){
        //<边缘节点名，<时刻，理想期望>>
        TreeMap<Integer, TreeMap<Integer, Double>> loadreference = new TreeMap<>();
        //<时刻，<节点序列>
        HashMap<Integer, ArrayList<Integer>> serverloadfull = new HashMap<>();

        //遍历每一个时刻，第一次遍历是为了获得所有的值
        for (int i=0; i<CSVUtils.timeLength;i++){
            //遍历每一个客户节点
            for (int j=0; j<CSVUtils.M.get(i).size();j++){
                ArrayList<Integer> server = CSVUtils.GetAllServer(j);
                //每个客户节点给所有可达到边缘节点的平均负载
                double avgOfLoad = CSVUtils.M.get(i).get(j) / server.size();
                //遍历每一个客户节点连通的边缘节点
                for (int s:server){
                    if (!loadreference.containsKey(s)) {
                        TreeMap<Integer,Double> timeReference=new TreeMap<>();
                        timeReference.put(i,avgOfLoad);
                        loadreference.put(s, timeReference);
                    }
                    else{
//                        TreeMap<Integer,Double> timeReference=loadreference.get(s);
//                        timeReference.replace(i,loadreference.get(s).get(i),loadreference.get(s).get(i)+avgOfLoad);
//                        loadreference.replace(s,loadreference.get(s),timeReference);
                        if (loadreference.get(s).containsKey(i)){
                            double oldnum= loadreference.get(s).get(i);
                            loadreference.get(s).put(i,oldnum+avgOfLoad);
                        }else{
                            loadreference.get(s).put(i,avgOfLoad);
                        }
                    }
                }
            }
        }
        //将获取到的值排序
        for (int k:loadreference.keySet()){
            List<Map.Entry<Integer, Double>> sort= CSVUtils.MapSort(loadreference.get(k));
            for (int i=0;i<(int)(CSVUtils.timeLength*0.05);i++){
                int time=sort.get(i).getKey();
                if (!serverloadfull.containsKey(time)){
                    ArrayList<Integer> arrayList =new ArrayList<>();
                    arrayList.add(k);
                    serverloadfull.put(time,arrayList);
                }else{
                    ArrayList arrayList  = serverloadfull.get(time);
                    arrayList.add(k);
                    serverloadfull.replace(time,serverloadfull.get(time),arrayList);
                }
            }
        }
        return serverloadfull;
    }
    //计算成本
    public static Integer CountSource(Map<Integer ,Map<String, Map<String, Integer>>> allData) {
        /***
         * 这个是计算总成本的方法：
         * 思路： 获取每个时间节点的数据分配情况 ，比如0时刻 A客户节点给各个边缘服务器分配情况 A：<d1, 10><d2, 20><dn, 50>...
         * 那么我们遍历所有的客户节点分配情况 统计每个边缘节点被分配了都多少负载记录下来就得到了服务器当前时刻下的总负载
         *
         * 以下代码依次流程：
         * 循环所有时刻数据<时刻，<客户节点, <边缘节点，分配值>>>  循环变量 时刻，取时刻对应的数据
         *     循环查找客户节点 循环变量：客户节点，获取对应客户节点的分配情况数据
         *          循环检验边缘节点被分配情况 循环变量：边缘节点 获取数据为对应分配值
         *              保存当前边缘节点名称与对应被分配值（如果边缘节点已经被保存过，那么分其对应的被分配至就与此次被分配值进行累加）
         *  做输出
         *
         */


        //保存单个服务器节点的数据 map<服务器节点，list[]>  list里面依次序保存了每个时刻的节点对应负载
        Map<String, List<Integer>> serverAllTimeData = new HashMap<String, List<Integer>>();
        Set<Integer> timeSet = allData.keySet();

        //对每个时间点数据进行解包 循环处理每个客户节点对边缘节点的分配情况
        for(int nowTime : timeSet){
            //单个时间点的数据  <客户节点，<边缘节点, 分配值>>
            Map<String, Map<String, Integer>> oneTimeData = allData.get(nowTime);
            Set<String> oneClientData = oneTimeData.keySet();

            //从每个客户节点分配的情况分别计算每个服务节点的数据大小 对单个客户节点分配情况进行解包
            for(String client : oneClientData){
                //对应客户节点对边缘节点的分配情况 <边缘节点，分配值>
                Map<String, Integer> allServerData = oneTimeData.get(client);


                if(null == allServerData){ //如果客户节点需求为0就跳过他
                    continue;
                }
                Set<String> serverLoadData = allServerData.keySet();
                //对每个边缘节点被分配情况进行统计
                for(String server : serverLoadData){
                    //如果是第一次记录边缘节点的值
                    if(!serverAllTimeData.containsKey(server)){

                        //保存每个边缘节点每个时间点对应的负载总值 list中一个index保存的一个时间点下的总值
                        List<Integer> load = new ArrayList<>();

                        //这一步的处理是可能会出现第一个时刻未出现此边缘节点 在后面的时刻出现的话它的list就会同步不上nowTime 所以进行差值补齐
                        for(int i = 0; i <= nowTime; i++ ){
                            load.add(0);
                        }

                        //保存被分配值
                        load.set(nowTime, allServerData.get(server));
                        serverAllTimeData.put(server, load);
                    }else{
                        //当边缘节点已经被记录过 那么就直接继续累加他被分配的负载值
                        //同样的是为了避免中间未出现而出现时刻差 做补齐
                        for(int i = serverAllTimeData.get(server).size()-1; i < nowTime; i++ ){
                            serverAllTimeData.get(server).add(0);
                        }

                        //统计对应server节点的数据
                        serverAllTimeData.get(server).set(nowTime, serverAllTimeData.get(server).get(nowTime) + allServerData.get(server));
                    }

                }
            }
        }
        //上面完成了所有的数据记录 下面是打印
        //保存95分为时刻数据的和
        int allPrice = 0;

        // serverAllTimeData: <边缘节点，list[1时刻负载,2时刻负载...]>
        for(String oneServerAllData : serverAllTimeData.keySet()){
            System.out.println("-------------------------------------------------------------------------------------------");
            System.out.println("当前的服务器名称：" + oneServerAllData );
            System.out.println("未排序所有时刻的负载序列： <时刻：大小>");
            for(int timeIndex = 0; timeIndex < allData.size(); timeIndex++){
                System.out.print("<"+timeIndex + "：" + serverAllTimeData.get(oneServerAllData).get(timeIndex) + ">   ");
            }
            System.out.println("");
            System.out.println("排序后情况");
            //升序排列
            TreeMap<Integer, Integer> timeIndexToLoad = new TreeMap<Integer, Integer>();

            for(int timeIndex = 0; timeIndex < allData.size(); timeIndex++){
                timeIndexToLoad.put(timeIndex, serverAllTimeData.get(oneServerAllData).get(timeIndex));
            }
            List<Map.Entry<Integer, Integer>> timeIndexToLoadAsc =  CSVUtils.MapSort(timeIndexToLoad, false);
            int count95 = 0;
            int numOf95 = 0;
            for(Map.Entry<Integer, Integer> e: timeIndexToLoadAsc){
                if (count95 == indexOf95) {
                    //九十五分位数
                    numOf95 =  e.getValue();
                    System.out.println("");
                    allPrice += numOf95;
                }
                count95++;
                System.out.print("<"+e.getKey() + "：" + e.getValue() + ">   ");
            }
            System.out.println("95%分位向下取整数："+ numOf95);

        }
        System.out.println("=================================================================================================");
        System.out.println("总代价：" + allPrice);

        return allPrice;
    }
    //所有的结果集(结果包括两部分，一部分是这次遍历的数据，一部分是这个结果的总成本)
    public static List<Result> AllResults = new ArrayList<Result>();


    //将一个结果集存储到结果集list
    public static void StoreData(Map<Integer ,Map<String, Map<String, Integer>>> allData) {
        Result res = new Result();
        res.setCost(CountSource(allData));
        res.setData(allData);
        AllResults.add(res);
    }
    //将数据集转入为Map<ServerName, Server>
    public static Map<String, Server> In (Map<Integer ,Map<String, Map<String, Integer>>> allData) {
        Map<String, List<Integer>> serverAllTimeData = GetServerData(allData);
        Map<String, Server> res = new HashMap<>();
        // serverAllTimeData: <边缘节点，list[1时刻负载,2时刻负载...]>
        for (String serverName : serverAllTimeData.keySet()){
            int serverIndex =  Mp_2.get(serverName);
            int maxLoad = N.get(serverIndex);
            ArrayList<Integer> allClientIndex = GetAllClient(serverIndex);
            List<Integer> everyTimeLoad = serverAllTimeData.get(serverName);
            ArrayList<String> allClientName = new ArrayList<>();
            for (int i = 0; i < allClientIndex.size(); i++) {
                int clientIndex = allClientIndex.get(i);
                for (String clientName : Mp_1.keySet()) {
                    if (Mp_1.get(clientName) == clientIndex) {
                        allClientName.add(clientName);
                    }
                }
            }
            Server server = new Server(serverName,serverIndex, maxLoad, allClientName);
            //初始化其timeClientLoad
            FindServerLoad(allData, server);
            for (int i =0; i < everyTimeLoad.size(); i++){
                server.setCurLoad(i, everyTimeLoad.get(i));
            }
            res.put(serverName, server);
        }
        return res;
    }
    //为一个server初始化它的timeClientLoad
    public static void FindServerLoad(Map<Integer ,Map<String, Map<String, Integer>>> allData, Server server) {
        String findSeverName = server.getName();
        //最外层遍历时间
        for (Integer timeIndex : allData.keySet()) {
            Map<String, Map<String, Integer>> totalDis = allData.get(timeIndex);
            //第二层遍历客户节点
            for(String clientName : totalDis.keySet()) {
                Map<String, Integer> curClientDis =  totalDis.get(clientName);
                //第三层遍历客户节点所有的边缘节点的分配
                for (String serverName : curClientDis.keySet()) {

                    int thisClientDemand = curClientDis.get(serverName);
                    if (serverName.equals(findSeverName)) {
                        server.setTimeClientLoad(timeIndex, clientName, thisClientDemand);
                    }
                }
            }
        }
    }
    //把allData转化成serverAllData
    public static Map<String, List<Integer>> GetServerData(Map<Integer ,Map<String, Map<String, Integer>>> allData) {
        //保存单个服务器节点的数据 map<服务器节点，list[]>  list里面依次序保存了每个时刻的节点对应负载
        Map<String, List<Integer>> serverAllTimeData = new HashMap<String, List<Integer>>();
        Set<Integer> timeSet = allData.keySet();
        //对每个时间点数据进行解包 循环处理每个客户节点对边缘节点的分配情况
        for(int nowTime : timeSet){
            //单个时间点的数据  <客户节点，<边缘节点, 分配值>>
            Map<String, Map<String, Integer>> oneTimeData = allData.get(nowTime);
            Set<String> oneClientData = oneTimeData.keySet();

            //从每个客户节点分配的情况分别计算每个服务节点的数据大小 对单个客户节点分配情况进行解包
            for(String client : oneClientData){
                //对应客户节点对边缘节点的分配情况 <边缘节点，分配值>
                Map<String, Integer> allServerData = oneTimeData.get(client);


                if(null == allServerData){ //如果客户节点需求为0就跳过他
                    continue;
                }
                Set<String> serverLoadData = allServerData.keySet();
                //对每个边缘节点被分配情况进行统计
                for(String server : serverLoadData){
                    //如果是第一次记录边缘节点的值
                    if(!serverAllTimeData.containsKey(server)){

                        //保存每个边缘节点每个时间点对应的负载总值 list中一个index保存的一个时间点下的总值
                        List<Integer> load = new ArrayList<>();

                        //这一步的处理是可能会出现第一个时刻未出现此边缘节点 在后面的时刻出现的话它的list就会同步不上nowTime 所以进行差值补齐
                        for(int i = 0; i <= nowTime; i++ ){
                            load.add(0);
                        }

                        //保存被分配值
                        load.set(nowTime, allServerData.get(server));
                        serverAllTimeData.put(server, load);
                    }else{
                        //当边缘节点已经被记录过 那么就直接继续累加他被分配的负载值
                        //同样的是为了避免中间未出现而出现时刻差 做补齐
                        for(int i = serverAllTimeData.get(server).size()-1; i < nowTime; i++ ){
                            serverAllTimeData.get(server).add(0);
                        }

                        //统计对应server节点的数据
                        serverAllTimeData.get(server).set(nowTime, serverAllTimeData.get(server).get(nowTime) + allServerData.get(server));
                    }

                }
            }
        }
        return serverAllTimeData;
    }
    //根据某服务器名称返回该服务器的序号
    public static Integer GetServerIndex (String serverName) {
        for (String key : Mp_2.keySet()) {
            if (key.equals(serverName)) {
                return Mp_2.get(key);
            }
        }
        return -1;
    }
    //根据某客户端名称返回该客户端的序号
    public static Integer GetClientIndex (String clientName) {
        for (String key : Mp_1.keySet()) {
            if (key.equals(clientName)) {
                return Mp_1.get(key);
            }
        }
        return -1;
    }
    //判断两个List是否相同
    public static <E>boolean isListEqual(List<E> list1, List<E> list2) {
        for(int i =0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }
        return true;
    }
    //《时刻，《客户节点《边缘节点，负载》》
    public static Map<Integer ,Map<String, Map<String, Integer>>> Out(Map<String,Server> serverMap){
        //要输出的map
        Map<Integer ,Map<String, Map<String, Integer>>> out = new HashMap<>();
        //因为在循环里面判断存不存在键值对太麻烦了，
        // 由于时刻和客户节点是固定的
        //所以首先对out初始化
        for (int i=0;i<timeLength;i++){
            HashMap<String, Map<String, Integer>> clientmap=new HashMap<>();
            Iterator iterator= CSVUtils.Mp_1.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry entry = (Map.Entry) iterator.next();
                clientmap.put((String) entry.getKey(),new HashMap<String, Integer>());
            }
            out.put(i,clientmap);
        }
        //遍历每一个边缘节点
        for (Server server:serverMap.values()){
            String serverName = server.getName();
            //取出边缘节点，<时间，<客户节点，分配给这个边缘服务器的值>>
            //遍历每一个键值对
            //放到out的《时刻，《客户节点《边缘节点，负载》》中
            //out的时刻和客户节点都已经有了
            HashMap<Integer, HashMap<String, Integer>> timeClientLoad=server.getTimeClientLoad();
            for (int time : timeClientLoad.keySet()){
                for (String  client:timeClientLoad.get(time).keySet()){
                    out.get(time).get(client).put(serverName,timeClientLoad.get(time).get(client));
                }
            }
        }
        return out;
    }
}
