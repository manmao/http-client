package com.china.http.client;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

/**
 * Http客户端重试判断处理
 *
 * @author manmao
 * @since 2019-03-11
 */
public class SimpleHttpRequestRetryHandler implements HttpRequestRetryHandler {

    private static Logger logger = LoggerFactory.getLogger(SimpleHttpRequestRetryHandler.class);

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_TIME = 3;

    @Override
    public boolean retryRequest(IOException e, int executionCount, HttpContext context) {
        if (executionCount > MAX_RETRY_TIME) {
            //重试超过3次,放弃请求
            logger.error("retry has more than 3 time, give up request", e);
            return false;
        }
        if (e instanceof NoHttpResponseException) {
            //服务器没有响应,可能是服务器断开了连接,应该重试
            logger.error("receive no response from server, retry", e);
            return true;
        }
        if (e instanceof SSLHandshakeException) {
            // SSL握手异常
            logger.error("SSL hand shake exception", e);
            return false;
        }
        if (e instanceof InterruptedIOException) {
            //超时
            logger.error("InterruptedIOException", e);
            return false;
        }
        if (e instanceof UnknownHostException) {
            // 服务器不可达
            logger.error("server host unknown", e);
            return false;
        }
        if (e instanceof SSLException) {
            logger.error("SSLException", e);
            return false;
        }

        HttpClientContext httpContext = HttpClientContext.adapt(context);
        HttpRequest request = httpContext.getRequest();
        // 如果请求不是关闭连接的请求
        return !(request instanceof HttpEntityEnclosingRequest);
    }
}
