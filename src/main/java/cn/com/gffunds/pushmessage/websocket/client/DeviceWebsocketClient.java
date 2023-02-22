package cn.com.gffunds.pushmessage.websocket.client;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.URI;
import java.util.Map;
 
/**
 * @Author Curtain
 * @Date 2021/12/3 16:51
 * @Description
 */
@Slf4j
public class DeviceWebsocketClient extends BaseWebsocketClient{
    
    private static final String ACS_CTRL_RESULT = "deviceWebsocketClient";
    private static final String SUBSCRIBE = "subscribe";
    /*这个订阅格式是实现约定好的，可以具体情况具体分析*/
    private String sendStr = "{\"msgId\":\"1\",\"command\":\"subscribe\",\"bizTopics\":[{\"bizId\":\"1\",\"topics\":[\"user\"]}]}";
 
    public DeviceWebsocketClient(URI serverUri, Map<String, String> httpHeaders, ThreadPoolTaskExecutor workPoolScheduler) {
        super(serverUri, httpHeaders, ACS_CTRL_RESULT, workPoolScheduler);
    }
 
    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("------ {} onOpen ------", ACS_CTRL_RESULT);
      //  this.send(sendStr);
        setConnectState(true);
    }
 
    @Override
    @SneakyThrows
    public void onMessage(String msg) {
        log.info("-------- receive acs ctrl result： " + msg + "--------");
    }
}