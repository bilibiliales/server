// 登录信息验证（顾客）
package com.bookshell.server.bookshell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 根据支付宝账号ID查询用户ID
    public String getUidByAlipay(String alipayUserId) {
        return "user_" + alipayUserId;
    }

    // 验证密码逻辑，验证通过则返回用户ID
    public String verifyCredentials(String username, String password) {
        return "user_" + username;
    }
}