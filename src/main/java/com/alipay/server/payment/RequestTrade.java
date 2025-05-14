package com.alipay.server.payment;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RequestTrade {
    public RequestTrade(String outTradeNo, double totalAmount, String goodsName, HttpServletResponse response, AlipayClient alipayClient, String notifyUrl, String returnUrl) throws AlipayApiException {
        //构造订单信息
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(Double.toString(totalAmount));
        model.setSubject(goodsName);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        //发起支付宝请求
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);
        try {
            String form = alipayClient.pageExecute(request, "GET").getBody();
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(form);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (AlipayApiException | IOException e) {
            e.printStackTrace();
        }
    }
}
