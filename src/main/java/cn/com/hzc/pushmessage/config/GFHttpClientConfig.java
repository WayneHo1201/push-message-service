package cn.com.hzc.pushmessage.config;

import cn.com.gffunds.httpclient.client.GFHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hezhc
 * @date 2023/3/1 13:50
 * @description httpClient
 */
@Configuration
public class GFHttpClientConfig {
    /**
     *  构造访问sso的httpClient
     */
    @Bean("ssoGfHttpClient")
    public GFHttpClient ssoGfHttpClient() {
        return new GFHttpClient()
                .setSocketTimeout(3000)
                .setConnectTime(3000)
                .setAppId("sso");
    }
}
