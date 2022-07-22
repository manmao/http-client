package com.china.http.client;

import com.china.http.client.model.PoolConfig;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HttpClient
 *
 * @author manmao
 * @since 2019-03-11
 */
public class HttpClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    /**
     * 默认字符编码
     */
    public static final String DEFAULT_CHARSET = "utf-8";

    /**
     * 设置建立连接的超时时间, 单位: ms
     */
    private static final int CONNECT_TIMEOUT = 10000;

    /**
     * SO_TIMEOUT 等待读取数据或者写数据的最大超时时间, 单位: ms
     */
    private static final int SOCKET_TIMEOUT = 5000;


    private final CloseableHttpClient closeableHttpClient;


    /**
     * http连接池配置
     */
    private final HttpConnectionPool httpConnectionPool = new HttpConnectionPool();

    /**
     * 连接超时时间
     */
    private final int connectionTimeout;

    /**
     * 读取数据超时时间
     */
    private final int socketReadTimeout;


    public HttpClient() {
        closeableHttpClient = httpConnectionPool.createHttpClientInstance();
        connectionTimeout = CONNECT_TIMEOUT;
        socketReadTimeout = SOCKET_TIMEOUT;
    }

    public HttpClient(PoolConfig config) {
        closeableHttpClient = httpConnectionPool.createHttpClientInstance(config);
        connectionTimeout = config.getConnectionTimeout() == 0 ? CONNECT_TIMEOUT : config.getConnectionTimeout();
        socketReadTimeout = config.getSocketReadTimeout() == 0 ? SOCKET_TIMEOUT : config.getSocketReadTimeout();
    }

    /**
     * 自定义pool配置和重试handler
     *
     * @param config              路由配置
     * @param requestRetryHandler 重试handler
     * @see SimpleHttpRequestRetryHandler
     */
    public HttpClient(PoolConfig config, HttpRequestRetryHandler requestRetryHandler) {
        closeableHttpClient = httpConnectionPool.createHttpClientInstance(config, requestRetryHandler);
        connectionTimeout = config.getConnectionTimeout() == 0 ? CONNECT_TIMEOUT : config.getConnectionTimeout();
        socketReadTimeout = config.getSocketReadTimeout() == 0 ? SOCKET_TIMEOUT : config.getSocketReadTimeout();
    }

    /**
     * GET请求返回string
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头部参数
     * @return 如果异常返回空
     */
    public String getForString(String url, Map<String, String> params, Map<String, String> headers) {
        if (StringUtils.isBlank(url)) {
            logger.error("请求url为空!!!!");
            return null;
        }
        HttpGet httpGet = this.buildHttpGetInstance(url, params, headers);
        return this.executeRequest(httpGet);
    }

    /**
     * POST 请求返回string
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头部参数
     * @return 如果异常返回空
     */
    public String postForString(String url, Map<String, String> params, Map<String, String> headers) {
        return this.postForString(url, params, null, headers);
    }

    /**
     * POST 请求返回string
     *
     * @param url     请求地址
     * @param headers 请求头部参数
     * @return 如果异常返回空
     */
    public String postForString(String url, String body, Map<String, String> headers) {
        return this.postForString(url, null, body, headers);
    }

    /**
     * POST 请求返回string
     *
     * @param url     请求地址
     * @param headers 请求头部参数
     * @return 如果异常返回空
     */
    public String postForString(String url, Map<String, String> params, String body, Map<String, String> headers) {
        if (StringUtils.isBlank(url)) {
            logger.error("request url is blank !!!!");
            return null;
        }
        HttpPost httpPost;
        if (StringUtils.isNotBlank(body)) {
            httpPost = this.buildHttpPostInstance(url, null, body, headers);
        } else {
            httpPost = this.buildHttpPostInstance(url, params, null, headers);
        }

        return this.executeRequest(httpPost);
    }


    /**
     * PUT 请求返回string
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头部参数
     * @return 如果异常返回空
     */
    public String putForString(String url, Map<String, String> params, String body, Map<String, String> headers) {
        if (StringUtils.isBlank(url)) {
            logger.error("请求url为空!!!!");
            return null;
        }
        HttpPut httpPut = this.buildHttpPutInstance(url, params, body, headers);
        return this.executeRequest(httpPut);
    }


    /**
     * 执行http请求的方法
     *
     * @param httpRequest HttpGet或者HttpPost
     * @return 网络异常会返回空
     */
    private String executeRequest(HttpUriRequest httpRequest) {
        CloseableHttpResponse response = null;
        InputStream in = null;
        String result = null;
        try {
            response = closeableHttpClient.execute(httpRequest, HttpClientContext.create());
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                in = entity.getContent();
                result = IOUtils.toString(in, DEFAULT_CHARSET);
            }
        } catch (IOException e) {
            logger.error("执行http请求,IO异常,url:{}", httpRequest.getURI(), e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                logger.error("关闭输入流异常,url:{}", httpRequest.getURI(), e);
            }
        }
        return result;
    }


    /**
     * 构造HTTP POST 对象
     *
     * @param url     请求地址
     * @param params  参数
     * @param headers header
     * @return post实例
     */
    private HttpPost buildHttpPostInstance(String url, Map<String, String> params, String body, Map<String, String> headers) {

        HttpPost httpPost = new HttpPost(url);
        /* 设置 http header */
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }

        /*
         * 设置http post请求参数
         */
        if (MapUtils.isNotEmpty(params)) {
            try {
                List<NameValuePair> nvps = new ArrayList<>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                logger.error("编码http参数异常", e);
                return null;
            }
        }

        /*
         * 如果body不为空,设置消息体
         */
        if (StringUtils.isNotBlank(body)) {
            httpPost.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        }

        // 设置socket连接参数
        this.setRequestConfig(httpPost);

        return httpPost;
    }


    /**
     * 构造HTTP GET 对象
     *
     * @param url     地址
     * @param params  参数
     * @param headers header
     * @return Http对象
     */
    private HttpGet buildHttpGetInstance(String url, Map<String, String> params, Map<String, String> headers) {
        HttpGet httpGet;
        try {
            httpGet = new HttpGet(createUri(url, params));
            /* 设置 http header */
            if (MapUtils.isNotEmpty(headers)) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpGet.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
                }
            }
            this.setRequestConfig(httpGet);
        } catch (URISyntaxException e) {
            logger.error("URL解析异常", e);
            return null;
        }
        // 设置socket连接参数
        this.setRequestConfig(httpGet);
        return httpGet;
    }


    /**
     * 构造HTTP PUT 对象
     *
     * @param url     地址
     * @param params  参数
     * @param body    消息体
     * @param headers header
     * @return Http对象
     */
    private HttpPut buildHttpPutInstance(String url, Map<String, String> params, String body, Map<String, String> headers) {
        HttpPut httpPut = new HttpPut(url);
        /* 设置 http header */
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPut.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }

        /*
         * 设置http post请求参数
         */
        if (MapUtils.isNotEmpty(params)) {
            try {
                List<NameValuePair> nvps = new ArrayList<>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
                httpPut.setEntity(new UrlEncodedFormEntity(nvps, DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                logger.error("编码http参数异常", e);
                return null;
            }
        }

        /*
         * 如果body不为空,设置消息体
         */
        if (StringUtils.isNotBlank(body)) {
            httpPut.setEntity(new StringEntity(body, DEFAULT_CHARSET));
        }

        // 设置socket连接参数
        this.setRequestConfig(httpPut);
        return httpPut;
    }


    /**
     * 对http 网络连接进行基本设置
     *
     * @param httpRequestBase http
     */
    private void setRequestConfig(HttpRequestBase httpRequestBase) {
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionTimeout)
                .setConnectTimeout(connectionTimeout).setSocketTimeout(socketReadTimeout).build();
        httpRequestBase.setConfig(requestConfig);
    }

    /**
     * 构造带参数的URL
     *
     * @param url    url
     * @param params 参数
     * @return URI
     * @throws URISyntaxException uri格式异常
     */
    private URI createUri(String url, Map<String, String> params) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url);
        /* 设置http put请求参数 */
        if (MapUtils.isNotEmpty(params)) {
            List<NameValuePair> nvps = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            uriBuilder.setParameters(nvps);
        }
        return uriBuilder.build();
    }

    @Override
    public void close() {
        try {
            // 关闭连接池
            httpConnectionPool.closeConnectionPool();
            closeableHttpClient.close();
        } catch (Exception ex) {
            logger.error("close http client exception", ex);
        }
    }
}
