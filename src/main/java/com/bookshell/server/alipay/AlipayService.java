package com.bookshell.server.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.bookshell.server.config.AlipayConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AlipayService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;

    @Autowired
    public AlipayService(AlipayClient alipayClient, AlipayConfig alipayConfig) {
        this.alipayClient = alipayClient;
        this.alipayConfig = alipayConfig;
    }

    /**
     * 创建支付宝交易
     *
     * @param outTradeNo  商户订单号
     * @param totalAmount 订单金额
     * @param goodsName   商品名称
     * @param httpResponse HttpServletResponse对象
     * @throws AlipayApiException 支付宝API异常
     * @throws IOException        IO异常
     */
    public void createTrade(String outTradeNo, double totalAmount, String goodsName, HttpServletResponse httpResponse)
            throws AlipayApiException, IOException {

        // 构造订单信息
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(String.format("%.2f", totalAmount));
        model.setSubject(goodsName);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");

        // 构造请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        // 执行请求
        AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "GET");
        if (response.isSuccess()) {
            writeResponse(response.getBody(), httpResponse);
        } else {
            throw new AlipayApiException("支付宝交易创建失败: " + response.getSubMsg());
        }
    }

    private void writeResponse(String form, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(form);
        response.getWriter().flush();
        response.getWriter().close();
    }
}