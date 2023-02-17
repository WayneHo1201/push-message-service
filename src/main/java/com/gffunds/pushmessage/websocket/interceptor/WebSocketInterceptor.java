package com.gffunds.pushmessage.websocket.interceptor;

import cn.com.gffunds.commons.json.JacksonUtil;
import cn.com.gffunds.httpclient.client.GFHttpClient;
import cn.com.gffunds.httpclient.entity.HttpClientResult;
import com.gffunds.pushmessage.common.ReturnResult;
import com.gffunds.pushmessage.exception.PushMessageException;
import com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import com.gffunds.pushmessage.websocket.entity.UserInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Data
public class WebSocketInterceptor extends HttpSessionHandshakeInterceptor {

    @Value("${proxy.sso.enable}")
    private boolean enable;
    @Autowired
    private GFHttpClient gfHttpClient;

    public WebSocketInterceptor() {
        this.setCopyAllAttributes(true);
        this.setCreateSession(true);
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("===========握手之前=============");
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        //  根据sessionId获取用户信息
        String sessionId = servletRequest.getHeader(WebSocketConstants.SESSION_ID);
        UserInfo userInfo;
        // 校验sessionId
        if (enable) {
            // todo 对接sso校验并获取用户信息
            Map<String, String> map = new HashMap<>();
            map.put("sessionId", sessionId);
            HttpClientResult<ReturnResult> rs = gfHttpClient.doPostForJson("/sso/authorize", null, JacksonUtil.toJson(map), true, ReturnResult.class);
            ReturnResult returnResult = rs.getContent();
            if (!"0".equals(returnResult.getErrorCode())) {
                throw new PushMessageException(returnResult.getErrorMsg());
            }
            userInfo = JacksonUtil.toObject(JacksonUtil.toJson(returnResult.getData()), UserInfo.class);
        } else {
            userInfo = new UserInfo().setSessionId(sessionId).setUsername("guxh"); // for test
        }
        attributes.put(WebSocketConstants.ATTR_USER, userInfo);
        //从request里面获取对象，存放attributes
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception ex) {
        log.info("===========握手之后=============");
        super.afterHandshake(request, response, wsHandler, ex);
    }


}