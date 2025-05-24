package com.bookshell.server.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.bookshell.server.config.XfxhConfig;
import com.bookshell.server.alipay.CreateTrade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bookshell.server.config.AlipayConfig;
import com.bookshell.server.xfxh.BookDigest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AppController {
    private final CreateTrade createTrade;
    private final AlipayConfig alipayConfig;
    private final XfxhConfig xfxhConfig;
    private final BookDigest bookDigest;

    @Autowired
    public AppController(CreateTrade createTrade, AlipayConfig alipayConfig,
                         XfxhConfig xfxhConfig, BookDigest bookDigest) {
        this.createTrade = createTrade;
        this.alipayConfig = alipayConfig;
        this.xfxhConfig = xfxhConfig;
        this.bookDigest = bookDigest;
    }

    @PostMapping("/getBookDigest")
    public String getBookDigest(@RequestParam String bookName){
        try{
            return bookDigest.sendPostRequest(bookName, xfxhConfig.getAPIPassword());
        } catch (Exception e) {
            throw new RuntimeException("获取图书摘要失败", e);
        }
    }

    @PostMapping("/createTrade")
    public void createTrade(
            @RequestParam String userId,
            HttpServletResponse response) {
        try {
            // 生成订单号
            String outTradeNo = "bookshell" + System.currentTimeMillis() + userId;
            // 商品名称（测试）
            String goodsName = "商品名称";
            // 商品价格
            double totalAmount = 30.00;

            createTrade.createTrade(outTradeNo, totalAmount, goodsName, response);
        } catch (Exception e) {
            throw new RuntimeException("创建支付宝交易失败", e);
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
                System.out.println("验签通过");
            }
        } catch (Exception e) {
            throw new Exception("验签不通过");
        }
    }

    @GetMapping("/return")
    public String returnTrade() {
        return "success";
    }
}