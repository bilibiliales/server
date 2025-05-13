package com.alipay.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.domain.AlipayTradePagePayModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.alipay.server.config.AlipayConfig;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.AlipayApiException;

import static com.alipay.api.AlipayConstants.CHARSET;

@RestController
@RequestMapping("/alipay")
public class AppController {
    @Autowired
    private AlipayConfig alipayConfig;

    @PostMapping("/createTrade")
    public void createTrade(
            //@RequestParam String goodsName,
            @RequestParam String userId,
            HttpServletResponse response) {

        try {
            //获取商品价格
            Double totalAmount = 30.00;
            //生成订单号
            String outTradeNo = "zdjlales" + System.currentTimeMillis() + userId;
            //商品名称（测试）
            String goodsName = "30天VIP";
            //构造请求体
            AlipayClient alipayClient = new DefaultAlipayClient(
                    "https://openapi-sandbox.dl.alipaydev.com/gateway.do",
                    alipayConfig.getAppId(),
                    alipayConfig.getAppPrivateKey(),
                    "JSON",
                    "UTF-8",
                    alipayConfig.getAlipayPublicKey(),
                    "RSA2"
            );
            //构造订单信息
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(outTradeNo);
            model.setTotalAmount(totalAmount.toString());
            model.setSubject(goodsName);
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            //发起支付宝请求
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setBizModel(model);
            request.setNotifyUrl(alipayConfig.getNotifyUrl());
            request.setReturnUrl(alipayConfig.getReturnUrl());
            try {
                String form = alipayClient.pageExecute(request, "GET").getBody();
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(form);
                response.getWriter().flush();
                response.getWriter().close();
                System.out.println(response);
            } catch (AlipayApiException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", "Internal Server Error");
            error.put("message", e.getMessage());
        }
    }
}