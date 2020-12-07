# Simple To Debbug Dubbo

##启动方法

依赖 openjdk 1.8+

java [-Dxxxx=yyyy] -jar s2dd.jar


##说明
提供 Http 到 Dubbo 接口的泛化调用，仅用于本地调式使用，参数如下图，ip，port，服务类，调用方法作为参数放在url path上，version，group，还有请求体类路径放在header部分，请求体作为body

![param](https://raw.githubusercontent.com/Nonlone/Suck2Dubbo/master/images/params.png)
