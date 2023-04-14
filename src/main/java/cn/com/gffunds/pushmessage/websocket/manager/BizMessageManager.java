package cn.com.gffunds.pushmessage.websocket.manager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

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
    private Set<String> topics;

    /**
     * 添加topics
     */
    public void addTopics(Set<String> topics) {
        this.topics.addAll(topics);
    }

    /**
     * 移除topics
     */
    public void removeTopics(Set<String> topics) {
        this.topics.removeAll(topics);
    }
}
