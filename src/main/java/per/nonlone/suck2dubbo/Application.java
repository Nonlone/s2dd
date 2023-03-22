package per.nonlone.suck2dubbo;

import java.util.HashMap;
import java.util.Map;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
// @EnableDubbo
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        // Dubbo 启动
        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        ApplicationConfig applicationConfig = new ApplicationConfig("s2dd");
        applicationConfig.setQosEnable(false);
        applicationConfig.setCompiler("jdk");
        Map<String, String> m = new HashMap<>(1);
        m.put("proxy", "jdk");
        applicationConfig.setParameters(m);


        ProtocolConfig protocolConfig = new ProtocolConfig(CommonConstants.DUBBO, -1);
        protocolConfig.setSerialization("fastjson2");
        bootstrap.application(applicationConfig)
            // .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
            .protocol(protocolConfig)
            .start();

    }

}
