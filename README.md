#zookeeper 下载、安装、运行
网址：http://www.apache.org/dyn/closer.cgi/zookeeper
下载：apache-zookeeper-3.5.6-bin.tar.gz
解压后，在conf目录下，新建一个名为zoo.cfg的文件，其中内容如下：

tickTime=2000
initLimit=10
syncLimit=5
dataDir=/Users/zm/Downloads/zookeeper/data
dataLogDir=/Users/zm/Downloads/zookeeper/logs
clientPort=2181

linux运行
启动ZK服务:          sh zkServer.sh start
查看ZK服务状态:   sh zkServer.sh status
停止ZK服务:          sh zkServer.sh stop
重启ZK服务:          sh zkServer.sh restart


#运行provider


#运行provider2


#运行consumer

#访问地址
http://localhost:8091/api/getInfo
