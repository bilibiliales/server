项目结构：
    controller类：设置各个controller路由
    产品名/Service类：设置各个实际处理逻辑
    config类：定义各个产品的DTO及配置文件数据

验签规则：
    现已启用jwt签名验证，如需正常访问被加入拦截器的controller需要在前端包含有效的签名，示例：
    Authorization: Bearer <JWT>

互联登录：
使用支付宝互联登录：
    流程：用户选择支付宝授权登录创建登录请求ID->用户使用URL跳转登录支付宝->支付宝回调消息包含访问令牌->使用访问令牌获取OpenID(支付宝账号唯一凭证)->验证AppID后返回登录会话ID
    登录URL：https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=APPID&scope=SCOPE&redirect_uri=ENCODED_URLstate=STATE
    *其中APPID可以在AlipayConfig.java中获取，ENCODED_URL设置为Controller层处理登录请求的路由，STATE会原封不动的返回，需设置为登录请求ID

    使用登录请求ID生成登录URL：
        方法：GET.
        请求地址：http://localhost:8080/auth/alipay
        返回示例：
        {
            "auth_url": "https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2021004113670539&scope=auth_user&redirect_uri=http%3A%2F%2F10.200.92.146%3A8080%2Fauth%2Fcallback&state=dd79ce5a-b8d1-4bc0-96e9-528e036608c2"
        }

    访问登录URL：
        方法：GET
        请求地址：登录URL
        示例请求：https://openauth.alipay.com/oauth2/publicAppAuthorize.htm?app_id=2021004113670539&scope=auth_user&redirect_uri=http%3A%2F%2F10.200.92.146%3A8080%2Fauth%2Fcallback&state=dd79ce5a-b8d1-4bc0-96e9-528e036608c2
        支付宝回调示例：
        {
          auth_code: '9786bd68a4724d8f8c1e93bf3b25MX83',
          state: '123456789',
          app_id: '2021004113670539',
          source: 'alipay_wallet',
          userOutputs: 'auth_user',
          scope: 'auth_user',
          alipay_token: ''
        }
        支付宝返回地址示例：http://10.200.92.146:8080/auth/callback?auth_code=f0ab1a40283c4d1ca62f594552abFD83&state=dd79ce5a-b8d1-4bc0-96e9-528e036608c2&app_id=2021004113670539&source=alipay_wallet&userOutputs=auth_user&scope=auth_user&alipay_token=
    登录回调URL：
        方法：登录成功自动回调（GET）
        请求地址：http://localhost:8080/auth/callback
        请求体携带参数（回调时自动填写）：auth_code、state、app_id、source、userOutputs、scope
        示例换取access_token和openid结果：
        {
            "alipay_system_oauth_token_response":{
                "access_token":"authusrB60c6c42b28524c2d9fc52835c3a4cX83",
                "auth_start":"2025-05-24 20:18:53",
                "expires_in":1296000,
                "re_expires_in":2592000,
                "refresh_token":"authusrB5b807e61b5b54125a834a3f10c499X83",
                "open_id":"083dALza2Yowfx4P-z2Ut1FpAQjtjJ-_sW8m87-yqXIfKsb"
            },
            "sign":"L86RghAPSOwDr7K07Xj12mbGYnPMcaoZReU+vB91BD0SU6mV+Q/pBk29G5t9Xozv0T3uKQC8ut5Wp+9N3KlJIEnzfXNHeGjSqvbO4CZkhuJqJidiAdoBHzr3mBoepCLntq8VjKstTdxUhmSgj3i7tik04lCtcdc60p/le8Hytc/jf+dJg7/vWGjfCQvzoAm8gVpKnGRHPVdArbTHSpjpbxqbQhupAJiCtT8BT0eNZVxy3zQKQY87OqWMT1MnbvRHCqU1OK5nPMT0jiSoqy+HPfJ/oO9jYcyueMj+fAgTWZvk9FhquJDI3rD8niMPll8dZBI02XdeAPZn1soIFArpgQ=="
        }
        示例使用access_token查询昵称和头像结果：
使用用户名+密码登录：


