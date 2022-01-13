package com.chinaway.http.client;

import com.chinaway.http.client.model.PoolConfig;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultClientConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * HTTP连接池
 *
 * @author manmao
 * @since 2019-03-11
 */
public class HttpConnectionPool {

    private static Logger logger = LoggerFactory.getLogger(HttpConnectionPool.class);

    /**
     * 连接池 最大连接数
     */
    private static final int DEFAULT_MAX_CONN = 20;

    /**
     * 每个连接地址默认最大连接数
     */
    private static final int DEFAULT_MAX_PRE_ROUTE = 4;

    /**
     * 默认连接空闲超时时间 单位: ms
     */
    private static final long DEFAULT_CONNECTION_IDLE_TIMEOUT = 8000;

    /**
     * 默认连接过期时间，-1 为永不过期
     */
    private static final long DEFAULT_CONNECTION_EXPIRED_TIME = -1;

    /**
     * 相当于线程锁,用于线程安全
     */
    private final static Object HTTP_CLIENT_LOCK = new Object();

    /**
     * 发送请求的客户端单例
     */
    private volatile static CloseableHttpClient httpClient;

    /**
     * 连接池管理类
     */
    private static PoolingHttpClientConnectionManager connectionManager;

    /**
     * 定时任务,定时关闭空闲连接
     */
    private static ScheduledExecutorService monitorExecutor;


    /**
     * 获取httpClient单例，并且第一次获取，启动连接池管理任务，默认配置
     *
     * @return httpclient
     */
    public static CloseableHttpClient getHttpClientInstance() {
        return getHttpClientInstance(new PoolConfig(DEFAULT_MAX_CONN, DEFAULT_MAX_PRE_ROUTE, DEFAULT_CONNECTION_IDLE_TIMEOUT, DEFAULT_CONNECTION_EXPIRED_TIME));
    }


    /**
     * 获取httpClient单例，并且第一次获取，启动连接池管理任务
     * <p>
     *    默认重试handler
     * </p>
     * @param config http连接池客户端
     * @return httpclient
     */
    public static CloseableHttpClient getHttpClientInstance(PoolConfig config) {
        // 请求失败时,进行请求重试
        HttpRequestRetryHandler handler = new SimpleHttpRequestRetryHandler();
        return getHttpClientInstance(config, handler);
    }


    /**
     * 获取httpClient单例，并且第一次获取，启动连接池管理任务
     *
     * @param config  http连接池客户端
     * @param handler 重试handler
     * @return httpclient
     */
    public static CloseableHttpClient getHttpClientInstance(PoolConfig config, HttpRequestRetryHandler handler) {
        if (httpClient == null) {
            synchronized (HTTP_CLIENT_LOCK) {
                if (httpClient == null) {
                    httpClient = createHttpClient(config, handler);
                    startManagerMonitor(config);
                }
            }
        }
        return httpClient;
    }


    /**
     * 定时管理空闲连接和过期连接
     * 每隔 10s 关闭一次空闲连接
     */
    private static void startManagerMonitor(final PoolConfig config) {
        if (monitorExecutor == null) {
            monitorExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory());
            monitorExecutor.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    //关闭过期连接
                    connectionManager.closeExpiredConnections();
                    //关闭5s空闲的连接
                    connectionManager.closeIdleConnections(config.getIdleTimeout(), TimeUnit.MILLISECONDS);
                    logger.info("close expired and idle for over 10 s connection,current pool stats:{}", connectionManager.getTotalStats().toString());
                }
            }, 1000, 10000, TimeUnit.MILLISECONDS);
        }
    }


    /**
     * 构造连接池管理器，生成http client
     *
     * @return httpclient
     */
    private static CloseableHttpClient createHttpClient(PoolConfig config, HttpRequestRetryHandler handler) {

        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        LayeredConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainSocketFactory)
                .register("https", sslSocketFactory).build();

        connectionManager = new PoolingHttpClientConnectionManager(registry, null, null, null, config.getExpiredTime(), TimeUnit.SECONDS);
        // 设置连接参数 最大连接数
        connectionManager.setMaxTotal(config.getMaxConnections());
        // 设置路由默认最大连接数
        connectionManager.setDefaultMaxPerRoute(config.getMaxPerRouteConnections());

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                // 设置保活策略
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                // 设置连接重用策略
                .setConnectionReuseStrategy(new DefaultClientConnectionReuseStrategy())
                .setRetryHandler(handler).build();
    }

    /**
     * 关闭连接池
     */
    public static void closeConnectionPool() {
        try {
            httpClient.close();
            connectionManager.close();
            monitorExecutor.shutdown();
        } catch (IOException e) {
            logger.error("关闭连接池异常", e);
        }
    }
}