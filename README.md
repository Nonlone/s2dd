# Simple To Debug Dubbo

## 启动方法

依赖 openjdk 1.8+ -D为自定义参数，可以填入spring参数，进程默认占用8081端口提供Http服务

java [-Dxxxx=yyyy] -jar s2dd.jar


## 说明
提供 Http 到 Dubbo 接口的泛化调用，仅用于本地调式使用，ip，port，服务类，调用方法作为参数放在url path上，version，group，还有请求体类路径放在header部分，请求体作为body

### 统一 Header 参数
- group：服务组
- version：服务版本
- class: 请求类全路径

### dd接口 
直接Debug，debugDirect

路径为 dd/{ip}/{port}/{class}/{method}
说明如下：
- ip：目标ip
- port：目标端口
- class：目标类
- method：目标调用方法

详见：DebugController#debugDirect 方法，curl样例如下：

```bash
curl --location --request POST 'http://localhost:8081//dd/localhost/20886/com.yyyy.XxxxService/AaaaBbbbMethod' \
--header 'version: 1.0.0' \
--header 'class: com.yyyy.xxxx.ZzzzRequest' \
--header 'group: local-cccc' \
--header 'Content-Type: application/json' \
--data-raw '{
    "param1":"I am params",
    "param2":"I am params"
}'
```

### dwn接口 
使用Nacos进行服务发现debug，debugWithNacos

路径为 dwn/{namespace}/{serverAddr}/{port}/{class}/{method}
说明如下：
- namespace：对应服务提供nacos注册的namespace
- serverAddr：nacos服务地址
- port：nacos服务端口
- class：目标类
- method：目标方法

详见：DebugController#debugWithNacos 方法，curl样例如下：

```bash
curl --location --request POST 'http://localhost:8081/dwn/develop/nacos.xxxx.com/6801/com.yyyy.XxxxxxService/AaaaBbbbMethod' \
--header 'version: 1.0.0' \
--header 'class: com.yyyy.xxxx.ZzzzzRequest' \
--header 'group: local-cccc' \
--header 'Content-Type: application/json' \
--data-raw '{
    "params1":"I am params",
    "params2":"I am params"
}'
```