使用留言板API：
    获取留言板信息：
        方法：GET，
        请求地址：http://localhost:8080/api/msg/messages
        返回示例：
        {
            "topMessage": {
                "id": "a39f2f8d-139d-420c-bd5a-727b1c4db0fb",
                "nickname": "示例管理员",
                "content": "测试置顶消息1",
                "timestamp": "2025-05-23T23:26:54.6065932",
                "timestampAsInstant": "2025-05-23T15:26:54.606593200Z"
            },
            "recentMessages": [
                {
                    "id": "309b0a53-b930-4533-b977-7e5de15e964b",
                    "nickname": "测试用户2",
                    "content": "测试留言内容2",
                    "timestamp": "2025-05-23T23:22:38.9628834",
                    "timestampAsInstant": "2025-05-23T15:22:38.962883400Z"
                },
                {
                    "id": "9c5e29c7-5d19-4835-a0b4-5a664e354f78",
                    "nickname": "测试用户",
                    "content": "测试留言内容",
                    "timestamp": "2025-05-23T23:22:35.0873767",
                    "timestampAsInstant": "2025-05-23T15:22:35.087376700Z"
                }
            ]
        }
    新增留言板信息：
        方法：POST
        请求地址：http://localhost:8080/api/msg/messages
        请求头：
            必填：
            Content-Type: application/json
            选填（普通用户可不填，填写正确后可以发送有管理员权限标识的留言）：
            X-Admin-Token: JWT生成的token，正确令牌拥有管理员权限（测试时填写"SECRET_ADMIN_KEY"）
        请求体示例：
        {
            "nickname": "测试用户",
            "content": "测试留言内容"
        }
        正常返回/全局禁言后管理员权限用户示例：
        {
            "success": true
        }
        全局禁言后普通用户/管理员验签错误返回示例：
        {
            "success": false
        }
        触发屏蔽词请求体示例：
        {
            "nickname": "pbc1",
            "content": "测试留言内容5"
        }
        触发屏蔽词请求体示例2：
        {
            "nickname": "测试用户5",
            "content": "pbc1"
        }
        触发屏蔽词请求体示例3：
        {
            "nickname": "pbc1",
            "content": "pbc2"
        }
        触发屏蔽词请求体示例4（开启自动屏蔽后被自动屏蔽）：
        {
            "nickname": "测试用户5",
            "content": "测试留言内容5"
        }
        以上四类情况普通用户/管理员验签错误返回示例：
        {
            "success": false
        }
        以上四类情况管理员权限用户示例：
        {
            "success": true
        }
    设置留言板置顶信息：
        方法：POST
        请求地址：http://localhost:8080/api/msg/admin/top-message
        请求头：
            必填：
            Content-Type: application/x-www-form-urlencoded
            X-Admin-Token: SECRET_ADMIN_KEY
        请求体示例：
            设置置顶留言：
            messageId: a39f2f8d-139d-420c-bd5a-727b1c4db0fb
            清除置顶留言：
            messageId: （空）
        正常返回示例：
        状态：200 OK
        {
            "success": true
        }
        验签错误返回示例：
        状态：403 Forbidden
        {
            "success": false
        }
    删除留言信息：
        方法：DELETE
        请求地址：http://localhost:8080/api/msg/admin/messages/{messageId}
        请求头：
            必填：
            X-Admin-Token: SECRET_ADMIN_KEY
        请求示例：http://localhost:8080/api/msg/admin/messages/22ca304c-9682-48fb-ba29-fd997d19fdbf
        正常返回示例：
        {
            "success": true
        }
        不包含验签返回示例：
        {
            "timestamp": "2025-05-23T16:06:48.958+00:00",
            "status": 400,
            "error": "Bad Request",
            "path": "/api/msg/admin/messages/22ca304c-9682-48fb-ba29-fd997d19fdbf"
        }
        验签错误返回示例：
        状态：403 Forbidden
        {
            "success": false
        }
    更新全局禁言：
        方法：POST
        请求地址：http://localhost:8080/api/msg/admin/global-mute
        请求头：
            必填：
            Content-Type: application/x-www-form-urlencoded
            X-Admin-Token: SECRET_ADMIN_KEY
        请求体：
            enabled: 布尔类型
        请求体示例（开启全局禁止发帖）：
            enabled: true
        正常返回示例：
        {
            "success": true
        }
    查询昵称屏蔽词：
        方法：GET
        请求地址：http://localhost:8080/api/msg/admin/nickname-blacklist
        请求头：
            必填：
            X-Admin-Token: SECRET_ADMIN_KEY
        正常返回示例：
        [
            "pbc1",
            "pbc2"
        ]
    覆盖修改昵称屏蔽词（完全匹配）：
        方法：POST
        请求地址：http://localhost:8080/api/msg/admin/nickname-blacklist
        请求头：
            必填：
            Content-Type: application/json
            X-Admin-Token: SECRET_ADMIN_KEY
        请求体示例：
            ["pbc1","pbc2"]
        正常返回示例：
        {
            "success": true
        }
    查询消息屏蔽词：
        方法：GET
        请求地址：http://localhost:8080/api/msg/admin/message-blacklist
        请求头：
            必填：
            X-Admin-Token: SECRET_ADMIN_KEY
        正常返回示例：
        [
            "pbc1",
            "pbc2"
        ]
    覆盖修改消息屏蔽词（包含片段（不区分大小写））：
        方法：POST
        请求地址：http://localhost:8080/api/msg/admin/message-blacklist
        请求头：
            必填：
            Content-Type: application/json
            X-Admin-Token: SECRET_ADMIN_KEY
        请求体示例：
            ["pbc1","pbc2"]
        正常返回示例：
        {
            "success": true
        }
    更新触发屏蔽词自动屏蔽昵称：
        方法：POST
        请求地址：http://localhost:8080/api/msg/admin/auto-block
        请求头：
            必填：
            Content-Type: application/x-www-form-urlencoded
            X-Admin-Token: SECRET_ADMIN_KEY
        请求体：
            enabled: 布尔类型
        请求体示例（开启全局禁止发帖）：
            enabled: true
        正常返回示例：
        {
            "success": true
        }
