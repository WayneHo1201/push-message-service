package cn.com.gffunds.pushmessage.websocket.client;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
 
import java.net.URI;
import java.util.Map;
 
/**
 * @Author Curtain
 * @Date 2021/11/1 9:49
 * @Description
 */
@Slf4j
public class BaseWebsocketClient extends WebSocketClient {
	//客户端标识
    private String clientName;
    //客户端连接状态
    private boolean isConnect = false;
    //spring包下的线程池类
    private ThreadPoolTaskExecutor workPoolScheduler;
    
    public BaseWebsocketClient(URI serverUri, Map<String, String> httpHeaders,
                               String clientName,
                               ThreadPoolTaskExecutor workPoolScheduler) {
        super(serverUri, new Draft_6455(), httpHeaders, 0);
        this.clientName = clientName;
        this.workPoolScheduler = workPoolScheduler;
    }
    
    @Override
    public void onOpen(ServerHandshake serverHandshake) {   
    }
 
    @Override
    public void onMessage(String s) {
    }
    /***检测到连接关闭之后，会更新连接状态以及尝试重新连接***/
    @Override
    public void onClose(int i, String s, boolean b) {
        log.info("------ {} onClose ------{}", clientName, b);
        setConnectState(false);
        recontact();
    }
	/***检测到错误，更新连接状态***/
    @Override
    public void onError(Exception e) {
        log.info("------ {} onError ------{}", clientName, e);
        setConnectState(false);
    }
 
    public void setConnectState(boolean isConnect) {
        this.isConnect = isConnect;
    }
    
    public boolean getConnectState(){
        return this.isConnect;
    }
 
    public ThreadPoolTaskExecutor getWorkPoolScheduler() {
        return workPoolScheduler;
    }
 
    /**
     * 重连
     */
    public void recontact() {
        workPoolScheduler.execute(() -> {
            Thread.currentThread().setName( "ReconnectThread-" + Thread.currentThread().getId() );
            try {
                Thread.sleep(10000);
                log.info("重连开始");
                if (isConnect) {
                    log.info("{} 重连停止", clientName);
                    return;
                }
                this.reconnect();
                log.info("重连结束");
            } catch (Exception e) {
                log.info("{} 重连失败", clientName);
            }
        });
    }
}