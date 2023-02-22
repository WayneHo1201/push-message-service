package cn.com.gffunds.pushmessage.websocket.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author hezhc
 * @date 2023/2/14 15:58
 * @description 客户端请求
 */
@Data
@Accessors(chain = true)
public class MessageRequest {
    /** 消息id */
    private String msgId; 
    /** 命令，subscribe/unSubscribe */
    private String command; 
    /** 订阅或退订信息 */
    private List<BizTopic> bizTopics;
}
