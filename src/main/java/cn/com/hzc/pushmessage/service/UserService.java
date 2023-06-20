package cn.com.hzc.pushmessage.service;

import cn.com.gffunds.httpclient.client.GFHttpClient;
import cn.com.hzc.pushmessage.websocket.entity.UserInfo;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    @Resource
    private RedisTemplate defaultRestTemplate;
    @Value("${websocket.authentication.expire:30}")
    private long expireTime;

    /**
     * 请求sso鉴权获取用户信息
     */
    @SuppressWarnings("unchecked")
    public String gettoken(String username) {
        // 构建token
        String uuid = IdUtil.randomUUID().replace("-", "");
        // todo 从数据库获取用户信息
        UserInfo userInfo =  new UserInfo().setUserId("999").setNickname("何智聪").setUsername("wayne");
        defaultRestTemplate.opsForValue().set(uuid, userInfo, expireTime, TimeUnit.SECONDS);
        return uuid;
    }


    /**
     * token换取用户信息
     */
    @SuppressWarnings("unchecked")
    public UserInfo getUserInfo(String token) {
        UserInfo userInfo = null;
        try {
            userInfo = (UserInfo) defaultRestTemplate.opsForValue().get(token);
        } catch (Exception e) {
            String msg = "token获取用户信息失败，请检查redis配置和token！";
            log.error(msg);
        }
        // 消费后删除
        if (userInfo != null) {
            defaultRestTemplate.delete(token);
        } else {
            String msg = "token获取用户信息为空，请检查token！";
            log.error(msg);
        }
        return userInfo;
    }

}
