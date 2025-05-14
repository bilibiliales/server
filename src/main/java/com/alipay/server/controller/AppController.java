package com.alipay.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alipay.server.config.AlipayConfig;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.server.payment.RequestTrade;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
public class AppController {
    @Autowired
    private AlipayConfig alipayConfig;

    //构造支付宝配置
    AlipayClient alipayClient = new DefaultAlipayClient(
            "https://openapi-sandbox.dl.alipaydev.com/gateway.do",
            alipayConfig.getAppId(),
            alipayConfig.getAppPrivateKey(),
            "JSON",
            "UTF-8",
            alipayConfig.getAlipayPublicKey(),
            "RSA2"
    );

    @PostMapping("/createTrade")
    public void createTrade(
            //@RequestParam String goodsName,
            @RequestParam String userId,
            HttpServletResponse response) {
        try {
            //获取商品价格
            double totalAmount = 30.00;
            //生成订单号
            String outTradeNo = "zdjlales" + System.currentTimeMillis() + userId;
            //商品名称（测试）
            String goodsName = "30天VIP";
            //发起支付
            RequestTrade requestTrade = new RequestTrade(outTradeNo,totalAmount,goodsName,response,alipayClient,alipayConfig.getNotifyUrl(),alipayConfig.getReturnUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/notify")
    public void notify(
            @RequestParam String out_trade_no,
            @RequestParam String trade_status,
            HttpServletRequest request) throws Exception {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                params.put(name, request.getParameter(name));
            }
            boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), "UTF-8" , "RSA2");
            if(signVerified) {
                System.out.println("sign success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/return")
    public String returnTrade() {
        return "success";
    }

}