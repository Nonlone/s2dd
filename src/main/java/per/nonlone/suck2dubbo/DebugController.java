package per.nonlone.suck2dubbo;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class DebugController {

    private static final String DUBBO_PROTOCOL = "dubbo";

    private static final String NACOS_PROTOCOL = "nacos";

    private static final String PRODIVERS = "providers:";

    private static final Map<String,NamingService> NAMING_SERVICE_MAP = new ConcurrentHashMap<>();

    private static final String RPC_CONTEXT_PREFIX = "rpccontext|";

    @Value("dubbo.application.name")
    private String dataId;


    @GetMapping("index")
    public Object index() {
        return "hello world";
    }

    /**
     * 直连调试
     * @param ip
     * @param port
     * @param classOfService
     * @param metchod
     * @param version
     * @param group
     * @param classOfRequest
     * @param data
     * @return
     */
    @PostMapping("dd/{ip}/{port}/{class}/{method}")
    public Object debugDirect(
        @PathVariable("ip") String ip,
        @PathVariable("port") String port,
        @PathVariable("class") String classOfService,
        @PathVariable("method") String metchod,
        @RequestHeader(value = "version",defaultValue = "")String version,
        @RequestHeader(value = "group",defaultValue = "")String group,
        @RequestHeader(value = "class")String classOfRequest,
        @RequestBody Map<String,Object> data,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
        reference.setGeneric(true);
        reference.setInterface(classOfService);
        reference.setVersion(version);
        reference.setUrl(DUBBO_PROTOCOL+"://"+ip+":"+port);
        reference.setProtocol(DUBBO_PROTOCOL);
        if(StringUtils.isNotBlank(version)){
            reference.setVersion(version);
        }
        if(StringUtils.isNotBlank(group)){
            reference.setGroup(group);
        }
        // 获取头部数据，放入rpccontext
        Enumeration<String> headerEnmeration = httpServletRequest.getHeaderNames();
        while (headerEnmeration.hasMoreElements()) {
            String headerKey = headerEnmeration.nextElement();
            if (headerKey.contains(RPC_CONTEXT_PREFIX)) {
                String realKey = headerKey.replace(RPC_CONTEXT_PREFIX, "");
                RpcContext.getContext().setAttachment(realKey, httpServletRequest.getHeader(headerKey));
            }
        }
        GenericService genericService = reference.get();
        Object result = genericService.$invoke(metchod,new String[]{classOfRequest},new Object[]{data});
       
        Map<String,Object> attachment = RpcContext.getContext().getObjectAttachments();
        attachment.entrySet().stream().forEach(t->{
            httpServletResponse.addHeader(t.getKey(), t.getValue().toString());
        });

        return result;
    }


    /**
     * 使用Nacos调试
     * @param namespace nacos 配置
     * @param serverAddr nacos 地址
     * @param port nacos 端口
     * @param classOfService 调用服务类
     * @param metchod 调用服务方法
     * @param version 调用服务方法版本
     * @param group 调用服务组
     * @param classOfRequest 请求类型
     * @param data 数据体
     * @return
     */
    @PostMapping("dwn/{namespace}/{serverAddr}/{port}/{class}/{method}")
    public Object debugWithNacos(
        @PathVariable("namespace")String namespace,
        @PathVariable("serverAddr") String serverAddr,
        @PathVariable("port") String port,
        @PathVariable("class") String classOfService,
        @PathVariable("method") String metchod,
        @RequestHeader(value = "version",defaultValue = "")String version,
        @RequestHeader(value = "group",defaultValue = "")String group,
        @RequestHeader(value = "class")String classOfRequest,
        @RequestBody Map<String,Object> data,
        HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse
    ) {
        try {

            String namingServiceKey =  namespace+":"+serverAddr+":"+port;
            NamingService namingService = NAMING_SERVICE_MAP.get(namingServiceKey);
            if(Objects.isNull(namingService)){
                log.info("init nacos client namingServiceKey<{}>",namingServiceKey);
                Properties properties = new Properties();
                properties.put(PropertyKeyConst.SERVER_ADDR,serverAddr+":"+port);
                properties.put(PropertyKeyConst.NAMESPACE,namespace);
                namingService = NacosFactory.createNamingService(properties);
                NAMING_SERVICE_MAP.putIfAbsent(namingServiceKey,namingService);
            }else{
                log.info("use nacos client without init namingServiceKey<{}>",namingServiceKey);
            }

            String serviceName = PRODIVERS + classOfService + ":";
            if(StringUtils.isNotBlank(version)){
                serviceName = serviceName + version + ":";
            }
            if(StringUtils.isNotBlank(group)){
                serviceName = serviceName + group;
            }
            List<Instance> instanceList = namingService.selectInstances(serviceName,true);
            if(CollectionUtils.isEmpty(instanceList)){
                return Maps.immutableEntry("error",String.format("cannot find the provider of service in nacos； serviceName<%s>", serviceName));
            }
            int index = ThreadLocalRandom.current().nextInt(instanceList.size());
            Instance instance = instanceList.get(0);
            log.info("use instance ip<{}> port<{}>",instance.getIp(),instance.getPort());
            ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
            reference.setGeneric(true);
            reference.setInterface(classOfService);
            reference.setVersion(version);
            reference.setUrl(DUBBO_PROTOCOL+"://"+instance.getIp()+":"+instance.getPort()     );
            reference.setProtocol(DUBBO_PROTOCOL);
            if(StringUtils.isNotBlank(version)){
                reference.setVersion(version);
            }
            if(StringUtils.isNotBlank(group)){
                reference.setGroup(group);
            }
            // 获取头部数据，放入rpccontext
            Enumeration<String> headerEnmeration = httpServletRequest.getHeaderNames();
            while (headerEnmeration.hasMoreElements()) {
                String headerKey = headerEnmeration.nextElement();
                if (headerKey.contains(RPC_CONTEXT_PREFIX)) {
                    String realKey = headerKey.replace(RPC_CONTEXT_PREFIX, "");
                    RpcContext.getContext().setAttachment(realKey, httpServletRequest.getHeader(headerKey));
                }
            }
            GenericService genericService = reference.get();
            Object result = genericService.$invoke(metchod,new String[]{classOfRequest},new Object[]{data});

            Map<String,Object> attachment = RpcContext.getContext().getObjectAttachments();
            attachment.entrySet().stream().forEach(t->{
                httpServletResponse.addHeader(t.getKey(), t.getValue().toString());
            });

            return result;

        }catch (NacosException ne){
            log.error("getClient error",ne);
            throw new RuntimeException(ne);
        }


    }
}
