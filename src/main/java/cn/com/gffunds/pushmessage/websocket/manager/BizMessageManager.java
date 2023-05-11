package cn.com.gffunds.pushmessage.websocket.manager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
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
    private AntPathMatcher matcher = new AntPathMatcher();

    public BizMessageManager(String bizId, Set<String> topics) {
        this.bizId = bizId;
        this.topics = topics;
    }

    /**
     * 添加topics
     */
    public void addTopics(Set<String> topics) {
        this.topics.addAll(topics);
    }

    /**
     * 移除topics
     */
    public Set<String> removeTopics(Set<String> topicSet) {
        Set<String> removeSet = new HashSet<>();
        for (String topic : topicSet) {
            // 通配符匹配删除主题
            for (String subscribedTopic : topics) {
                if (matcher.match(topic, subscribedTopic)) {
                    removeSet.add(subscribedTopic);
                }
            }
        }
        this.topics.removeAll(removeSet);
        return removeSet;
    }

    /**
     * 判断订阅列表是否为空
     */
    public boolean isEmpty() {
        return CollectionUtils.isEmpty(topics);
    }
}
