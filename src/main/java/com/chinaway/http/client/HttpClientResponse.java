package com.chinaway.http.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.chinaway.http.client.model.PoolConfig;
import com.chinaway.http.client.model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 将http请求结果转化成Response<T>的格式
 *
 * @param <T>
 * @author manmao
 * @since 2019-03-12
 */
public class HttpClientResponse<T> {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientResponse.class);

    private final HttpClient<JSONObject> client;

    public HttpClientResponse() {
        client = new HttpClient<>();
    }

    public HttpClientResponse(PoolConfig config) {
        client = new HttpClient<>(config);
    }

    /**
     * http post请求
     *
     * @param url     请求地址
     * @param body    请求消息体
     * @param headers 请求头部参数
     * @return 如果异常会返回为空
     */
    public Response<T> post(String url, String body, Map<String, String> headers) {
        JSONObject response = client.post(url, body, headers);
        try {
            return JSON.parseObject(response.toJSONString(), new TypeReference<Response<T>>() {
            });
        } catch (Exception e) {
            logger.error("http返回结果反序列化对象失败,url:{}", url, e);
            return null;
        }
    }

    /**
     * http post请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头部参数
     * @return 如果异常会返回为空
     */
    public Response<T> post(String url, Map<String, String> params, Map<String, String> headers) {
        JSONObject response = client.post(url, params, headers);
        try {
            return JSON.parseObject(response.toJSONString(), new TypeReference<Response<T>>() {
            });
        } catch (Exception e) {
            logger.error("http返回结果反序列化对象失败,url:{}", url, e);
            return null;
        }
    }


    /**
     * http get请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头部参数
     * @return 如果异常会返回为空
     */
    public Response<T> get(String url, Map<String, String> params, Map<String, String> headers) {
        JSONObject response = client.get(url, params, headers);
        try {
            return JSON.parseObject(response.toJSONString(), new TypeReference<Response<T>>() {
            });
        } catch (Exception e) {
            logger.error("http返回结果反序列化对象失败,url:{}", url, e);
            return null;
        }
    }

}
