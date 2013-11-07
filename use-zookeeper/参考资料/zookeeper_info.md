
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

* 配置管理 *
集中的配置管理中心，应对不同的应用集群对于共享各自配置的需求，并且在配置变更时能够通知到集群中的每一个机器。

比如将APP1的所有配置配置到/APP1 znode下。

APP1所有机器一启动就对/APP1这个节点进行监控(zk.exist("/APP1",true)),
并且实现回调方法Watcher。

那么在zookeeper上/APP1 znode节点下数据发生变化的时候，
每个机器都会收到通知，Watcher方法将会被执行，
那么应用再取下数据即可(zk.getData("/APP1",false,null))。

* 集群管理 *
应用集群中，我们常常需要让每一个机器知道集群中（或依赖的其他某一个集群）哪些机器是活着的，并且在集群机器因为宕机，网络断链等原因能够不在人工介入的情况下迅速通知到每一个机器。

在zookeeper服务器端有一个znode叫/APP1SERVERS,
那么集群中每一个机器启动的时候都去这个节点下创建一个EPHEMERAL类型的节点，
比如server1创建/APP1SERVERS/SERVER1(可以使用ip,保证不重复)，
server2创建/APP1SERVERS/SERVER2，
然后SERVER1和SERVER2都watch /APP1SERVERS这个父节点，
那么也就是这个父节点下数据或者子节点变化都会通知对该节点进行watch的客户端。

因为EPHEMERAL类型节点有一个很重要的特性，
就是客户端和服务器端连接断掉或者session过期就会使节点消失，
那么在某一个机器挂掉或者断链的时候，其对应的节点就会消失，
然后集群中所有对/APP1SERVERS进行watch的客户端都会收到通知，
然后取得最新列表即可。


另外有一个应用场景就是集群选master,一旦master挂掉能够马上能从slave中选出一个master,
实现步骤和前者一样，只是机器在启动的时候在APP1SERVERS创建的节点类型变为EPHEMERAL_SEQUENTIAL类型，
这样每个节点会自动被编号，例如      
'''
zk.create("/testRootPath/testChildPath1","1".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        
zk.create("/testRootPath/testChildPath2","2".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        
zk.create("/testRootPath/testChildPath3","3".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        
// 创建一个子目录节点
zk.create("/testRootPath/testChildPath4","4".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);

System.out.println(zk.getChildren("/testRootPath", false));
'''

打印结果：[testChildPath10000000000, testChildPath20000000001, testChildPath40000000003, testChildPath30000000002]

'''
zk.create("/testRootPath", "testRootData".getBytes(),Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

// 创建一个子目录节点
zk.create("/testRootPath/testChildPath1","1".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        
zk.create("/testRootPath/testChildPath2","2".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        
zk.create("/testRootPath/testChildPath3","3".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        
// 创建一个子目录节点
zk.create("/testRootPath/testChildPath4","4".getBytes(), Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);

System.out.println(zk.getChildren("/testRootPath", false));
'''

打印结果:[testChildPath2, testChildPath1, testChildPath4, testChildPath3]

我们默认规定编号最小的为master,所以当我们对/APP1SERVERS节点做监控的时候，得到服务器列表，
只要所有集群机器逻辑认为最小编号节点为master，那么master就被选出，而这个master宕机的时候，
相应的znode会消失，然后新的服务器列表就被推送到客户端，然后每个节点逻辑认为最小编号节点为master，
这样就做到动态master选举。

