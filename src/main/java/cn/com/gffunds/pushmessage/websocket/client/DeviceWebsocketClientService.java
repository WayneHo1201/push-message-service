package cn.com.gffunds.pushmessage.websocket.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Curtain
 * @Date 2021/12/3 17:04
 * @Description
 */
@Service
@Slf4j
public class DeviceWebsocketClientService {

    private String sendStr = "{\"msgId\":\"1\",\"command\":\"subscribe\",\"bizTopics\":[{\"bizId\":\"1\",\"topics\":[\"user\"]}]}";

    @Resource
    private ThreadPoolTaskExecutor workPoolScheduler;

    @PostConstruct
    public void start() {
        try {
            log.info("start to receive device data");
            URI uri = new URI("ws://10.89.123.50:8000/websocket");
            Map<String, String> httpHeaders = new HashMap<>(4);
            httpHeaders.put("Origin", "http://" + uri.getHost());
            httpHeaders.put("Session-Id", "sso:sessionId:guxh:0e9b64b10fa34479b13959c55344a5b2");
            httpHeaders.put("payload", sendStr);
            DeviceWebsocketClient deviceWebsocketClient = new DeviceWebsocketClient(uri, httpHeaders, workPoolScheduler);
            deviceWebsocketClient.connect();
        } catch (Exception e) {
            log.error("start to receive device data failed", e);
        }
    }
}
 