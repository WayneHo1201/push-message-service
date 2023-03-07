package cn.com.gffunds.pushmessage.client;

import cn.com.gffunds.commons.json.JacksonUtil;
import cn.com.gffunds.pushmessage.websocket.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@Component
@ClientEndpoint
@Slf4j
public class WebSocketClient {

    @Value("${websocket.server.url:10.89.188.67:8098/push-message-service/websocket?token=test}")
    private String serverUrl;


    private Session session;

    @PostConstruct
    void init() {
        try {
            // 本机地址
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String wsUrl = "ws://" + serverUrl ;
            URI uri = URI.create(wsUrl);
            session = container.connectToServer(WebSocketClient.class, uri);
        } catch (DeploymentException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开连接
     * @param session
     */
    @OnOpen
    public void onOpen(Session session) {
        log.info("{}，已连接", session);
        this.session = session;
    }

    /**
     * 接收消息
     * @param text
     */
    @OnMessage
    public void onMessage(String text) {
        Message message = JacksonUtil.toObject(text, Message.class);
        log.info("接收到的信息：{}", message);

    }

    /**
     * 异常处理
     * @param throwable
     */
    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    /**
     * 关闭连接
     */
    @OnClose
    public void onClosing() throws IOException {
        log.info("关闭会话");
        session.close();
    }

    /**
     * 主动发送消息
     */
    public void send(String message) {
        this.session.getAsyncRemote().sendText(message);
    }
    public void close() throws IOException{
        if(this.session.isOpen()){
            this.session.close();
        }
    }


}
