package com.gffunds.pushmessage.websocket.manager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hezhc
 * @date 2023/2/14 16:38
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BizMessageManager {
    private String bizId;
    private List<String> topics;
}
