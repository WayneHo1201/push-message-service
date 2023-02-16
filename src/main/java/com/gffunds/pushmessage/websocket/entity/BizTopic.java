package com.gffunds.pushmessage.websocket.entity;

import lombok.Data;

import java.util.List;

/**
 * @author hezhc
 * @date 2023/2/14 15:59
 * @description 业务消息实体类
 */
@Data
public class BizTopic {
    /** 业务id */
    private String bizId;
    /** 消息主题列表，支持*匹配符 */
    private List<String> topics;
}
