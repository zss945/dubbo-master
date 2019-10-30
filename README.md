## 1、zookeeper 下载、运行

zookeeper是dobbo推荐使用的注册中心，zookeeper为我们的分布式项目提供：配置维护、域名服务、节点存储、获取数据等等。

#### 下载地址：[https://www-eu.apache.org/dist/zookeeper/](https://www-eu.apache.org/dist/zookeeper/)

选择相应版本，我这里用的是3.5.6版本：apache-zookeeper-3.5.6-bin.tar.gz

解压后，在conf目录下，复制zoo_sample.cfg粘贴重命名为zoo.cfg
配置如下内容：
```
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/Users/zm/Downloads/zookeeper/data
dataLogDir=/Users/zm/Downloads/zookeeper/logs
clientPort=2181
```
#### 运行zookeeper

在bin目录下，有运行的脚本，我用的是Mac，在cmd输入，即可运行zookeeper
```
./bin/zkServer.sh start 
```
启动完成后，zookeeper://127.0.0.1:2181就是注册中心的地址。

## 2、创建springboot工程
首先创建一个springboot工程作为父模块。
### 2.1 创建一个api子模块
api子模块用于定义接口，不需要启动类，可以删除Application启动类， 我们这里定义一个获取服务器信息的接口，为负载均衡服务器信息。
```
package com.duboo.api;

import java.util.Map;

public interface DubboService {

    Map<String, Object> getServiceInfo();

}

```
### 2.2 创建一个provider子模块
首先application.properties中添加
```
server.port=8090
dubbo.application.id=dubbo-provider
dubbo.application.name=dubbo-provider
dubbo.monitor.protocol=dubbo-registry
dubbo.registry.address=zookeeper://127.0.0.1:2181
dubbo.protocol.name=dubbo
dubbo.protocol.port=20880
```
然后pom.xml中添加
```
    <dependencies>
        <!-- 引用api子模块 -->
        <dependency>
            <groupId>com.dubbo</groupId>
            <artifactId>api</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>

        <!-- apache的springboot dubbo依赖 -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
            <version>2.7.3</version>
        </dependency>

        <!-- zookeeper -->
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.5.6</version>
            <exclusions>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-log4j12</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>log4j</groupId>
                    <artifactId>log4j</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>io.netty</groupId>
                    <artifactId>netty</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <!-- Zookeeper客户端，封装了大量ZooKeeper常用API操作 -->
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-recipes</artifactId>
            <version>4.2.0</version>
        </dependency>
    </dependencies>
```
然后，实现api子模块DubboService接口
```
import com.duboo.api.DubboService;

import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class DubboServiceImpl implements DubboService {

    private Logger logger = LoggerFactory.getLogger(DubboServiceImpl.class);

    @Override
    public Map<String, Object> getServiceInfo() {
        String fullURL = RpcContext.getContext().getUrl().toFullString();
        Map<String, Object> map = new HashMap<>();
        map.put("FullURL", fullURL);
        logger.info("-----fullURL = {}", fullURL);
        return map;
    }

}
```
这里注意是@Service引用org.apache.dubbo.config.annotation.Service。

最后，在springboot启动中添加@EnableDubbo，启动服务即可。
```
@EnableDubbo
@SpringBootApplication
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

}
```
### 2.3 创建一个consumer子模块
首先application.properties中添加
```
server.port=8091
dubbo.application.id=dubbo-consumer
dubbo.application.name=dubbo-consumer
dubbo.monitor.protocol=dubbo-registry
dubbo.registry.address=zookeeper://127.0.0.1:2181
dubbo.protocol.name=dubbo
dubbo.protocol.port=20880

```
然后pom.xml中添加
```
    <dependencies>
        <!-- 引用api子模块 -->
        <dependency>
            <groupId>com.dubbo</groupId>
            <artifactId>api</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>

        <!-- apache的springboot dubbo依赖 -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-spring-boot-starter</artifactId>
            <version>2.7.3</version>
        </dependency>

        <!-- zookeeper -->
        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.5.6</version>
            <exclusions>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-log4j12</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>log4j</groupId>
                    <artifactId>log4j</artifactId>
                </exclusion>
                <exclusion>
                    <groupId>io.netty</groupId>
                    <artifactId>netty</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <!-- Zookeeper客户端，封装了大量ZooKeeper常用API操作 -->
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-recipes</artifactId>
            <version>4.2.0</version>
        </dependency>

        <!-- web依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>2.1.6.RELEASE</version>
        </dependency>
    </dependencies>
```
然后，创建ConsumerController类
```
import com.duboo.api.DubboService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class ConsumerController {

    // random 随机，默认的
    // roundrobin 轮询
    // leastactive //最少活跃
    // consistenthash //一致性Hash
    @Reference(loadbalance = "roundrobin")
    private DubboService dubboService;

    @GetMapping("getInfo")
    public Object getServiceInfo() {
        return dubboService.getServiceInfo();
    }

}
```
这里注意是调用DubboService服务用到的负载均衡策略是轮询访问，默认是随机

最后，在springboot启动中添加@EnableDubbo，启动consumer服务即可。
```
@EnableDubbo
@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

}

```
### 2.4 创建多个provider子模块
这一步是用于测试负载均衡策略，不需要测试的话可以略，复制一个provider子模块重命名provider2
修改application.properties中的端口，启动服务即可
```
server.port=8091
dubbo.application.id=dubbo-provider
dubbo.application.name=dubbo-provider
dubbo.monitor.protocol=dubbo-registry
dubbo.registry.address=zookeeper://127.0.0.1:2181
dubbo.protocol.name=dubbo
dubbo.protocol.port=20881
```
测试访问：[http://localhost:8091/api/getInfo](http://localhost:8091/api/getInfo) 可以看见
dubbo://192.168.43.55:20880/com.duboo.api.DubboService
dubbo://192.168.43.55:20881/com.duboo.api.DubboService
两个服务轮询打印。

最后源码github地址
[https://github.com/zss945/dubbo-master](https://github.com/zss945/dubbo-master)