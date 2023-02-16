package com.gffunds.pushmessage.websocket.handler;

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


    /**
     * 线程安全Map，用来存放每个客户端对应的MessageConsumer对象
     */
    private static final Map<WebSocketSession, MessageConsumer> SESSION = new ConcurrentHashMap<>();

    /**
     * 新增socket
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        log.info("成功建立连接");
        //获取用户信息
        UserInfo userInfo = (UserInfo) webSocketSession.getAttributes().get(WebSocketConstants.ATTR_USER);
        MessageConsumer messageConsumer = new MessageConsumer()
                .setValid(WebSocketConstants.VALID)
                .setUserInfo(userInfo)
                .setWebSocketSession(webSocketSession);
        SESSION.put(webSocketSession, messageConsumer);
        log.info("链接成功");
    }

    /**
     * 接收socket信息
     */
    @Override
    public void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) {
        String payload = message.getPayload();
        MessageConsumer messageConsumer = SESSION.get(webSocketSession);
        log.info("接收到的信息 --- " + payload);
        // 构造messageRequest
        MessageRequest messageRequest = JacksonUtil.toObject(payload, MessageRequest.class);
        String msgId = messageRequest.getMsgId();
        // 构建bizMessageManagerMap
        List<BizTopic> bizTopics = messageRequest.getBizTopics();
        Map<String, BizMessageManager> bizMessageManagerMap = bizTopics.stream()
                .collect(Collectors.toConcurrentMap(BizTopic::getBizId, bizTopic -> new BizMessageManager(bizTopic.getBizId(), bizTopic.getTopics())));
        messageConsumer.setBizMessageManagers(bizMessageManagerMap);
        MessageResponse response = new MessageResponse()
                .setMsgId(msgId)
                .setMsgType(WebSocketConstants.MSG_TYPE_COMMAND);
        String command = messageRequest.getCommand();
        // todo 通用消息返回
        if (command.equals(WebSocketConstants.SUBSCRIBE)) {
            // 发送订阅通知
            messageConsumer.subscribe(bizMessageManagerMap);
            response.setData("订阅成功！");
        } else if (command.equals(WebSocketConstants.UNSUBSCRIBE)) {
            // 发送退订通知
            messageConsumer.unsubscribe(bizMessageManagerMap);
            response.setData("退订成功！");
        } else {
            String msg = String.format("不支持该命令！command=%s", command);
            log.error(msg);
            response.setData(msg);
        }
        sendMessage(webSocketSession, new TextMessage(String.valueOf(response)));
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
        SESSION.remove(session);
        messageConsumer.closeConnection();
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