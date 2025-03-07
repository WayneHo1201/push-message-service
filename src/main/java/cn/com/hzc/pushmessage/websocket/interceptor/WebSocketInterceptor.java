package cn.com.hzc.pushmessage.websocket.interceptor;

import cn.com.hzc.pushmessage.service.UserService;
import cn.com.hzc.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.hzc.pushmessage.websocket.entity.UserInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Data
public class WebSocketInterceptor extends HttpSessionHandshakeInterceptor {

    @Autowired
    private UserService userService;


    public WebSocketInterceptor() {
        this.setCopyAllAttributes(true);
        this.setCreateSession(true);
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        // 根据token获取用户信息
        String token = servletRequest.getParameter(WebSocketConstants.TOKEN);
        // 校验sessionId
        log.info("握手之前，开始校验token");
        UserInfo userInfo = userService.getUserInfo(token);
        if (userInfo != null) {
            attributes.put(WebSocketConstants.ATTR_USER, userInfo);
        }

        //从request里面获取对象，存放attributes
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception ex) {
        super.afterHandshake(request, response, wsHandler, ex);
    }
}