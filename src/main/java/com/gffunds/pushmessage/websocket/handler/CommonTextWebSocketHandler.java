package com.gffunds.pushmessage.websocket.handler;

import cn.com.gffunds.commons.exception.JsonDeserializerException;
import cn.com.gffunds.commons.json.JacksonUtil;
import com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import com.gffunds.pushmessage.websocket.consumer.MessageConsumer;
import com.gffunds.pushmessage.websocket.entity.BizTopic;
import com.gffunds.pushmessage.websocket.entity.MessageRequest;
import com.gffunds.pushmessage.websocket.entity.MessageResponse;
import com.gffunds.pushmessage.websocket.entity.UserInfo;
import com.gffunds.pushmessage.websocket.manager.BizMessageManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommonTextWebSocketHandler extends TextWebSocketHandler {

    @Value("${websocket.retry-times:3}")
    private int retry;

    @Value("${websocket.sleep-millis:3000}")
    private int sleepMillis;


    /**
     * 线程安全Map，用来存放每个客户端对应的MessageConsumer对象
     */
    private static final Map<WebSocketSession, MessageConsumer> SESSION = new ConcurrentHashMap<>();

    /**
     * 新增socket
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        //获取用户信息
        UserInfo userInfo = (UserInfo) webSocketSession.getAttributes().get(WebSocketConstants.ATTR_USER);
        MessageConsumer messageConsumer = new MessageConsumer()
                .setValid(WebSocketConstants.VALID)
                .setUserInfo(userInfo)
                .setWebSocketSession(webSocketSession)
                .setRetryTimes(retry)
                .setSleepMillis(sleepMillis);
        SESSION.put(webSocketSession, messageConsumer);
        log.info("===========成功建立连接===========");
    }

    /**
     * 接收socket信息
     */
    @Override
    public void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) {
        String payload = message.getPayload();
        MessageConsumer messageConsumer = SESSION.get(webSocketSession);
        log.info("ws接收到的信息： " + payload);
        // 构造messageRequest
        MessageRequest messageRequest;
        try {
            messageRequest = JacksonUtil.toObject(payload, MessageRequest.class);
        } catch (JsonDeserializerException e) {
            String msg = String.format("请求消息不合法！payload=%s", payload);
            log.error(msg);
            sendMessage(webSocketSession, new TextMessage(msg));
            return;
        }
        String msgId = messageRequest.getMsgId();
        List<BizTopic> bizTopics = messageRequest.getBizTopics();
        // 构建bizMessageManagerMap
        Map<String, BizMessageManager> bizMessageManagerMap = bizTopics.stream()
                .collect(Collectors.toConcurrentMap(BizTopic::getBizId, bizTopic -> new BizMessageManager(bizTopic.getBizId(), bizTopic.getTopics())));
        // 构建命令返回对象
        MessageResponse response = new MessageResponse()
                .setMsgId(msgId)
                .setMsgType(WebSocketConstants.MSG_TYPE_COMMAND);
        String command = messageRequest.getCommand();
        // todo 通用消息返回
        if (WebSocketConstants.SUBSCRIBE.equals(command)) {
            // 发送订阅通知
            messageConsumer.subscribe(bizMessageManagerMap);
            response.setData("订阅成功！");
        } else if (WebSocketConstants.UNSUBSCRIBE.equals(command)) {
            // 发送退订通知
            messageConsumer.unsubscribe(bizMessageManagerMap);
            response.setData("退订成功！");
        } else {
            String msg = String.format("不支持该命令！command=%s", command);
            log.error(msg);
            response.setData(msg);
        }
        sendMessage(webSocketSession, new TextMessage(JacksonUtil.toJson(response)));
    }

    /**
     * 发送信息给指定用户
     */
    @SneakyThrows
    public void sendMessage(WebSocketSession webSocketSession, TextMessage textMessage) {
        if (webSocketSession.isOpen()) {
            webSocketSession.sendMessage(textMessage);
        }
    }

    /**
     * 关闭连接
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        MessageConsumer messageConsumer = SESSION.get(session);
        messageConsumer.closeConnection();
        SESSION.remove(session);
        //获取用户信息
        log.info("连接已关闭：" + status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        log.info("连接出错");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}