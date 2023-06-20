package cn.com.hzc.pushmessage.config;

import cn.com.hzc.pushmessage.websocket.entity.BizTopic;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author hezhc
 * @date 2023/2/24 17:38
 * @description redis连接默认配置
 */
@Data
@Accessors(chain = true)
public class DefaultRedisProperties {
    private String host;
    private Integer port;
    private String password;
    private Integer maxActive = 20;
    private Integer maxWait = 20;
    private Integer maxIdle = 20;
    private Integer minIdle= 20;
    private Long timeout = 3000L;
    private List<BizTopic> subscribes;
    private Sentinel sentinel;
    /**
     * Redis sentinel properties.
     */
    @Data
    public static class Sentinel {

        /**
         * Name of the Redis server.
         */
        private String master;

        /**
         * Comma-separated list of "host:port" pairs.
         */
        private List<String> nodes;

        /**
         * Password for authenticating with sentinel(s).
         */
        private String password;
    }
}
