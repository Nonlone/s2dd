package per.nonlone.suck2dubbo;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    private static final String DUBBO_PROTOCOL = "dubbo";

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
    @PostMapping("dd/{ip}/{port}//{class}/{method}")
    public Object debugDirect(
        @PathVariable("ip") String ip,
        @PathVariable("port") String port,
        @PathVariable("class") String classOfService,
        @PathVariable("method") String metchod,
        @RequestHeader(value = "version",defaultValue = "")String version,
        @RequestHeader(value = "group",defaultValue = "")String group,
        @RequestHeader(value = "class")String classOfRequest,
        @RequestBody Map<String,Object> data
    ) {

        DebugReferenceConfig<GenericService> reference = new DebugReferenceConfig<GenericService>();
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
        GenericService genericService = reference.get();
        return genericService.$invoke(metchod,new String[]{classOfRequest},new Object[]{data});
    }
}
