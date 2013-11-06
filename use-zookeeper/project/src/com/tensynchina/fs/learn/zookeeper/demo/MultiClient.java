package com.tensynchina.fs.learn.zookeeper.demo;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class MultiClient {
	public static void main(String args[]) throws InterruptedException{
        mutex = new Integer(-1);
		MultiClient app=new MultiClient();
		app.run();
	}
    protected static Integer mutex;
	public void run() throws InterruptedException{
		final String zkHost="192.168.1.15:2181";//,192.168.1.16:2181,192.168.1.18:2181";
		final String checkDir="/testRootPath";
		DeletePath(zkHost,checkDir);
		CreatePath(zkHost,checkDir);
		Listen(zkHost,checkDir);
//		Thread listener=new Thread(new Runnable() {
//			public void run() {
//				Listen(zkHost,checkDir);
//			}
//		});
//		listener.start();
		//TEST01(zkHost);
		//TEST02(zkHost);
		Thread.sleep(5*60*1000);
//		listener.interrupt();
		DeletePath(zkHost,checkDir);
	}
	private void DeletePath(String zkHost, String checkDir) {
		ZooKeeper zk=null;
		int timeout=5000;
		try{
			zk = new ZooKeeper(zkHost,timeout,new TEST02Watcher("DeletePath")); 
			zk.delete(checkDir, -1);
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
	private void CreatePath(String zkHost, String checkDir) {
		ZooKeeper zk=null;
		int timeout=5000;
		try{
			zk = new ZooKeeper(zkHost,timeout,new TEST02Watcher("CreatePath")); 
			zk.create(checkDir, checkDir.getBytes(),Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
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
	public void Listen(String zkHost,String checkDir){
		ZooKeeper zk=null;
		int timeout=5000;
		try{
			zk = new ZooKeeper(zkHost,timeout,null);//,new TEST02Watcher("Listener")); 
			zk.register(new TEST02Watcher("Listener"));
			Stat status=zk.exists(checkDir,true);
			System.out.println("目录节点状态：["+status+"]");
//			synchronized (mutex) {
//	            mutex.notify();
//	        }
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
	public void TEST02(String zkHost){
		ZooKeeper zk=null;
		int timeout=5000;
		try{
			// 创建一个与服务器的连接
			zk = new ZooKeeper(zkHost,timeout,new TEST02Watcher("TEST02"));
			
			// 创建一个子目录节点
			System.out.println("create /testRootPath/testChildPathOne"); 
			zk.create("/testRootPath/testChildPathOne", "testChildDataOne".getBytes(),
			  Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); 
			System.out.println("create /testRootPath/testChildPath3"); 
			zk.create("/testRootPath/testChildPath3", "testChildDataTwo".getBytes(), 
					  Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); 

			System.out.println("zk.getData /testRootPath"); 
			System.out.println(new String(zk.getData("/testRootPath",false,null))); 
			// 取出子目录节点列表
			System.out.println(zk.getChildren("/testRootPath",true)); 
			// 修改子目录节点数据
			zk.setData("/testRootPath/testChildPathOne","modifyChildDataOne".getBytes(),-1); 
			//System.out.println("目录节点状态：["+zk.exists("/testRootPath",true)+"]"); 
			// 创建另外一个子目录节点
			System.out.println("create /testRootPath/testChildDataTwo"); 
			zk.create("/testRootPath/testChildPathTwo", "testChildDataTwo".getBytes(), 
			  Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); 
			System.out.println(new String(zk.getData("/testRootPath/testChildPathTwo",true,null))); 
			// 删除子目录节点
			zk.delete("/testRootPath/testChildPath3",-1); 
			zk.delete("/testRootPath/testChildPathTwo",-1); 
			zk.delete("/testRootPath/testChildPathOne",-1); 
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

		public TEST02Watcher(String name){
			this.name=name;
		}
		
		/**
		 * 监控所有被触发的事件
		 */
		@Override
		public void process(WatchedEvent event) {			
			System.out.println(name+" 已经触发了" + event.getType() + "事件！"+event); 
		}
		
	}

}
