package com.gffunds.pushmessage.websocket.interceptor;

import com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import com.gffunds.pushmessage.websocket.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@Component
public class WebSocketInterceptor extends HttpSessionHandshakeInterceptor {

    public WebSocketInterceptor() {
        this.setCopyAllAttributes(true);
        this.setCreateSession(true);
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        //  根据sessionId获取用户信息
        String sessionId = servletRequest.getHeader("Session-Id"); // todo 获取Session-Id的方法待定
        UserInfo userInfo = new UserInfo().setSessionId(sessionId).setUsername("guxh"); // for test
        attributes.put(WebSocketConstants.ATTR_USER, userInfo);
        log.info("握手之前");
        //从request里面获取对象，存放attributes
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception ex) {
        log.info("握手之后");
        super.afterHandshake(request, response, wsHandler, ex);
    }


}