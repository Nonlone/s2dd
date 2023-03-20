package per.nonlone.suck2dubbo;

import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class DummyStubService implements DummyService {
    @Override
    public String sayHello() {
        return "Hello world";
    }
}
