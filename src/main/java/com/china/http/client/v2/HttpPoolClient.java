package com.china.http.client.v2;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * http客户端请求工具类 <br>
 *
 * @author gan.wang@rootcloud.com<br>
 * @version 1.0<br>
 * @date 2021/08/17 <br>
 */
public class HttpPoolClient implements AutoCloseable {

    /**
     * 默认最大线程数,默认20
     */
    public static final Integer DEFAULT_MAX_THREAD = 20;
    /**
     * 最小线程数为5
     */
    public static final Integer DEFAULT_MIN_THREAD = 5;
    /**
     * 默认重试次数
     */
    public static final Integer DEFAULT_RETRY_COUNT = 3;
    /**
     * utf-8字符编码
     */
    public static final String CHARSET_UTF_8 = "utf-8";
    /**
     * 从连接池获取连接的timeout
     */
    public static final Integer CONNECTION_REQUEST_TIMEOUT = 30000;
    /**
     * url与参数的分隔符
     */
    public static final String URL_PARAM_SEPARATOR = "?";
    public static final char QP_SEP_A = '&';
    public static final String NAME_VALUE_SEPARATOR = "=";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPoolClient.class);
    /**
     * 连接管理器
     */
    private PoolingHttpClientConnectionManager pool;

    /**
     * httpClient缓存
     */
    private CloseableHttpClient httpClient;

    /**
     * 根据请求方法创建对应的请求
     *
     * @param method    请求方法,支持{@link HttpMethod}
     * @param reqUri    请求地址
     * @param body      请求体
     * @param headerMap 请求头
     * @return 返回通用的请求
     */
    protected static HttpUriRequest createHttpUriRequest(String method, String reqUri, String body, Map<String, String> headerMap) {
        HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
        URI uri = URI.create(reqUri);
        switch (httpMethod) {
            case GET:
                HttpGet httpGet = new HttpGet(uri);
                return configHeader(httpGet, headerMap);
            case HEAD:
                HttpHead httpHead = new HttpHead(uri);
                return configHeader(httpHead, headerMap);
            case POST:
                HttpPost httpPost = new HttpPost(uri);
                return configEntity(httpPost, body, headerMap);
            case PUT:
                HttpPut httpPut = new HttpPut(uri);
                return configEntity(httpPut, body, headerMap);
            case PATCH:
                HttpPatch patch = new HttpPatch(uri);
                return configEntity(patch, body, headerMap);
            case DELETE:
                HttpDelete httpDelete = new HttpDelete(uri);
                return configEntity(httpDelete, body, headerMap);
            case OPTIONS:
                HttpOptions options = new HttpOptions(uri);
                return configHeader(options, headerMap);
            case TRACE:
                HttpTrace trace = new HttpTrace(uri);
                return configHeader(trace, headerMap);
            default:
                throw new RuntimeException("不支持的方法");
        }

    }

    public static HttpRequestBase configHeader(HttpRequestBase base, Map<String, String> headerMap) {
        //设置header
        if (null != headerMap && !headerMap.isEmpty()) {
            for (Map.Entry<String, String> headerEntry : headerMap.entrySet()) {
                base.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        LOGGER.info("req Header:{}", headerMap);
        return base;
    }

    private static HttpEntityEnclosingRequestBase configEntity(HttpEntityEnclosingRequestBase base, String body, Map<String, String> headerMap) {
        configHeader(base, headerMap);
        /*
         * 只有不为空,才进行放置
         */
        if (null != body && !"".equalsIgnoreCase(body.trim())) {
            LOGGER.info("req body:{}", body);
            // 创建请求内容
            StringEntity entity = new StringEntity(body, ContentType.APPLICATION_JSON);
            base.setEntity(entity);
        }
        return base;
    }

    /**
     * 初始化处理
     *
     * @param retryCount     最大重试次数
     * @param maxThreadNum   连接池最大线程数
     * @param socketTimeout  客户端从服务器读取数据的timeout
     * @param connectTimeout 从连接池获取连接的timeout
     */
    public void init(Integer retryCount, Integer maxThreadNum, int socketTimeout, int connectTimeout) {
        retryCount =
                Optional.ofNullable(retryCount).filter(v -> v.compareTo(DEFAULT_RETRY_COUNT) >= 0).orElse(DEFAULT_RETRY_COUNT);
        /**
         * 如果最大线程数为null,或者小于最小线程,那么使用最小线程,
         * 如果大于最大线程,那么使用默认最大线程,防止过载
         */
        if (null == maxThreadNum || maxThreadNum.compareTo(DEFAULT_MIN_THREAD) < 0) {
            maxThreadNum = DEFAULT_MIN_THREAD;
        } else if (maxThreadNum.compareTo(DEFAULT_MAX_THREAD) > 0) {
            maxThreadNum = DEFAULT_MAX_THREAD;
        }
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            // 配置同时支持 HTTP 和 HTPPS
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf).build();

            // 初始化连接管理器
            pool = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // 设置连接池的最大连接数
            pool.setMaxTotal(maxThreadNum);
            // 设置每个路由上的默认连接个数
            pool.setDefaultMaxPerRoute(maxThreadNum);
            /**
             * 当连接空闲的时候,30s后进行驱逐,保证其他可用
             */
            pool.closeIdleConnections(30, TimeUnit.SECONDS);
            // 从连接池获取连接的timeout
            int connectionRequestTimeout = CONNECTION_REQUEST_TIMEOUT;
            //设置请求超时时间,无限等待超时
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(connectionRequestTimeout)
                    .setSocketTimeout(socketTimeout)
                    .setConnectTimeout(connectTimeout).build();
            // 状态码是503的时候，该策略生效
            HttpRequestRetryHandler httpRequestRetryHandler = new SelfHttpRequestRetryHandler(retryCount, false);
            httpClient = HttpClients.custom()
                    // 设置连接池管理
                    .setConnectionManager(pool)
                    // 设置请求配置
                    .setDefaultRequestConfig(requestConfig)
                    // 设置重试次数
                    .setRetryHandler(httpRequestRetryHandler)
                    .setKeepAliveStrategy((res, context) -> 30 * 1000)
                    .setConnectionReuseStrategy((res, context) -> true)
                    .build();
            LOGGER.info("init HttpClient......finish");
        } catch (Exception e) {
            LOGGER.error("init HttpClient......fail", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * 使用线程池发起真正的http请求
     *
     * @param reqUri    请求地址
     * @param method    请求方法,支持{@link HttpMethod}
     * @param body      请求体,有就进行处理,没有传null
     * @param reqParams 请求参数
     * @param headerMap 请求头
     * @return 返回通用的请求
     * @throws IOException io异常
     * @see #createHttpUriRequest(String, String, String, Map)
     */
    public String doRequest(String reqUri, String method, String body, Map<String, String> reqParams, Map<String, String> headerMap) throws IOException {
        if (!reqParams.isEmpty()) {
            final StringBuilder nameValueBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : reqParams.entrySet()) {
                if (nameValueBuilder.length() > 0) {
                    nameValueBuilder.append(QP_SEP_A);
                }
                nameValueBuilder.append(entry.getKey());
                nameValueBuilder.append(NAME_VALUE_SEPARATOR);

                /*
                 * 需要对value为string类型进行编码
                 */
                Object value = entry.getValue();
                if (value != null) {
                    value = URLEncoder.encode((String) value, StandardCharsets.UTF_8.name());
                }
                nameValueBuilder.append(value);
            }
            StringBuilder uriBuilder = new StringBuilder(reqUri);
            reqUri = uriBuilder.append(URL_PARAM_SEPARATOR).append(nameValueBuilder).toString();
        }
        LOGGER.info("req url:{},method:{}", reqUri, method);

        HttpUriRequest request = createHttpUriRequest(method, reqUri, body, headerMap);
        CloseableHttpResponse response = null;
        try {
            response = getHttpClient().execute(request);
            return EntityUtils.toString(response.getEntity(), CHARSET_UTF_8);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                LOGGER.error("close response exception", e);
            }
        }
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        if (null != pool) {
            pool.shutdown();
        }
        CloseableHttpClient client = getHttpClient();
        if (null != client) {
            try {
                client.close();
            } catch (IOException e) {
                LOGGER.error("shutdown httpclient exception", e);
            }
        }
        LOGGER.info("shutdown httpclient connection pool.");
    }

    @Override
    public void close() throws Exception {
        shutdown();
    }

    private static class HttpDelete extends HttpEntityEnclosingRequestBase {
        public HttpDelete(URI uri) {
            this.setURI(uri);
        }

        @Override
        public String getMethod() {
            return "DELETE";
        }
    }
}
