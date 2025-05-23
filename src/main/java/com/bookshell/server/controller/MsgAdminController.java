package com.bookshell.server.controller;

import com.bookshell.server.MsgBoard.MsgBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/msg/admin")
public class MsgAdminController {
    // 管理员配置接口组
    @Autowired
    private MsgBoardService msgBoardService;

    // 返回类
    private static class ApiResponse {
        private boolean success;

        public ApiResponse(boolean success) {
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }
    }

    // 全局异常处理
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse(false));
    }

    // 更新全局禁言
    @PostMapping("/global-mute")
    public ResponseEntity<ApiResponse> setGlobalMute(
            @RequestParam boolean enabled,
            @RequestHeader("X-Admin-Token") String token
    ) {
        try {
            boolean isValidAdmin = validateAdminToken(token);
            msgBoardService.updateConfig("msgDisabled", enabled, validateAdminToken(token));
            return ResponseEntity.ok(new ApiResponse(true));
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足", e);
        }
    }

    // 更新自动拉黑
    @PostMapping("/auto-block")
    public ResponseEntity<ApiResponse> setAutoBlock(
            @RequestParam boolean enabled,
            @RequestHeader("X-Admin-Token") String token
    ) {
        try {
            boolean isValidAdmin = validateAdminToken(token);
            msgBoardService.updateConfig("autoBlocked", enabled, validateAdminToken(token));
            return ResponseEntity.ok(new ApiResponse(true));
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足", e);
        }
    }

    // 查询昵称黑名单
    @GetMapping("/nickname-blacklist")
    public ResponseEntity<Set<String>> getNicknameBlacklist(
            @RequestHeader("X-Admin-Token") String token) {
        try {
            boolean isValidAdmin = validateAdminToken(token);
            return ResponseEntity.ok(msgBoardService.getBlacklist("nickname", isValidAdmin));
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足", e);
        }
    }

    // 查询消息黑名单
    @GetMapping("/message-blacklist")
    public ResponseEntity<Set<String>> getMessageBlacklist(
            @RequestHeader("X-Admin-Token") String token) {
        try {
            boolean isValidAdmin = validateAdminToken(token);
            return ResponseEntity.ok(msgBoardService.getBlacklist("msg", isValidAdmin));
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足", e);
        }
    }

    // 更新昵称黑名单
    @PostMapping("/nickname-blacklist")
    public ResponseEntity<ApiResponse> updateNicknameBlacklist(
            @RequestBody Set<String> words,
            @RequestHeader("X-Admin-Token") String token
    ) {
        try {
            boolean isValidAdmin = validateAdminToken(token);
            msgBoardService.updateBlacklist(words, "nickname", isValidAdmin);
            return ResponseEntity.ok(new ApiResponse(true));
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足", e);
        }
    }

    // 更新消息黑名单
    @PostMapping("/message-blacklist")
    public ResponseEntity<ApiResponse> updateMessageBlacklist(
            @RequestBody Set<String> phrases,
            @RequestHeader("X-Admin-Token") String token
    ) {
        try {
            boolean isValidAdmin = validateAdminToken(token);
            msgBoardService.updateBlacklist(phrases, "msg", isValidAdmin);
            return ResponseEntity.ok(new ApiResponse(true));
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足", e);
        }
    }

    // 设置置顶消息
    @PostMapping("/top-message")
    public ResponseEntity<ApiResponse> setTopMessage(
            @RequestParam String messageId,
            @RequestHeader("X-Admin-Token") String token
    ) {
        try {
            boolean isValidAdmin = validateAdminToken(token);
            msgBoardService.updateTopMessage(messageId, isValidAdmin);
            return ResponseEntity.ok(new ApiResponse(true));
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足", e);
        }
    }

    // 删除留言
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse> deleteMessage(
            @PathVariable String messageId,
            @RequestHeader("X-Admin-Token") String token
    ) {
        try {
            boolean isValidAdmin = validateAdminToken(token);
            msgBoardService.deleteMessage(messageId, isValidAdmin);
            return ResponseEntity.ok(new ApiResponse(true));
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足", e);
        }
    }

    // 管理员验签
    private boolean validateAdminToken(String token) {
        return token != null && token.equals("SECRET_ADMIN_KEY");
    }
}
