package com.huawei.java.main;

import java.io.IOException;
import java.util.*;

public class Main {
	public static void main(String[] args) throws IOException {
		//初始化工具类得各种参数，
		CSVUtils.Initialize(true);
		//结果集
		Map<Integer ,Map<String, Map<String, Integer>>> sortData = new HashMap<Integer ,Map<String, Map<String, Integer>>>();
		//分配每一个时间节点
		for(int i=0;i<CSVUtils.timeLength;i++){
			Map<String, Map<String, Integer>> oneTimeData = CSVUtils.loop(CSVUtils.M.get(i),i);
			sortData.put(i, oneTimeData);
		}
//		CSVUtils.OutPutFile(sortData);
//		CSVUtils.StoreData(sortData);

		//循环查优

		/***
		 * 优化结果集逻辑
		 *
		 *
		 *
		 */
		//初始化变量

		ArrayList<String> clientName = CSVUtils.GetClientName();
		ArrayList<String> serverName = CSVUtils.GetServerName();

		//初始化客户节点
		//<客户节点名， 边缘节点对象>
		Map<String, Client> clientMap = new HashMap<>();
		for(int i = 0; i < CSVUtils.Mp_1.size(); i++ ){
			ArrayList<String> allServer = new ArrayList<>();
			for(int a : CSVUtils.GetAllServer(i)){
				allServer.add(serverName.get(a));
			}
			Client newClient = new Client(i, clientName.get(i));
			newClient.setAllServerName(allServer);
			clientMap.put(clientName.get(i), newClient);
		}
		//<边缘节点名，边缘节点对象>
		Map<String, Server> serverMap = new HashMap<>();
		for(int i = 0; i < CSVUtils.Mp_2.size(); i++ ){
			ArrayList<String> allClient = new ArrayList<>();
			for(int a : CSVUtils.GetAllClient(i)){
				allClient.add(clientName.get(a));
			}
			Server newServer = new Server(serverName.get(i), i, CSVUtils.N.get(i), allClient);
			serverMap.put(serverName.get(i), newServer);
		}

		//剔除所有不连通的服务器
		ArrayList<String> delServer = new ArrayList<>();
		for(String server : serverMap.keySet()){
			if(null == serverMap.get(server).getAllClientName())
				delServer.add(server);
		}
		for(String server : delServer){
			serverMap.remove(server);
		}
		for(int TIME : sortData.keySet()){
			for(String server : serverMap.keySet()){
				serverMap.get(server).setTimeClientLoad(TIME);
			}
		}
		//解包数据并初始化实体对象 以TIME为单位 解包数据主要传给各个server 与client无关
		for(int TIME : sortData.keySet()){
			Map<String, Map<String, Integer>> oneClientData = sortData.get(TIME);
			for(String client : oneClientData.keySet()){
				Map<String, Integer> oneServerData = oneClientData.get(client);
				for(String server : oneServerData.keySet()){
					serverMap.get(server).setTimeClientLoad(TIME, client, oneServerData.get(server));
				}
			}
		}
		//记录大哥位的值<边缘节点名称， 值>
		Map<String, Integer> minBossLoad = new HashMap<>();
		for(String server: serverMap.keySet()){
			int a = serverMap.get(server).getMinBossLoad();
			minBossLoad.put(server, a);
		}




//		System.out.println(sortnum);
//		for(String server : serverMap.keySet()){
//			System.out.println("大哥值：" +serverMap.get(server).getMinBossLoad()+" 95值:"+ serverMap.get(server).getLoad_95());
//		}


		//看看95值和大哥值  按95分位值排序一下  从小到大
//		Map<Integer, String> sortServer_95 = new HashMap<>();
//		ArrayList<Integer> sortnum = new ArrayList<>();
//		for(String server : serverMap.keySet()){
//			int num_95 = serverMap.get(server).getLoad_95();
//			if(num_95 == 0){
//				while(sortServer_95.containsKey(num_95))
//					num_95++;
//			}else {
//				while(sortServer_95.containsKey(num_95))
//					num_95--;
//			}
//			sortnum.add(num_95);
//			sortServer_95.put(num_95, server);
//		}
		Map<Integer, String> sortServer_95 = new HashMap<>();
		ArrayList<Integer> sortnum = new ArrayList<>();
		for(String server : serverMap.keySet()){
			int num_95 = serverMap.get(server).getMinBossLoad();
			if(num_95 == 0){
				while(sortServer_95.containsKey(num_95))
					num_95++;
			}else {
				while(sortServer_95.containsKey(num_95))
					num_95--;
			}
			sortnum.add(num_95);
			sortServer_95.put(num_95, server);
		}
//		sortnum.sort(Comparator.naturalOrder());
		sortnum.sort(Comparator.reverseOrder());




		//调整优化 除最后一个服务器外每个服务器都做
		int allPrice = 0;
		ArrayList<String> noUseServer = new ArrayList<>();
//		for(String mainServer : serverMap.keySet()){
		for(int i = 0; i < sortServer_95.size(); i++){
			String mainServer = sortServer_95.get(sortnum.get(i));
			Server nowServer = serverMap.get(mainServer);
			noUseServer.add(mainServer);

			if(nowServer.getTimeClientLoad().size() == 0) continue;

			if(null == nowServer.getAllClientName()) continue;

			//首先获取95分位大小
			int num_95 = nowServer.getLoad_95();
//			if(i == 0){
//				num_95 = nowServer.getMaxLoad();
//			}
			System.out.println("服务器："+ nowServer.getName() +"  本次的 num_95 :" + num_95);
			allPrice += num_95;

			//循环处理每个时间点下各个小弟位数据  拉到95位大小
			for(int TIME = 0; TIME < CSVUtils.timeLength; TIME++){

				if(!nowServer.getTimeClientLoad().containsKey(TIME)) {
					nowServer.setTimeClientLoad(TIME);
				}
				if(nowServer.getCurLoad(TIME) < num_95){ //判断是否是小弟位 是才重调整
					//获取需要分配的值 mainserver的当前负载与95分位的差距
					int gapLoad =num_95 - nowServer.getCurLoad(TIME);
					//获取nowServer的所有联通客户节点并循环处理
					//保存的是<边缘节点，联通的客户节点列表>  注意这个步骤需要查验其是否是大哥时刻 是大哥时刻的都跳过
					Map<String, ArrayList<String>> serverClient = new HashMap<>();
					for(String ClientByNowServer : nowServer.getAllClientName()){
						//获取每个ClientByNowServer联通的边缘节点用来吸收负载
						for(String serverByClient : clientMap.get(ClientByNowServer).getAllServerName()){
							//如果是已处理过的服务器，跳过
							if(noUseServer.contains(serverByClient)) continue;

							//如果当前时刻下服务器未被此客户节点分配过负载 则跳过
							if(!serverMap.get(serverByClient).getTimeClientLoad().containsKey(TIME)) continue;
							if(!serverMap.get(serverByClient).getTimeClientLoad().get(TIME).containsKey(ClientByNowServer)) continue;
							// 用对应服务起的95分位来判断是否是大哥节点
//							if(serverMap.get(serverByClient).getLoad_95() < serverMap.get(serverByClient).getCurLoad(TIME))
//								continue;

							if(minBossLoad.get(serverByClient) <= serverMap.get(serverByClient).getCurLoad(TIME)){
								continue;
							}
							//如果已经存在了边缘节点 就往后保存对应的客户节点到list做对应
							if(serverClient.containsKey(serverByClient)){
								serverClient.get(serverByClient).add(ClientByNowServer);
							}else {
								serverClient.put(serverByClient, new ArrayList<>());
								serverClient.get(serverByClient).add(ClientByNowServer);
							}

						}
					}

					/***
					 *
					 *  零一吸收法
					 *
					 */
					//01 通吃
//					for(String server : serverClient.keySet()) {
//						for (String client_name : serverClient.get(server)) {
//							int load = gapLoad;
//							//判断被吸的边缘节点是否够吸 够吸吸满，不够的话有多少吸多少
//							if(serverMap.get(server).getTimeClientLoad(TIME, client_name) < load)
//								load = serverMap.get(server).getTimeClientLoad(TIME, client_name);
//							serverMap.get(server).setTimeClientLoad(TIME, client_name, serverMap.get(server).getTimeClientLoad(TIME, client_name) - load);
//							nowServer.setTimeClientLoad(TIME, client_name, nowServer.getTimeClientLoad(TIME, client_name) + load);
//							gapLoad -= load;
//							if (gapLoad == 0) break;
//						}
//						if (gapLoad == 0) break;
//					}


					/***
					 * 分权统治均衡
					 */

					//上面获取的serverClient 就保存了所有与MainServer联通的边缘节点 现在再他们之间平衡负载 计算权值做比值来分配
					//计算总权值
					int allWeight = 0;
					for(String server : serverClient.keySet()){
						if(!serverMap.get(server).getTimeClientLoad().containsKey(TIME)) continue;

						for(String client_name : serverClient.get(server)){
							allWeight += serverMap.get(server).getTimeClientLoad(TIME, client_name);
						}
					}
					//分权做比例分配
					for(String server : serverClient.keySet()){
						for(String client_name : serverClient.get(server)){
							double propor = (double) serverMap.get(server).getTimeClientLoad(TIME, client_name)/(double)allWeight;
							int load = (int)(propor * gapLoad);
							//如果太小了就算了吧
							if(load == 0) continue;
							//判断被吸的边缘节点是否够吸 够吸吸满，不够的话有多少吸多少
							if(serverMap.get(server).getTimeClientLoad(TIME, client_name) < load){
								load = serverMap.get(server).getTimeClientLoad(TIME, client_name);
							}
							serverMap.get(server).setTimeClientLoad(TIME, client_name, serverMap.get(server).getTimeClientLoad(TIME, client_name) - load);
							nowServer.setTimeClientLoad(TIME, client_name, nowServer.getTimeClientLoad(TIME, client_name) + load);
							System.out.println("调整了");
						}
					}
				}
			}
		}

//		System.out.println("总成本：" + allPrice);


		sortData = CSVUtils.Out(serverMap);
//
//		for(int key : sortData.keySet()){
//			CSVUtils.Check(sortData.get(key), key);
//		}

		CSVUtils.StoreData(sortData);

		/***
		 * 结束
		 *
		 *
		 *
		 */

//		//输出文件
		CSVUtils.OutPutFile(sortData);
	}

}

