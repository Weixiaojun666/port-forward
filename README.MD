# [port-forward](https://github.com/Weixiaojun666/port-forward)

## 基于源项目[port-forwarding](https://github.com/anexplore/port-forwarding)

* 添加UDP转发功能
* 添加Toml配置文件支持
* 添加基础防护功能
* 使用Java17+开发
* 添加云防护功能【需配合服务器端】

tcp/udp port forwarding based on netty / 基于Netty的TCP/UDP端口流量转发

this project may be helpful when you are not familiar with iptables or other port forwarding techs.

this project may be helpful when you want to do some stuff on data flow, based on this project's source code, you can
write your own code to handle data flow before forwarding.

### configuration

this project will read config from java system properties and system env, properties first.

* mappingFilePath, port mapping file
* timeout, io timeout in mills for read/write idle timeout, **if your connection is long-connection you may need to set
  timeout to bigger one**
* connectTimeout, io timeout in mills for connection to target remote port
* ioAcceptThreadNumber, accept thread number default system cores
* ioWorkThreadNumber, work thread number default system cores
* ioMaxBacklog, socket backlog default 64
* openLoggingHandler, detail netty debug logging

