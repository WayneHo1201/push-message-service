package cn.com.gffunds.pushmessage.authenticate;

import cn.com.gffunds.commons.json.JacksonUtil;
import cn.com.gffunds.httpclient.client.GFHttpClient;
import cn.com.gffunds.httpclient.entity.HttpClientResult;
import cn.com.gffunds.pushmessage.common.ReturnResult;
import cn.com.gffunds.pushmessage.controller.AuthenticateController;
import cn.com.gffunds.pushmessage.websocket.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: Wu Teng
 * @email: wut@gffunds.com.cn
 * @date: 2023-02-21 14:25
 * @modified By：
 * @version: 1.0.0$
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("鉴权接口单元测试")
@Slf4j
class AuthenticateTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthenticateController authenticateController;
    private static String sessionId;
    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() {
        // 构建一个controller mockMvc实例
        // Build a MockMvc instance by registering one or more @Controller instances and configuring
        // Spring MVC infrastructure programmatically.
        mockMvc = MockMvcBuilders.standaloneSetup(authenticateController).build();
    }

    @BeforeEach
    public void setSessionId() {
        // 设置请求头，包含sessionId等获取
        httpHeaders = new HttpHeaders();
        GFHttpClient ssoHttpClient = new GFHttpClient();
        ReturnResult rs = ssoHttpClient.doGet("http://10.89.187.68:8222/sso/encrypt?plaintext=test@gfjj", ReturnResult.class);
        String encrypt = (String) rs.getData();
        Map<String, String> map = new HashMap<>();
        map.put("username", "guxh");
        map.put("password", encrypt);
        HttpClientResult<ReturnResult> res = ssoHttpClient.doPostForJson("http://10.89.188.67:8086/sso/login", null, JacksonUtil.toJson(map), false, ReturnResult.class);
        ReturnResult returnResult = res.getContent();
        UserInfo userInfo = JacksonUtil.toObject(JacksonUtil.toJson(returnResult.getData()), UserInfo.class);
        sessionId = userInfo.getSessionId();
    }


    @Test
    @DisplayName("sessionId获取token接口请求方法")
    void testAuthenticate() throws Exception {
        /*
         * 1、mockMvc.perform执行一个请求。
         * 2、MockMvcRequestBuilders.get("XXX")构造一个请求。
         * 3、ResultActions.param添加请求传值
         * 4、ResultActions.accept(MediaType.APPLICATION_JSON))设置返回类型
         * 5、ResultActions.andExpect添加执行完成后的断言。
         * 6、ResultActions.andDo添加一个结果处理器，表示要对结果做点什么事情
         *   比如此处使用MockMvcResultHandlers.print()输出整个响应结果信息。
         * 7、ResultActions.andReturn表示执行完成后返回相应的结果。
         */

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .get("/gettoken?sessionId=" + sessionId)
                .headers(httpHeaders)
                // 设置返回值类型为utf-8，否则默认为ISO-8859-1
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        ReturnResult result = JacksonUtil.toObject(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()), ReturnResult.class);
        log.info("输出的结果是：" + result.toString());
        Assertions.assertEquals("00000", result.getErrorCode());
        Assertions.assertEquals("SUCCESS", result.getErrorMsg());

    }
}
