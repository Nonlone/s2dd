package per.nonlone.suck2dubbo;

import org.apache.dubbo.config.ReferenceConfig;

public class DebugReferenceConfig<T> extends ReferenceConfig<T> {

    @Override
    public String getUrl() {
        return super.getUrl();
    }

    @Override
    public void setUrl(final String url) {
        super.setUrl(url);
    }

}
