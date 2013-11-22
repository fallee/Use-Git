package com.tensynchina.fs.learn.zookeeper.demo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;

public class MultiClient {
	public static void main(String args[]) throws InterruptedException{
        mutex = new Integer(-1);
		MultiClient app=new MultiClient();
		app.run();
	}
    protected static Integer mutex;
	private ZooKeeper zookeeper = null;
	public void run() throws InterruptedException{
		final String zkHost="192.168.1.15:2181";//,192.168.1.16:2181,192.168.1.18:2181";
//		final String rootPath="/testRootPath";
		try{
//			try {
//				connection(zkHost);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			// 创建根节点
//			CreatePath(zkHost,rootPath);
			
			TEST02(zkHost);
			// 监听
//			final CountDownLatch latch=new CountDownLatch(1);
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					Listen(zkHost, rootPath,latch);
//				}
//			}).start();
			// 进行操作
//			TEST03(zkHost,rootPath);// 测试添加删除节点
//			TEST04(rootPath);// 测试命名服务
//			TEST05(rootPath);// 测试数据更新  抛异常了 。。。 节点被删除的时候，不能再尝试获取节点内容
//			TEST06(rootPath);// 测试目录数据更新
			// ACL
			
			//Thread.sleep(1000);
//			latch.countDown();
			// 关闭监听
	//		DeletePath(zkHost,checkDir);
	//		CreatePath(zkHost,checkDir);
	//		Listen(zkHost,checkDir);
	////		Thread listener=new Thread(new Runnable() {
	////			public void run() {
	////				Listen(zkHost,checkDir);
	////			}
	////		});
	////		listener.start();
	//		//TEST01(zkHost);
	//		//TEST02(zkHost);
	//		Thread.sleep(5*60*1000);
	////		listener.interrupt();
	//		DeletePath(zkHost,checkDir);
			// 删除根节点
//			DeletePath(zkHost,rootPath);
		}finally{
			disconnect();
		}
	}
	
	private void connection(String zkHost) throws IOException {
		int timeout=50000;
		zookeeper  = new ZooKeeper(zkHost,timeout,new TEST02Watcher("MultiClient"));
	}

