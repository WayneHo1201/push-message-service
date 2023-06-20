package cn.com.hzc.pushmessage.websocket.handler;


import cn.com.hzc.pushmessage.common.enumeration.ErrCodeEnum;
import cn.com.hzc.pushmessage.config.LogConfig;
import cn.com.hzc.pushmessage.util.JacksonUtil;
import cn.com.hzc.pushmessage.websocket.common.enumeration.WebsocketCommandEnum;
import cn.com.hzc.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.hzc.pushmessage.websocket.consumer.MessageConsumer;
import cn.com.hzc.pushmessage.websocket.entity.BizTopic;
import cn.com.hzc.pushmessage.websocket.entity.MessageRequest;
import cn.com.hzc.pushmessage.websocket.entity.MessageResponse;
import cn.com.hzc.pushmessage.websocket.entity.UserInfo;
import cn.com.hzc.pushmessage.websocket.manager.BizMessageManager;
import cn.hutool.extra.spring.SpringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommonTextWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private LogConfig logConfig;


    /**
     * 线程安全Map，用来存放每个客户端对应的MessageConsumer对象
     */
    private static final Map<WebSocketSession, MessageConsumer> SESSION_MAP = new ConcurrentHashMap<>();
    /**
     * 心跳包检测任务map
     */
    private static final Map<String, ScheduledFuture<?>> SCHEDULED_MAP = new ConcurrentHashMap<>();
    private static final String CONSUMER = "messageConsumer";

    // 心跳检测时间
    @Value("${websocket.heartbeat.interval:600000}")
    private long heartbeatInterval;

    private final ScheduledThreadPoolExecutor heartbeatExecutor = new ScheduledThreadPoolExecutor(1);


    /**
     * 新增socket
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        //获取用户信息
        UserInfo userInfo = (UserInfo) webSocketSession.getAttributes().get(WebSocketConstants.ATTR_USER);
        if (userInfo == null) {
            this.close(webSocketSession);
            return;
        }
        MessageConsumer messageConsumer = SpringUtil.getBean(CONSUMER, MessageConsumer.class);
        messageConsumer.setUserInfo(userInfo)
                .setWebSocketSession(webSocketSession);
        SESSION_MAP.put(webSocketSession, messageConsumer);
        log.info("成功建立连接！用户={} ，session={}", userInfo.getUsername(), webSocketSession.getId());
        sendMessage(webSocketSession, new MessageResponse());
        startHeartbeatCheck(messageConsumer);
    }


    /**
     * 接收socket信息
     */
    @Override
    public void handleTextMessage(WebSocketSession webSocketSession, TextMessage message) {
        String payload = message.getPayload();
        MessageConsumer messageConsumer = SESSION_MAP.get(webSocketSession);
        handleCommand(webSocketSession, payload, messageConsumer);
    }

    /**
     * 处理订阅退订信息
     */
    @SneakyThrows
    private void handleCommand(WebSocketSession webSocketSession, String payload, MessageConsumer messageConsumer) {
        if (logConfig.isLogEnable()) {
            log.info("websocket接收到的信息=" + payload);
        }
        // 构造messageRequest
        MessageRequest messageRequest;
        MessageResponse response;
        try {
            messageRequest = JacksonUtil.toObject(payload, MessageRequest.class);
        } catch (Exception e) {
            String msg = String.format("请求消息解析异常！payload=%s", payload);
            response = new MessageResponse().setCode(ErrCodeEnum.REST_EXCEPTION.code()).setData(msg);
            sendMessage(webSocketSession, response);
            log.error(msg);
            return;
        }
        String msgId = messageRequest.getMsgId();
        // 构建命令返回对象
        response = new MessageResponse().setMsgId(msgId);
        if (WebsocketCommandEnum.PING.code().equals(messageRequest.getCommand())) {
            messageConsumer.setLastActiveTime(System.currentTimeMillis());
            log.info("心跳包检测！ 用户={}，session={}", messageConsumer.getUserInfo().getUsername(), webSocketSession.getId());
            sendMessage(webSocketSession, response);
            return;
        }
        List<BizTopic> bizTopics = messageRequest.getBizTopics();
        // 构建bizMessageManagerMap
        Map<String, BizMessageManager> bizMessageManagerMap = bizTopics.stream()
                .collect(Collectors.toConcurrentMap(BizTopic::getBizId, bizTopic -> new BizMessageManager(bizTopic.getBizId(), bizTopic.getTopics())));
        String command = Optional.ofNullable(messageRequest.getCommand()).orElse("").toLowerCase(Locale.ROOT);
        //  通用消息返回
        if (WebsocketCommandEnum.SUBSCRIBE.code().equals(command)) {
            // 发送订阅通知
            messageConsumer.subscribe(bizMessageManagerMap, response);
        } else if (WebsocketCommandEnum.UNSUBSCRIBE.code().equals(command)) {
            // 发送退订通知
            messageConsumer.unsubscribe(bizMessageManagerMap, response);
        } else {
            String msg = String.format("不支持该命令！command=%s", command);
            response.setCode(ErrCodeEnum.REST_EXCEPTION.code()).setData(msg);
            log.error(msg);
        }
        sendMessage(webSocketSession, response);
    }

    /**
     * 发送信息给指定用户
     */
    @SneakyThrows
    public void sendMessage(WebSocketSession webSocketSession, MessageResponse response) {
        if (webSocketSession.isOpen()) {
            webSocketSession.sendMessage(new TextMessage(JacksonUtil.toJson(response)));
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
        MessageConsumer messageConsumer = SESSION_MAP.get(session);
        stopHeartbeatCheck(session.getId());
        if (messageConsumer != null) {
            SESSION_MAP.remove(session);
            messageConsumer.closeConnection();
        } else {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * 启动心跳检测定时任务
     */
    private void startHeartbeatCheck(MessageConsumer messageConsumer) {
        final String id = messageConsumer.getWebSocketSession().getId();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // 获取session的最后活跃时间
                Long lastActiveTime = messageConsumer.getLastActiveTime();
                if (lastActiveTime == null || System.currentTimeMillis() - lastActiveTime > heartbeatInterval) {
                    // 如果最后活跃时间为null或超过了心跳检测时间，则断开连接
                    try {
                        log.warn("{}秒内没有接收到心跳包，主动断开连接！session={}", heartbeatInterval / 1000, id);
                        close(messageConsumer.getWebSocketSession());
                    } catch (IOException e) {
                        log.error("关闭连接失败！", e);
                    } finally {
                        stopHeartbeatCheck(id);
                    }
                }
            }
        };
        // 设置定时任务的执行时间为心跳检测时间
        ScheduledFuture<?> scheduledFuture = heartbeatExecutor.scheduleAtFixedRate(task, heartbeatInterval, heartbeatInterval, TimeUnit.MILLISECONDS);
        SCHEDULED_MAP.putIfAbsent(id, scheduledFuture);
        log.info("开始心跳检测任务！session={}", id);
    }

    /**
     * 停止心跳检测任务任务
     */
    private void stopHeartbeatCheck(String id) {
        if (SCHEDULED_MAP.containsKey(id)) {
            SCHEDULED_MAP.get(id).cancel(false);
            SCHEDULED_MAP.remove(id);
            log.info("停止心跳检测任务！session={}", id);
        }
    }
}