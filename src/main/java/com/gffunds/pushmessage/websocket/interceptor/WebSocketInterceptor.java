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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Data
public class WebSocketInterceptor extends HttpSessionHandshakeInterceptor {

    @Value("${websocket.authentication.enable:true}")
    private boolean enable;
    @Resource
    private GFHttpClient ssoGfHttpClient;

    public WebSocketInterceptor() {
        this.setCopyAllAttributes(true);
        this.setCreateSession(true);
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        log.info("===========握手之前=============");
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        // 根据sessionId获取用户信息
        String sessionId = servletRequest.getHeader(WebSocketConstants.SESSION_ID_HEADER);
        // 校验sessionId
        if (enable) {
            // 对接sso校验并获取用户信息
            Map<String, String> map = new HashMap<>();
            map.put(WebSocketConstants.SESSION_ID, sessionId);
            HttpClientResult<ReturnResult> rs = ssoGfHttpClient.doPostForJson(WebSocketConstants.AUTHORIZATION_URL, null, JacksonUtil.toJson(map), true, ReturnResult.class);
            ReturnResult returnResult = rs.getContent();
            if (!"0".equals(returnResult.getErrorCode())) {
                throw new PushMessageException(returnResult.getErrorMsg());
            }
            UserInfo userInfo = JacksonUtil.toObject(JacksonUtil.toJson(returnResult.getData()), UserInfo.class);
            attributes.put(WebSocketConstants.ATTR_USER, userInfo);
        }
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