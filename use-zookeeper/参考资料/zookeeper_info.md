
#网络摘录
----------
###### zookeeper使用和原理探究（一）
[http://www.blogjava.net/BucketLi/archive/2010/12/21/341268.html]

* zookeeper 是hadoop项目中的一个子项目，并且根据google发表的《The Chubby lock service for loosely-coupled distributed systems》论文来实现的
* 其中第一个端口用来集群成员的信息交换，第二个端口是在leader挂掉时专门用来进行选举leader所用 (zookeeper配置文件中的端口)
* 当我们起第二个zookeeper实例后，leader将会被选出，从而一致性服务开始可以使用，这是因为3台机器只要有2台可用就可以选出leader并且对外提供服务(2n+1台机器，可以容n台机器挂掉)。
* zookeeper使用了一个类似文件系统的树结构，数据可以挂在某个节点上，可以对这个节点进行删改。另外我们还发现，当改动一个节点的时候，集群中活着的机器都会更新到一致的数据。 
* zookeeper的文件系统特点
(1)     每个节点在zookeeper中叫做znode,并且其有一个唯一的路径标识，如/SERVER2节点的标识就为/APP3/SERVER2
(2)     Znode可以有子znode，并且znode里可以存数据，但是EPHEMERAL类型的节点不能有子节点
(3)     Znode中的数据可以有多个版本，比如某一个路径下存有多个数据版本，那么查询这个路径下的数据就需要带上版本。
(4)     znode 可以是临时节点，一旦创建这个 znode 的客户端与服务器失去联系，这个 znode 也将自动删除，Zookeeper 的客户端和服务器通信采用长连接方式，每个客户端和  服务器通过心跳来保持连接，这个连接状态称为 session，如果 znode 是临时节点，这个 session 失效，znode 也就删除了
(5)     znode 的目录名可以自动编号，如 App1 已经存在，再创建的话，将会自动命名为 App2 
(6)     znode 可以被监控，包括这个目录节点中存储的数据的修改，子节点目录的变化等，一旦变化可以通知设置监控的客户端，这个功能是zookeeper对于应用最重要的特性，通过这个特性可以实现的功能包括配置的集中管理，集群管理，分布式锁等等。  

以下为主要的API使用和解释
'''
//创建一个Zookeeper实例，第一个参数为目标服务器地址和端口，第二个参数为Session超时时间，第三个为节点变化时的回调方法
ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 500000,new Watcher() {
           // 监控所有被触发的事件
             public void process(WatchedEvent event) {
           //dosomething
           }
      });
//创建一个节点root，数据是mydata,不进行ACL权限控制，节点为永久性的(即客户端shutdown了也不会消失)
zk.create("/root", "mydata".getBytes(),Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

//在root下面创建一个childone znode,数据为childone,不进行ACL权限控制，节点为永久性的
zk.create("/root/childone","childone".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);

//取得/root节点下的子节点名称,返回List<String>
zk.getChildren("/root",true);

//取得/root/childone节点下的数据,返回byte[]
zk.getData("/root/childone", true, null);

//修改节点/root/childone下的数据，第三个参数为版本，如果是-1，那会无视被修改的数据版本，直接改掉
zk.setData("/root/childone","childonemodify".getBytes(), -1);

//删除/root/childone这个节点，第二个参数为版本，－1的话直接删除，无视版本
zk.delete("/root/childone", -1);
      
//关闭session
zk.close();
'''


----------
###### 参考列表
* zookeeper使用和原理探究（一）
[http://www.blogjava.net/BucketLi/archive/2010/12/21/341268.html]
* ZooKeeper Getting Started Guide 
[http://zookeeper.apache.org/doc/current/zookeeperStarted.html]
* [http://www.ibm.com/developerworks/cn/opensource/os-cn-zookeeper/]
* [http://hadoop.apache.org/zookeeper/docs/current/]
* [http://rdc.taobao.com/team/jm/archives/448]