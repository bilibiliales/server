// 文件位置: src/main/java/com/bookshell/server/controller/MsgController.java
package com.bookshell.server.controller;

import com.bookshell.server.MsgBoard.MsgBoardService;
import com.bookshell.server.MsgBoard.setting.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/msg")
public class MsgController {

    @Autowired
    private MsgBoardService msgBoardService;

    // 用户提交留言
    @PostMapping("/messages")
    public ResponseEntity<PostMessageResponse> postMessage(
            @RequestBody MessageRequest request,
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken
    ) {
        boolean isAdmin = validateAdminToken(adminToken);
        boolean success = msgBoardService.addMessage(
                request.getNickname(),
                request.getContent(),
                isAdmin
        );

        return ResponseEntity.ok(new PostMessageResponse(success));
    }

    // 获取留言列表
    @GetMapping("/messages")
    public ResponseEntity<MessageListResponse> getMessages() {
        MessageListResponse response = new MessageListResponse();
        response.setTopMessage(msgBoardService.getTopMessage());
        response.setRecentMessages(msgBoardService.getRecentMessages());
        return ResponseEntity.ok(response);
    }

    // 管理员配置接口组
    @RestController
    @RequestMapping("/admin")
    public class AdminController {

        // 更新全局禁言
        @PostMapping("/global-mute")
        public ResponseEntity<Void> setGlobalMute(
                @RequestParam boolean enabled,
                @RequestHeader("X-Admin-Token") String token
        ) {
            msgBoardService.updateConfig("msgDisabled", enabled, validateAdminToken(token));
            return ResponseEntity.ok().build();
        }

        // 更新自动拉黑
        @PostMapping("/auto-block")
        public ResponseEntity<Void> setAutoBlock(
                @RequestParam boolean enabled,
                @RequestHeader("X-Admin-Token") String token
        ) {
            msgBoardService.updateConfig("autoBlocked", enabled, validateAdminToken(token));
            return ResponseEntity.ok().build();
        }

        // 更新昵称黑名单
        @PostMapping("/nickname-blacklist")
        public ResponseEntity<Void> updateNicknameBlacklist(
                @RequestBody Set<String> words,
                @RequestHeader("X-Admin-Token") String token
        ) {
            msgBoardService.updateBlacklist(words, "nickname", validateAdminToken(token));
            return ResponseEntity.ok().build();
        }

        // 更新消息黑名单
        @PostMapping("/message-blacklist")
        public ResponseEntity<Void> updateMessageBlacklist(
                @RequestBody Set<String> phrases,
                @RequestHeader("X-Admin-Token") String token
        ) {
            msgBoardService.updateBlacklist(phrases, "msg", validateAdminToken(token));
            return ResponseEntity.ok().build();
        }

        // 设置置顶消息
        @PostMapping("/top-message")
        public ResponseEntity<Void> setTopMessage(
                @RequestParam String messageId,
                @RequestHeader("X-Admin-Token") String token
        ) {
            msgBoardService.updateTopMessage(messageId, validateAdminToken(token));
            return ResponseEntity.ok().build();
        }

        // 删除留言
        @DeleteMapping("/messages/{messageId}")
        public ResponseEntity<Void> deleteMessage(
                @PathVariable String messageId,
                @RequestHeader("X-Admin-Token") String token
        ) {
            msgBoardService.deleteMessage(messageId, validateAdminToken(token));
            return ResponseEntity.noContent().build();
        }
    }

    // 辅助方法
    private boolean validateAdminToken(String token) {
        // 实际生产环境应使用JWT验证等安全机制
        return token != null && token.equals("SECRET_ADMIN_KEY");
    }

    // DTO定义
    static class MessageRequest {
        private String nickname;
        private String content;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    static class PostMessageResponse {
        private boolean success;

        public PostMessageResponse(boolean success) {
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }

    static class MessageListResponse {
        private Message topMessage;
        private List<Message> recentMessages;

        public Message getTopMessage() {
            return topMessage;
        }

        public void setTopMessage(Message topMessage) {
            this.topMessage = topMessage;
        }

        public List<Message> getRecentMessages() {
            return recentMessages;
        }

        public void setRecentMessages(List<Message> recentMessages) {
            this.recentMessages = recentMessages;
        }
    }
}