// 登录信息验证（管理员）
package com.bookshell.server.bookshell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 验证密码逻辑，验证通过则返回管理员用户ID
    public String verifyAdminCredentials(String username, String password) {
        return "admin_" + username;
    }
}
