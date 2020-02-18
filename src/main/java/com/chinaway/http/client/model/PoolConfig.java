package com.chinaway.http.client.model;


/**
 * http 连接池配置
 *
 * @author manmao
 * @since 2019-03-15
 */
public class PoolConfig {

    /**
     * 连接池最大连接数
     */
    private int maxConnections;

    /**
     * 每个网关的最大连接数
     */
    private int maxPerRouteConnections;

    /**
     * 连接空闲超时时间
     */
    private long idleTimeout;

    /**
     * 连接过期时间
     */
    private long expiredTime;

    public PoolConfig() {
    }

    public PoolConfig(int maxConnections, int maxPerRouteConnections, long idleTimeout, long expiredTime) {
        this.maxConnections = maxConnections;
        this.maxPerRouteConnections = maxPerRouteConnections;
        this.idleTimeout = idleTimeout;
        this.expiredTime = expiredTime;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMaxPerRouteConnections() {
        return maxPerRouteConnections;
    }

    public void setMaxPerRouteConnections(int maxPerRouteConnections) {
        this.maxPerRouteConnections = maxPerRouteConnections;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }
}
