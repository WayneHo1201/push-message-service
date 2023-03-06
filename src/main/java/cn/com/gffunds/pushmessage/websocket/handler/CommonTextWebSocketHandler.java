package cn.com.gffunds.pushmessage.websocket.handler;

import cn.com.gffunds.commons.exception.JsonDeserializerException;
import cn.com.gffunds.commons.json.JacksonUtil;
import cn.com.gffunds.pushmessage.common.enumeration.ErrCodeEnum;
import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.consumer.MessageConsumer;
import cn.com.gffunds.pushmessage.websocket.entity.BizTopic;
import cn.com.gffunds.pushmessage.websocket.entity.MessageRequest;
import cn.com.gffunds.pushmessage.websocket.entity.MessageResponse;
import cn.com.gffunds.pushmessage.websocket.entity.UserInfo;
import cn.com.gffunds.pushmessage.websocket.manager.BizMessageManager;
import cn.hutool.extra.spring.SpringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
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

    private static final String CONSUMER = "messageConsumer";

    /**
     * 新增socket
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) {
        //获取用户信息
        UserInfo userInfo = (UserInfo) webSocketSession.getAttributes().get(WebSocketConstants.ATTR_USER);
        MessageConsumer messageConsumer = SpringUtil.getBean(CONSUMER, MessageConsumer.class);
        messageConsumer.setUserInfo(userInfo)
                .setWebSocketSession(webSocketSession);
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
        handleCommand(webSocketSession, payload, messageConsumer);
    }

    /**
     * 处理订阅退订信息
     */
    private void handleCommand(WebSocketSession webSocketSession, String payload, MessageConsumer messageConsumer) {
        log.info("websocket接收到的信息： " + payload);
        // 构造messageRequest
        MessageRequest messageRequest;
        MessageResponse response;
        try {
            messageRequest = JacksonUtil.toObject(payload, MessageRequest.class);
        } catch (JsonDeserializerException e) {
            String msg = String.format("请求消息解析异常！payload=%s", payload);
            log.error(msg);
            response = new MessageResponse().setCode(ErrCodeEnum.REST_EXCEPTION.code()).setData(msg);
            sendMessage(webSocketSession, new TextMessage(JacksonUtil.toJson(response)));
            return;
        }
        if (WebSocketConstants.PING.equals(messageRequest.getCommand())) {
            log.info("心跳包检测，用户：{}", messageConsumer.getUserInfo().getUsername());
            return;
        }
        String msgId = messageRequest.getMsgId();
        List<BizTopic> bizTopics = messageRequest.getBizTopics();
        // 构建bizMessageManagerMap
        Map<String, BizMessageManager> bizMessageManagerMap = bizTopics.stream()
                .collect(Collectors.toConcurrentMap(BizTopic::getBizId, bizTopic -> new BizMessageManager(bizTopic.getBizId(), bizTopic.getTopics())));
        // 构建命令返回对象
        response = new MessageResponse()
                .setMsgId(msgId)
                .setMsgType(WebSocketConstants.MSG_TYPE_COMMAND);
        String command = messageRequest.getCommand();
        //  通用消息返回
        if (WebSocketConstants.SUBSCRIBE.equals(command)) {
            // 发送订阅通知
            messageConsumer.subscribe(bizMessageManagerMap, response);
        } else if (WebSocketConstants.UNSUBSCRIBE.equals(command)) {
            // 发送退订通知
            messageConsumer.unsubscribe(bizMessageManagerMap, response);
        } else {
            String msg = String.format("不支持该命令！command=%s", command);
            log.error(msg);
            response.setCode(ErrCodeEnum.REST_EXCEPTION.code()).setData(msg);
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
        close(session);
        log.info("连接已关闭：" + status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        close(session);
        log.error("连接出错", exception);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 清空session
     */
    private void close(WebSocketSession session) throws IOException {
        MessageConsumer messageConsumer = SESSION.get(session);
        if (messageConsumer != null) {
            messageConsumer.closeConnection();
            SESSION.remove(session);
        } else {
            if (session.isOpen()) {
                session.close();
            }
        }
    }


}