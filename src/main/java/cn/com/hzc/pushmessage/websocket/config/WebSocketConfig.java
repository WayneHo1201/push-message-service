package cn.com.hzc.pushmessage.websocket.config;

import cn.com.hzc.pushmessage.websocket.handler.CommonTextWebSocketHandler;
import cn.com.hzc.pushmessage.websocket.interceptor.WebSocketInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private CommonTextWebSocketHandler commonTextWebSocketHandler;
    @Autowired
    private WebSocketInterceptor webSocketInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // handler是webSocket的核心，配置入口
        registry.addHandler(commonTextWebSocketHandler, "/websocket")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketInterceptor);
    }
}