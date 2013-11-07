package com.tensynchina.fs.learn.zookeeper.demo;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.event.DocumentEvent.EventType;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
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
		final String rootPath="/testRootPath";
		// 创建根节点
		CreatePath(zkHost,rootPath);
		
		// 监听
		final CountDownLatch latch=new CountDownLatch(1);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Listen(zkHost, rootPath,latch);
			}
		}).start();
		// 进行操作
		TEST03(zkHost,rootPath);
		latch.countDown();
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
		DeletePath(zkHost,rootPath);
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
		ZooKeeper zk=null;
		int timeout=5000;
		try{
			// 创建一个与服务器的连接
			zk = new ZooKeeper(zkHost,timeout,new TEST02Watcher("TEST02"));
			
			// 创建一个子目录节点
			System.out.println("create /testRootPath/testChildPathOne"); 
			zk.create("/testRootPath/testChildPathOne", "testChildDataOne".getBytes(),
			  Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
			
			Thread.sleep(1000);
			System.out.println("create /testRootPath/testChildPath3"); 
			zk.create("/testRootPath/testChildPath3", "testChildDataTwo".getBytes(), 
					  Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); 
			

			Thread.sleep(1000);
			System.out.println("zk.getData /testRootPath"); 
			System.out.println(new String(zk.getData("/testRootPath",false,null))); 
			// 取出子目录节点列表
			System.out.println(zk.getChildren("/testRootPath",true));
			

			Thread.sleep(1000);
			// 修改子目录节点数据
			zk.setData("/testRootPath/testChildPathOne","modifyChildDataOne".getBytes(),-1); 
			//System.out.println("目录节点状态：["+zk.exists("/testRootPath",true)+"]"); 
			// 创建另外一个子目录节点
			System.out.println("create /testRootPath/testChildDataTwo");
			

			Thread.sleep(1000);
			zk.create("/testRootPath/testChildPathTwo", "testChildDataTwo".getBytes(), 
			  Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); 
			System.out.println(new String(zk.getData("/testRootPath/testChildPathTwo",true,null))); 
			// 删除子目录节点

			Thread.sleep(1000);
			zk.delete("/testRootPath/testChildPath3",-1);

			Thread.sleep(1000);
			zk.delete("/testRootPath/testChildPathTwo",-1);
			
			Thread.sleep(1000);
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
					zk.getChildren(event.getPath(), true);
					out("process ["+name+"] children "+zk.getChildren(event.getPath(), false));
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

}
