package com.bookshell.server.MsgBoard;

import com.bookshell.server.MsgBoard.setting.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MsgBoardService {
    private static final String MESSAGES_ZSET = "messages:zset";
    private static final String MESSAGE_PREFIX = "message:";
    private static final String CONFIG_HASH = "config:blacklist";
    private static final String TOP_MESSAGE_KEY = "top:message";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public boolean addMessage(String nickname, String text, boolean isAdmin){
        // 检查昵称黑名单
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("blacklist:nickname", nickname))) {
            return false;
        }
        // 检查消息黑名单
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("blacklist:msg", text))) {
            // 检查是否启用自动拉黑
            if (Boolean.TRUE.equals(getConfig("autoBlocked"))) {
                redisTemplate.opsForSet().add("blacklist:nickname", nickname);
            }
            return false;
        }
        // 检查是否开启全局禁止留言（管理员不受限制）
        if (!isAdmin && Boolean.TRUE.equals(getConfig("msgDisabled"))) {
            return false;
        }

        // 存储消息
        String messageId = UUID.randomUUID().toString();
        Message message = new Message();
        message.setNickname(nickname);
        message.setMessage(text);
        message.setTimestamp(LocalDateTime.now());

        redisTemplate.opsForHash().put(MESSAGE_PREFIX + messageId, "data", message);
        redisTemplate.opsForZSet().add(MESSAGES_ZSET, messageId,
                message.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        // 自动维护100条最新消息
        redisTemplate.opsForZSet().removeRange(MESSAGES_ZSET, 0, -101);
        return true;
    }

    public List<Message> getRecentMessages() {
        String topId = this.getTopMessageId();
        Set<Object> allIds = redisTemplate.opsForZSet().reverseRange(MESSAGES_ZSET, 0, 100);

        return allIds.stream()
                .map(id -> (String) id)  // 明确转换为String类型
                .filter(id -> !id.equals(topId))  // 排除置顶消息
                .limit(100)  // 最终限制数量
                .map(this::getMessageById)  // 转换为Message对象
                .filter(Objects::nonNull)  // 过滤无效消息
                .collect(Collectors.toList());
    }

    // 查询置顶消息ID
    public String getTopMessageId() {
        return (String) redisTemplate.opsForValue().get(TOP_MESSAGE_KEY);
    }

    // 查询置顶消息
    public Message getTopMessage() {
        String messageId = (String) redisTemplate.opsForValue().get(TOP_MESSAGE_KEY);
        if (messageId != null) {
            return getMessageById(messageId);
        }
        return null;
    }

    // 查询单条消息
    public Message getMessageById(String messageId) {
        return (Message) redisTemplate.opsForHash().get(MESSAGE_PREFIX + messageId, "data");
    }


    /********************************************管理员功能********************************************/
    // 修改置顶消息
    public void updateTopMessage(String messageId, boolean isAdmin) {
        validateAdmin(isAdmin);
        redisTemplate.opsForValue().set(TOP_MESSAGE_KEY, messageId);
    }
    // 修改屏蔽词(屏蔽词集合、类型填nickname或msg、管理员签名)
    public void updateBlacklist(Set<String> words, String type, boolean isAdmin) {
        validateAdmin(isAdmin);
        redisTemplate.opsForSet().add("blacklist:" + type, words.toArray());
    }
    // 修改配置(字段填msgDisabled或autoBlocked、修改后状态、管理员签名)
    public void updateConfig(String key, Boolean value, boolean isAdmin) {
        validateAdmin(isAdmin);
        redisTemplate.opsForHash().put(CONFIG_HASH, key, value);
    }
    // 删除留言
    public void deleteMessage(String messageId, boolean isAdmin) {
        validateAdmin(isAdmin);
        redisTemplate.opsForZSet().remove(MESSAGES_ZSET, messageId);
        redisTemplate.delete(MESSAGE_PREFIX + messageId);
    }

    // 获取配置项
    private Boolean getConfig(String key) {
        return (Boolean) redisTemplate.opsForHash().get(CONFIG_HASH, key);
    }
    // 获取帖子ID
    private List<Message> getMessagesByIds(Set<Object> messageIds) {
        List<Message> messages = new ArrayList<>();
        messageIds.forEach(id -> {
            Message message = (Message) redisTemplate.opsForHash().get(MESSAGE_PREFIX + id, "data");
            if (message != null) messages.add(message);
        });
        return messages;
    }
    // 验证管理员
    private void validateAdmin(boolean isAdmin) {
        if (!isAdmin) throw new SecurityException("管理员验签失败");
    }
}