----------
###### 分布式服务框架 Zookeeper -- 管理分布式环境中的数据  ######
[ http://www.ibm.com/developerworks/cn/opensource/os-cn-zookeeper/ ]

* 统一命名服务 （Name Service） *
Name Service 已经是 Zookeeper 内置的功能，你只要调用 Zookeeper 的 API 就能实现。如调用 create 接口就可以很容易创建一个目录节点。

* 配置管理（Configuration Management） *
将配置信息保存在 Zookeeper 的某个目录节点中，然后将所有需要修改的应用机器监控配置信息的状态，一旦配置信息发生变化，每台应用机器就会收到 Zookeeper 的通知，然后从 Zookeeper 获取新的配置信息应用到系统中。

* 集群管理（Group Membership）*
如有多台 Server 组成一个服务集群，那么必须要一个“总管”知道当前集群中每台机器的服务状态，一旦有机器不能提供服务，集群中其它集群必须知道，从而做出调整重新分配服 务策略。同样当增加集群的服务能力时，就会增加一台或多台 Server，同样也必须让“总管”知道。

Zookeeper 不仅能够帮你维护当前的集群中机器的服务状态，而且能够帮你选出一个“总管”，让这个总管来管理集群，这就是 Zookeeper 的另一个功能 Leader Election。

它们的实现方式都是在 Zookeeper 上创建一个 EPHEMERAL 类型的目录节点，然后每个 Server 在它们创建目录节点的父目录节点上调用 getChildren (String  path, boolean watch) 方法并设置 watch 为 true，由于是 EPHEMERAL 目录节点，当创建它的 Server 死去，这个目录节点也随之被删除，所以 Children 将会变化，这时getChildren 上的 Watch 将会被调用，所以其它 Server 就知道已经有某台 Server 死去了。新增 Server 也是同样的原理。

Zookeeper 如何实现 Leader Election，也就是选出一个 Master Server。和前面的一样每台 Server 创建一个 EPHEMERAL 目录节点，不同的是它还是一个 SEQUENTIAL 目录节点，所以它是个 EPHEMERAL_SEQUENTIAL 目录节点。之所以它是 EPHEMERAL_SEQUENTIAL 目录节点，是因为我们可以给每台 Server 编号，我们可以选择当前是最小编号的 Server 为 Master，假如这个最小编号的 Server 死去，由于是 EPHEMERAL 节点，死去的 Server 对应的节点也被删除，所以当前的节点列表中又出现一个最小编号的节点，我们就选择这个节点为当前 Master。这样就实现了动态选择 Master，避免了传统意义上单 Master 容易出现单点故障的问题。

清单 3. Leader Election 关键代码

'''
 void findLeader() throws InterruptedException {
        byte[] leader = null;
        try {
            leader = zk.getData(root + "/leader", true, null);
        } catch (Exception e) {
            logger.error(e);
        }
        if (leader != null) {
            following();
        } else {
            String newLeader = null;
            try {
                byte[] localhost = InetAddress.getLocalHost().getAddress();
                newLeader = zk.create(root + "/leader", localhost,
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            } catch (Exception e) {
                logger.error(e);
            }
            if (newLeader != null) {
                leading();
            } else {
                mutex.wait();
            }
        }
    } 
'''

* 共享锁（Locks） *

共享锁在同一个进程中很容易实现，但是在跨进程或者在不同 Server 之间就不好实现了。Zookeeper 却很容易实现这个功能，实现方式也是需要获得锁的 Server 创建一个 EPHEMERAL_SEQUENTIAL 目录节点，然后调用 getChildren 方法获取当前的目录节点列表中最小的目录节点是不是就是自己创建的目录节点，如果正是自己创建的，那么它就获得了这个锁，如果不是那么它就调用 exists (String  path, boolean watch) 方法并监控 Zookeeper 上目录节点列表的变化，一直到自己创建的节点是列表中最小编号的目录节点，从而获得锁，释放锁很简单，只要删除前面它自己所创建的目录节点就行了。

清单 4. 同步锁的关键代码
'''
 void getLock() throws KeeperException, InterruptedException{
        List<String> list = zk.getChildren(root, false);
        String[] nodes = list.toArray(new String[list.size()]);
        Arrays.sort(nodes);
        if(myZnode.equals(root+"/"+nodes[0])){
            doAction();
        }
        else{
            waitForLock(nodes[0]);
        }
    }
    void waitForLock(String lower) throws InterruptedException, KeeperException {
        Stat stat = zk.exists(root + "/" + lower,true);
        if(stat != null){
            mutex.wait();
        }
        else{
            getLock();
        }
    } 
'''

* 队列管理 *
Zookeeper 可以处理两种类型的队列：
1. 当一个队列的成员都聚齐时，这个队列才可用，否则一直等待所有成员到达，这种是同步队列。
2. 队列按照 FIFO 方式进行入队和出队操作，例如实现生产者和消费者模型。

同步队列用 Zookeeper 实现的实现思路如下：

创建一个父目录 /synchronizing，每个成员都监控标志（Set Watch）位目录 /synchronizing/start 是否存在，然后每个成员都加入这个队列，加入队列的方式就是创建 /synchronizing/member_i 的临时目录节点，然后每个成员获取 / synchronizing 目录的所有目录节点，也就是 member_i。判断 i 的值是否已经是成员的个数，如果小于成员个数等待 /synchronizing/start 的出现，如果已经相等就创建 /synchronizing/start。
清单 5. 同步队列
'''
 void addQueue() throws KeeperException, InterruptedException{
        zk.exists(root + "/start",true);
        zk.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE,
        CreateMode.EPHEMERAL_SEQUENTIAL);
        synchronized (mutex) {
            List<String> list = zk.getChildren(root, false);
            if (list.size() < size) {
                mutex.wait();
            } else {
                zk.create(root + "/start", new byte[0], Ids.OPEN_ACL_UNSAFE,
                 CreateMode.PERSISTENT);
            }
        }
 } 
'''
当队列没满是进入 wait()，然后会一直等待 Watch 的通知，Watch 的代码如下：
'''
 public void process(WatchedEvent event) {
        if(event.getPath().equals(root + "/start") &&
         event.getType() == Event.EventType.NodeCreated){
            System.out.println("得到通知");
            super.process(event);
            doAction();
        }
    } 
'''


FIFO 队列用 Zookeeper 实现思路如下：

实现的思路也非常简单，就是在特定的目录下创建 SEQUENTIAL 类型的子目录 /queue_i，这样就能保证所有成员加入队列时都是有编号的，出队列时通过 getChildren( ) 方法可以返回当前所有的队列中的元素，然后消费其中最小的一个，这样就能保证 FIFO。

清单 6. 生产者代码
'''
 boolean produce(int i) throws KeeperException, InterruptedException{
        ByteBuffer b = ByteBuffer.allocate(4);
        byte[] value;
        b.putInt(i);
        value = b.array();
        zk.create(root + "/element", value, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT_SEQUENTIAL);
        return true;
    }
'''

清单 7. 消费者代码
'''
int consume() throws KeeperException, InterruptedException{
        int retvalue = -1;
        Stat stat = null;
        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(root, true);
                if (list.size() == 0) {
                    mutex.wait();
                } else {
                    Integer min = new Integer(list.get(0).substring(7));
                    for(String s : list){
                        Integer tempValue = new Integer(s.substring(7));
                        if(tempValue < min) min = tempValue;
                    }
                    byte[] b = zk.getData(root + "/element" + min,false, stat);
                    zk.delete(root + "/element" + min, 0);
                    ByteBuffer buffer = ByteBuffer.wrap(b);
                    retvalue = buffer.getInt();
                    return retvalue;
                }
            }
        }
 } 
'''




----------
###### 《hadoop in action》之zookeeper学习笔记
[ http://macrochen.iteye.com/blog/1005164 ]


----------
###### 笔记 ######
* CountDownLatch 
- 多线程同步锁
- java.util.concurrent.CountDownLatch
- 设定计数 CountDownLatch(总数);
- 等待计数归零 latch.await();
- 计数器减一 latch.countDown();

* Watcher 
- 持续监听需要在触发后再设置一次watcher（只监听一次消息，然后就会失效）
- 连接关闭后，监听失效


----------
###### 参考列表
* 《hadoop in action》之zookeeper学习笔记
[ http://macrochen.iteye.com/blog/1005164 ]
* 分布式服务框架 Zookeeper -- 管理分布式环境中的数据
[ http://www.ibm.com/developerworks/cn/opensource/os-cn-zookeeper/ ]

* zookeeper使用和原理探究（一）
[ http://www.blogjava.net/BucketLi/archive/2010/12/21/341268.html ]
* ZooKeeper Getting Started Guide 
[ http://zookeeper.apache.org/doc/current/zookeeperStarted.html ]
* [ http://www.ibm.com/developerworks/cn/opensource/os-cn-zookeeper/ ]
* [ http://hadoop.apache.org/zookeeper/docs/current/ ]
* [ http://rdc.taobao.com/team/jm/archives/448 ]

----------
###### 相关站点
* Hadoop技术论坛 
[http://www.hadoopor.com/]