	private void disconnect() throws InterruptedException {
		if(zookeeper!=null){
			zookeeper.close();
		}
	}
	/**
	 * 测试目录监测（配置管理）
	 * @param rootPath
	 * @throws InterruptedException
	 */
	private void TEST06(String rootPath) throws InterruptedException{
		final ZooKeeper zk=zookeeper;
		String path=rootPath+"/configTree";
		try {
			Watcher nWatcher=new Watcher(){
				@Override
				public void process(WatchedEvent event) {
					out("TEST06 process "+event);
					try {

						if(zk.exists(event.getPath(), false)!=null){
							List<String> cs=zk.getChildren(event.getPath(), this); // 获取数据并继续监测
							out("TREE_WATCHER TEST06 "+event.getPath()+" "+cs+" "+event);
						}
						if(event.getType().equals(Watcher.Event.EventType.NodeDataChanged)){
								if(zk.exists(event.getPath(), false)!=null){
									String data=new String(zk.getData(event.getPath(), this, null)); // 获取数据并继续监测
									out("TREE_WATCHER DataChanged "+event.getPath()+" "+data+" "+event);
								}else{
									out("TREE_WATCHER DataChanged "+event.getPath()+" is null "+event);
								}
						}else if(event.getType().equals(Watcher.Event.EventType.NodeDeleted)){
							out("TREE_WATCHER NodeDeleted "+event.getPath()+" NodeDeleted "+event);
						}else{
							out("TREE_WATCHER "+event.getPath()+" "+event);
						}
					} catch (KeeperException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			String name = zk.create(path, path.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			out("created node"+name);
			zk.getChildren(path,nWatcher); // 添加监测
			name=zk.create(path+"/config01", "some info by init".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			out("created node"+name);
			Stat s = zk.setData(path+"/config01", "some info by update".getBytes(),-1);
			out("changed data "+path+"/config01 "+s);
			
			zk.setData(path, "abcdefg".getBytes(), -1);
			out("set node"+name);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 测试内容监测 （配置管理）
	 * 
	 * - 已存在的节点才能监测
	 * - getData 可以监测到数据变更和节点删除
	 * @param rootPath
	 * @throws InterruptedException
	 */
	private void TEST05(String rootPath) throws InterruptedException{
		final ZooKeeper zk=zookeeper;
		String path=rootPath+"/config";
		try {
			Watcher nWatcher=new Watcher(){
				@Override
				public void process(WatchedEvent event) {
					if(event.getType().equals(Watcher.Event.EventType.NodeDataChanged)){
						try {
							if(zk.exists(event.getPath(), false)!=null){
								String data=new String(zk.getData(event.getPath(), this, null)); // 获取数据并继续监测
								out("NODE_WATCHER DataChanged "+event.getPath()+" "+data+" "+event);
							}else{
								out("NODE_WATCHER DataChanged "+event.getPath()+" is null "+event);
							}
						} catch (KeeperException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else if(event.getType().equals(Watcher.Event.EventType.NodeDeleted)){
						out("NODE_WATCHER NodeDeleted "+event.getPath()+" NodeDeleted "+event);
					}else{
						out("NODE_WATCHER "+event.getPath()+" "+event);
					}
				}
			};
			String name = zk.create(path, path.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			zk.getData(path,nWatcher,null); // 添加监测
			out("create node"+name);
			zk.setData(path, "abcdefg".getBytes(), -1);
			out("set node"+name);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 测试唯一名称 (统一命名服务）
	 * 
	 * - 节点后缀自动递增 namepath_0000000003
	 * - 节点删除后，不会有新增节点使用已删除节点的编号
	 * - 目录删除后节点将重新开始编号
	 * 
	 * @param rootPath
	 * @throws InterruptedException
	 */
	private void TEST04(String rootPath) throws InterruptedException {
		ZooKeeper zk=zookeeper;
		String path=rootPath+"/namepath_";
		String name;
		try {
			name = zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			out("create node"+name);
			name=zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			out("create node"+name);
			zk.delete(name, -1); // 删除上一个节点
			out("delete node"+name);
			name=zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			out("create node"+name);
			name=zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			out("create node"+name);
			name=zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			out("create node"+name);
			name=zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			out("create node"+name);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 基本操作测试
	public void TEST03(String zkHost,String path){
		ZooKeeper zk=null;
		int timeout=5000;
		try{
			// 创建一个与服务器的连接
			zk = new ZooKeeper(zkHost,timeout,new TEST02Watcher("TEST02"));
			
			// 创建一个子目录节点
			tryCreatePath(zk, path+"/testChildPathOne", "testChildPathOne", CreateMode.PERSISTENT);
			Thread.sleep(1000);
			tryCreatePath(zk, path+"/testChildDataTwo", "testChildDataTwo", CreateMode.PERSISTENT);
			tryCreatePath(zk, path+"/testChildData3", "testChildData3", CreateMode.PERSISTENT);
			Thread.sleep(1000);
			tryDeleteTree(zk, path+"/testChildPathOne");
			
		}catch(Exception e){
			System.out.println("TEST02 "+e);
			e.printStackTrace();
		}finally{
			if(zk!=null){
				// 关闭连接
				 try {
					zk.close();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
		
	}
	public void TEST02(String zkHost){
		int timeout=5000;
		ZooKeeper zk=null;
		
		try{
			try{
				out("== 创建一个与服务器的连接 ==");
				final ZooKeeper zk0= zk = new ZooKeeper(
						zkHost, // 服务器地址 ，多个地址用逗号分隔 192.168.1.16:2181,192.168.1.18:2181
						timeout, // 超时时间
						new NothingWatcher()
						);
				out("== 监听子节点变动 ==");
				zk0.getChildren("/", new Watcher() { 
					public void process(WatchedEvent event) {
						try {
							List<String> cs = zk0.getChildren(event.getPath(), this); // 持续监听
							out("ROOT_WATCHER CHILDREN CHANGED "+event.getPath()+" "+event.getType()+" "+event.getState()+" "+cs);
						} catch (KeeperException e) {
							out("Exception in ROOT_WATCHER "+e.getMessage());
						} catch (InterruptedException e) {
							out("Exception in ROOT_WATCHER "+e.getMessage());
						}
					}
				});
				
				// 创建节点
				String rNode="/TEST02";
				out("BEGIN 创建测试根节点 "+rNode);
				String rNoder=zk.create(
						rNode, // 节点路径 从/开始
						"SOMEDATA".getBytes(), // 数据内容
						Ids.OPEN_ACL_UNSAFE, // 访问权限
						CreateMode.PERSISTENT // 节点类型
						);
				out("END 创建测试根节点  "+rNode+" "+rNoder);

				out("== 监听测试子节点变动 =="); 
				zk0.getChildren(rNode, new Watcher() { 
					public void process(WatchedEvent event) {
						try {
							List<String> cs = zk0.getChildren(event.getPath(), this); // 持续监听
							out("TEST_WATCHER CHILDREN CHANGED "+event.getPath()+" "+event.getType()+" "+event.getState()+" "+cs);
						} catch (KeeperException e) {
							out("Exception in TEST_WATCHER "+e.getMessage());
						} catch (InterruptedException e) {
							out("Exception in TEST_WATCHER "+e.getMessage());
						}
					}
				});
				// 监测只对子目录有效，不能监测子目录的下一级变动
				
				String tPN01="/TEST02/PERSISTENT_NODE_01";
				out("BEGIN 创建一个持久的数据节点 "+tPN01);
				String tPN01r=zk.create(
						tPN01, // 节点路径 从/开始
						"SOMEDATA".getBytes(), // 数据内容
						Ids.OPEN_ACL_UNSAFE, // 访问权限
						CreateMode.PERSISTENT // 节点类型
						);
				out("END 创建一个持久的数据节点 "+tPN01+" "+tPN01r);
				try{
					out("BEGIN 重复创建上一个持久的数据节点 "+tPN01);
					tPN01r=zk.create(
							tPN01, // 节点路径 从/开始
							"SOMEDATA".getBytes(), // 数据内容
							Ids.OPEN_ACL_UNSAFE, // 访问权限
							CreateMode.PERSISTENT // 节点类型
							);
					out("END 重复创建上一个持久的数据节点 "+tPN01+" "+tPN01r);
				}catch(KeeperException e){
					out("Exception in Create Node "+e.getMessage());
				}
				// 非列队节点重复创建会抛出异常

				String tPSN01="/TEST02/PERSISTENT_SEQUENTIAL_NODE_01";
				out("BEGIN 创建一个持久的队列节点 "+tPN01);
				String tPSN01r=zk.create(
						tPSN01, // 节点路径 从/开始
						"SOMEDATA".getBytes(), // 数据内容
						Ids.OPEN_ACL_UNSAFE, // 访问权限
						CreateMode.PERSISTENT_SEQUENTIAL // 节点类型
						);
				out("END 创建一个持久的队列节点 "+tPSN01+" "+tPSN01r);
				
				out("BEGIN 重复创建上一个持久的队列节点 "+tPSN01);
				tPSN01r=zk.create(
						tPSN01, // 节点路径 从/开始
						"SOMEDATA".getBytes(), // 数据内容
						Ids.OPEN_ACL_UNSAFE, // 访问权限
						CreateMode.PERSISTENT_SEQUENTIAL // 节点类型
						);
				out("END 重复创建上一个持久的队列节点 "+tPSN01+" "+tPSN01r);
				
				tPSN01="/TEST02/PERSISTENT_SEQUENTIAL_NODE_02";
				out("BEGIN 创建一个不同名称的持久的队列节点 "+tPSN01);
				tPSN01r=zk.create(
						tPSN01, // 节点路径 从/开始
						"SOMEDATA".getBytes(), // 数据内容
						Ids.OPEN_ACL_UNSAFE, // 访问权限
						CreateMode.PERSISTENT_SEQUENTIAL // 节点类型
						);
				out("END 创建一个不同名称的持久的队列节点 "+tPSN01+" "+tPSN01r);
				
				zk.delete(tPSN01r, -1);
				out("END 删除列队节点 "+tPSN01r);
				tPSN01r=zk.create(
						tPSN01, // 节点路径 从/开始
						"SOMEDATA".getBytes(), // 数据内容
						Ids.OPEN_ACL_UNSAFE, // 访问权限
						CreateMode.PERSISTENT_SEQUENTIAL // 节点类型
						);
				out("END 重复创建上一个持久的队列节点 "+tPSN01+" "+tPSN01r);
				
				// 列队数据在名称后面自动增加10位数字序列
				// 列队数据允许使用已存在的名称创建节点
				// 列队节点编号不会因为删除某个节点而重新计算
				
				out("== 临时数据测试 ==");
				out("创建独立连接");
				final ZooKeeper zk02 = new ZooKeeper(zkHost, 5000, new NothingWatcher());
				String tcr01="";
				String tcr02="";
				try{
					tcr01=zk02.create("/TEST02/SUB_EPHEMERAL_SEQUENTIAL", "SOMEDATA".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
					out("创建临时数据节点 "+tcr01);
					try{
						out("BEGIN 尝试创建临时子节点 "+tcr01+"/SUB01");
						zk02.create(tcr01+"/SUB01", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
					}catch(KeeperException e){
						out("Exception in create sub node for ephemeral node "+e.getMessage());
					}
					
					
					tcr02=zk02.create("/TEST02/SUB_PERSISTENT_SEQUENTIAL", "SOMEDATA".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
					out("创建持久数据节点 "+tcr02);
					try{
						out("BEGIN 尝试创建持久序列节点的子节点 "+tcr02+"/SUB01");
						zk02.create(tcr02+"/SUB01", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						out("END 尝试创建持久序列节点的子节点 "+tcr02+"/SUB01");
					}catch(KeeperException e){
						out("Exception in create sub node for ephemeral node "+e.getMessage());
					}
				}finally{
					out("关闭独立连接");
					zk02.close();
				}
				
				out("检查临时数据节点 "+zk.exists(tcr01, false)); 
				out("检查持久数据节点 "+zk.exists(tcr02, false));
				// 临时数据节点消失
				
				out("== 临时队列测试 ==");

				out("创建独立连接");
				final ZooKeeper zk03 = new ZooKeeper(zkHost, 5000, new NothingWatcher());
				String tcr03="";
				String tcr04="";
				try{
					tcr03=zk03.create("/TEST02/SUB_EPHEMERAL_SEQUENTIAL", "SOMEDATA".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
					out("创建临时数据节点 "+tcr03);
					tcr04=zk03.create("/TEST02/SUB_PERSISTENT_SEQUENTIAL", "SOMEDATA".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
					out("创建持久数据节点 "+tcr04);
				}finally{
					out("关闭独立连接");
					zk03.close();
				}
				
				out("检查临时数据节点 "+zk.exists(tcr03, false)); 
				out("检查持久数据节点 "+zk.exists(tcr04, false));
				

				// 临时列队节点的编号不会因为删除而重新计算
				// 不论临时还是持久的队列序号在同级目录下都是在一条线上累计的 
				
				out("== 测试ACL ==");
				
				zk0.create("/TEST02/READONLY_NODE", "SOMEDATA".getBytes(), Ids.READ_ACL_UNSAFE, CreateMode.PERSISTENT);
				out("END 创建只读节点 /TEST02/READONLY_NODE");
				out("BEGIN 创建者尝试更新 /TEST02/READONLY_NODE");
				try{
					zk0.setData("/TEST02/READONLY_NODE","UPDATA".getBytes(),-1);
					out("END 创建者尝试更新 /TEST02/READONLY_NODE");
				}catch(KeeperException e){
					out("Exception in Change read only Node "+e.getMessage());
				}
				// 创建者也不能更新只读数据，只能删除
				out("ZK "+zk); //ZK State:CONNECTED Timeout:5000 sessionid:0x1420d717c8b0185 local:/192.168.1.56:3566 remoteserver:slave2/192.168.1.15:2181 lastZxid:8589936733 xid:30 sent:30 recv:43 queuedpkts:0 pendingresp:1 queuedevents:0
				out("ANYONE_ID_UNSAFE "+Ids.ANYONE_ID_UNSAFE); // 'world,'anyone
				out("AUTH_IDS "+Ids.AUTH_IDS); // 'auth,'
				out("CREATOR_ALL_ACL "+Ids.CREATOR_ALL_ACL); // [31,s{'auth,'}
				out("OPEN_ACL_UNSAFE "+Ids.OPEN_ACL_UNSAFE); // [31,s{'world,'anyone}
				out("READ_ACL_UNSAFE "+Ids.READ_ACL_UNSAFE); // [1,s{'world,'anyone}
				
				// 附加身份信息，发送创建一个所有者才能更改的节点
				// 新建连接 使用相同的身份信息，验证数据是否可以更新成功
				String auth_type = "digest";//digest";
				String auth = "userabckey";
				zk.addAuthInfo(auth_type, auth.getBytes());
				out("ADD AUTH INFO ");
				zk.create("/TEST02/AUTH_TEST", "some private data".getBytes(), Ids.CREATOR_ALL_ACL, CreateMode.EPHEMERAL);
				out("CREATE DATANODE /TEST02/AUTH_TEST");
				try{
					ZooKeeper zk04=new ZooKeeper(zkHost, 5000, new NothingWatcher());
					out("BEGIN TRY GET DATA");
					try{
						out("END TRY GET DATA "+new String(zk04.getData("/TEST02/AUTH_TEST", false,null)));
					}catch(KeeperException e){
						out("Exception in TRY AUTH "+e.getMessage());
					}
					zk04.addAuthInfo(auth_type, auth.getBytes());
					out("ADD AUTH INFO");
					out("BEGIN TRY GET DATA BY AUTH");
					out("END TRY GET DATA  BY AUTH "+new String(zk04.getData("/TEST02/AUTH_TEST", false,null)));
					zk04.close();
				}catch(KeeperException e){
					out("Exception in TRY AUTH "+e.getMessage());
				}
				// 附加了相同身份信息的可以存取数据
				// 临时节点不能创建子节点
				
				out("== 测试数据监听 ==");
				final ZooKeeper zk05=zk;
				zk.exists("/TEST02/UNExistsNode", new Watcher() {
					ZooKeeper _zk=null;
					String path="";
					{
						_zk=zk05;
						path="/TEST02/UNExistsNode";
					}
					public void process(WatchedEvent event) {
						out("ON UNExistsNode Event Triggered "+event);
						try {
//							if(event.getType().equals(EventType.NodeCreated)){
//								byte[] d = _zk.getData(path, new Watcher(){
//										public void process(WatchedEvent event) {
//											out("ON UNExistsNode DATA Changed "+event);
//										}
//									}, null);
//								String data="NULL";
//								if(d!=null){
//									data=new String(d);
//								}
//								out("watch data "+path+" "+data);
//							}
							_zk.exists(path, this);
						} catch (KeeperException e) {
							out("KeeperException in Watch UNExistsNode "+e.getMessage());
						} catch (InterruptedException e) {
							out("InterruptedException in Watch UNExistsNode "+e.getMessage());
						} 
					}
				});
				out("添加监听 /TEST02/UNExistsNode ");
				out("BEGIN 创建节点 /TEST02/UNExistsNode ");
				zk.create("/TEST02/UNExistsNode", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				out("END 创建节点 /TEST02/UNExistsNode ");
				Thread.sleep(100); // 因为是异步操作 如果没有延迟 可能监听不到下一个事件
				out("BEGIN 更改节点数据 /TEST02/UNExistsNode ");
				zk.setData("/TEST02/UNExistsNode", "TEST".getBytes(), -1);
				out("END 更改节点数据 /TEST02/UNExistsNode ");
				// exists 的watcher 可以监听节点的创建,数据变更和删除操作
				
				// 数据版本测试
				{
					String p="/TEST02/DataVersion";
					out("数据版本 "+p);
					zk.create(p,"TEST_V01".getBytes(),Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
					Stat s=new Stat();
					byte[] d=zk.getData(p, false, s);
					String r="[EMPTY]";
					if(d!=null){
						r=new String(d);
					}
					out("GET 01 "+p+" "+r+" version:"+s.getVersion()+" stat:"+s);
					zk.setData(p, "TEST_V02".getBytes(),s.getVersion());
					d=zk.getData(p, false, s);
					r="[EMPTY]";
					if(d!=null){
						r=new String(d);
					}
					out("GET 02 "+p+" "+r+" version:"+s.getVersion()+" stat:"+s);
					try{
						zk.setData(p, "TEST_V03".getBytes(),s.getVersion()-1);
					}catch(KeeperException e){
						out("SET 02 DATA Failed "+p+" version "+(s.getVersion()-1)+" Exception "+e);
					}
					try{
						zk.setData(p, "TEST_V03".getBytes(),s.getVersion()+1);
					}catch(KeeperException e){
						out("SET 02 DATA Failed "+p+" version "+(s.getVersion()+1)+" Exception "+e);
					}
					zk.setData(p, "TEST_V03".getBytes(),s.getVersion());
					out("SET 03 DATA "+p+" last version: "+s.getVersion());
					
					d=zk.getData(p, false, s);
					r="[EMPTY]";
					if(d!=null){
						r=new String(d);
					}
					out("GET 03 "+p+" "+r+" version:"+s.getVersion()+" stat:"+s);
					try{
						zk.delete(p, 1);//s.getVersion());// 只能使用最后的版本号进行删除
						out("DELETE 03 "+p+" "+1);
					}catch(KeeperException e){
						out("DELETE 03 DATA Failed "+p+" Exception "+e);
					}
					zk.delete(p, -1);
					out("DELETE 03 "+p+" -1");
					s=new Stat();
					try{
						d=zk.getData(p, false, s); // KeeperErrorCode = BadVersion for /TEST02/DataVersion
						r="[EMPTY]";
						if(d!=null){
							r=new String(d);
						}
						out("GET 04 "+p+" "+r+" version:"+s.getVersion()+" stat:"+s);
					}catch(KeeperException e){
						out("GET 04 DATA Failed "+p+" Exception "+e);
					}
					
				}
				
				
				// 多次监听会触发几次
				{
					
				}

			}catch(Exception e){
				System.out.println("TEST02 "+e);
				e.printStackTrace();
			}finally{
				if(zk!=null){
					out("== 清理环境 删除所有测试节点 /TEST02 ==");
					tryDeleteTree(zk, "/TEST02");
				}
			}
			 
		}catch(Exception e){
			System.out.println("TEST02 "+e);
			e.printStackTrace();
		}finally{
			if(zk!=null){
				// 关闭连接
				 try {
					zk.close();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
		
	}
	protected class TEST02Watcher implements Watcher{
		private String name;
		private ZooKeeper zk=null;
		public TEST02Watcher(String name){
			this.name=name;
		}
		
		public void setZk(ZooKeeper zk) {
			this.zk=zk;
		}

		/**
		 * 监控所有被触发的事件
		 */
		@Override
		public void process(WatchedEvent event) {			
			System.out.println(name+" 已经触发了" + event.getType() + "事件！"+event);
			if(zk!=null&&event.getPath()!=null&&event.getType().toString().equals("NodeChildrenChanged")){
				try {
					List<String> cs=zk.getChildren(event.getPath(), true);
					//cs=zk.getChildren(event.getPath(), true); //KeeperErrorCode = ConnectionLoss for /testRootPath
					out("process ["+name+"] children "+event.getPath()+" "+cs);//zk.getChildren(event.getPath(), false));// exception...
				} catch (KeeperException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	protected class NothingWatcher implements Watcher{
		@Override
		public void process(WatchedEvent arg0) {
			// nothing to do
		}
		
	}
	private void DeletePath(String zkHost, String path) {
		ZooKeeper zk=null;
		int timeout=5000;
		try{
			zk = new ZooKeeper(zkHost,timeout,new TEST02Watcher("DeletePath")); 
			tryDeleteTree(zk,path);
		}catch(Exception e){
			System.out.println("DeletePath "+e);
			e.printStackTrace();
		}finally{
			if(zk!=null){
				// 关闭连接
				 try {
					zk.close();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}
	private String tryCreatePath(ZooKeeper zk, String path,String data,CreateMode mode) throws KeeperException, InterruptedException{
		out("try create path "+path);
		Stat s=zk.exists(path, false);
		if(s==null){
			String r = zk.create(path, data.getBytes(),Ids.OPEN_ACL_UNSAFE,mode);
			out("created path "+path+" "+r);
			return r;
		}else{
			out("path ("+path+") is exists: "+s);
			return null;
		}
	}
	private void tryDeleteTree(ZooKeeper zk, String path) throws KeeperException, InterruptedException {
		out("try delete path "+path);
		Stat s=zk.exists(path, false);
		if(s==null){
			out("path ("+path+") is not exists: "+s);
			return ;
		}
		List<String> cs = zk.getChildren(path, false);
		if(cs!=null){
			for(String c:cs){
				tryDeleteTree(zk,path+"/"+c);
			}
		}
		zk.delete(path, -1);
		out("deleted path "+path);
	}
	private static void out(String msg){
		System.out.println(msg);
	}
	private void CreatePath(String zkHost, String path) {
		ZooKeeper zk=null;
		int timeout=5000;
		try{
			zk = new ZooKeeper(zkHost,timeout,new TEST02Watcher("CreatePath")); 
			tryCreatePath(zk,path,path,CreateMode.PERSISTENT);
		}catch(Exception e){
			System.out.println("CreatePath "+e);
			e.printStackTrace();
		}finally{
			if(zk!=null){
				// 关闭连接
				 try {
					zk.close();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}
	// 持续监测子节点变更
	public void Listen(String zkHost,String path,CountDownLatch latch){
		ZooKeeper zk=null;
		int timeout=5000;
		try{
			
			zk = new ZooKeeper(zkHost,timeout,null);//,new TEST02Watcher("Listener")); 
			TEST02Watcher w=new TEST02Watcher("Listener");
			w.setZk(zk);
			zk.register(w);
//			Stat status=zk.exists(path,true); // 监听目录变更
//			System.out.println("目录节点状态：["+status+"]");
			zk.getChildren(path, true); // 监听节点变更
//			zk.getData(path, true,null); // 监听数据变更
			latch.await();
		}catch(Exception e){
			System.out.println("Listen "+e);
			e.printStackTrace();
		}finally{
			if(zk!=null){
				// 关闭连接
				 try {
					zk.close();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}

}
