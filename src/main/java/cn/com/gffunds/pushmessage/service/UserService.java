package cn.com.gffunds.pushmessage.service;

import cn.com.gffunds.commons.json.JacksonUtil;
import cn.com.gffunds.httpclient.client.GFHttpClient;
import cn.com.gffunds.httpclient.entity.HttpClientResult;
import cn.com.gffunds.pushmessage.common.ReturnResult;
import cn.com.gffunds.pushmessage.common.enumeration.ErrCodeEnum;
import cn.com.gffunds.pushmessage.exception.PushMessageException;
import cn.com.gffunds.pushmessage.websocket.constants.WebSocketConstants;
import cn.com.gffunds.pushmessage.websocket.entity.UserInfo;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author hezhc
 * @date 2023/2/27 10:07
 * @description 用户服务
 */
@Service
@Slf4j
public class UserService {
    @Resource
    private GFHttpClient ssoGfHttpClient;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Value("${websocket.authentication.expire:30}")
    private long expireTime;

    /**
     * 请求sso鉴权获取用户信息
     */
    @SuppressWarnings("unchecked")
    public String gettoken(String sessionId) {
        // 对接sso校验并获取用户信息
        Map<String, String> map = new HashMap<>();
        map.put(WebSocketConstants.SESSION_ID, sessionId);
        HttpClientResult<ReturnResult> rs = ssoGfHttpClient.doPostForJson(WebSocketConstants.AUTHORIZATION_URL, null, JacksonUtil.toJson(map), true, ReturnResult.class);
        ReturnResult returnResult = rs.getContent();
        if (!"0".equals(returnResult.getErrorCode())) {
            throw new PushMessageException(returnResult.getErrorMsg(), ErrCodeEnum.TOKEN_INCORRECT.code());
        }
        // 构建token
        String uuid = IdUtil.randomUUID().replace("-", "");
        // 获取用户信息
        UserInfo userInfo = JacksonUtil.toObject(JacksonUtil.toJson(returnResult.getData()), UserInfo.class);
        redisTemplate.opsForValue().set(uuid, userInfo, expireTime, TimeUnit.SECONDS);
        return uuid;
    }


    /**
     * token换取用户信息
     */
    @SuppressWarnings("unchecked")
    public UserInfo getUserInfo(String token) {
        UserInfo userInfo = null;
        try {
            userInfo = (UserInfo) redisTemplate.opsForValue().get(token);
        } catch (Exception e) {
            String msg = "token获取用户信息失败，请检查redis配置和token！";
            log.error(msg);
        }
        // 消费后删除
        if (userInfo != null) {
            redisTemplate.delete(token);
        } else {
            String msg = "token获取用户信息为空，请检查token！";
            log.error(msg);
        }
        return userInfo;
    }

}
