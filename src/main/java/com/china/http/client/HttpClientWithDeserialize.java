package com.china.http.client;

import com.china.http.client.model.PoolConfig;
import com.china.http.client.model.Response;
import com.china.http.client.util.JacksonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
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
public class HttpClientWithDeserialize<T> {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientWithDeserialize.class);

    private final HttpClient client;

    public HttpClientWithDeserialize() {
        client = new HttpClient();
    }

    public HttpClientWithDeserialize(PoolConfig config) {
        client = new HttpClient(config);
    }


    /**
     * http post请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头部参数
     * @return 如果异常会返回为空
     */
    public Response<T> postResponse(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            return JacksonUtils.json2TypeReference(client.postForString(url, params, headers), new TypeReference<Response<T>>() {
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
    public Response<T> getResponse(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            return JacksonUtils.json2TypeReference(client.getForString(url, params, headers), new TypeReference<Response<T>>() {
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
     * @param headers 请求参数
     * @return 如果异常会返回为空
     */
    public T post(String url, Map<String, String> params, Map<String, String> headers) {
        return this.post(url, params, null, headers);
    }

    /**
     * http post请求
     *
     * @param url     请求地址
     * @param body    请求消息体
     * @param headers 请求头部参数
     * @return 如果异常会返回为空
     */
    public T post(String url, String body, Map<String, String> headers) {
        return this.post(url, null, body, headers);
    }


    /**
     * http get请求
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param headers 请求头部参数
     * @return 如果异常会返回为空
     */
    public T get(String url, Map<String, String> params, Map<String, String> headers) {
        String result =  client.getForString(url,params,headers);
        TypeReference<T> typeReference = new TypeReference<T>(){};
        try {
            return JacksonUtils.json2TypeReference(result, typeReference);
        } catch (Exception e) {
            logger.error("请求结果返回值反序列化对象失败,url:{}", url, e);
        }
        return null;
    }

    /**
     * post http 请求
     * params 和 body 参数必须传一个，如果同时传，优先使用body参数作为http请求参数
     *
     * @param url     请求地址
     * @param params  请求key&value参数体
     * @param body    请求String消息体
     * @param headers 头部参数
     * @return 返回JSON反序列化成T的对象，如果异常，将会返回null
     */
    private T post(String url, Map<String, String> params, String body, Map<String, String> headers) {
        String result = client.postForString(url, params, body, headers);

        try {
            return JacksonUtils.json2TypeReference(result, new TypeReference<T>(){});
        } catch (Exception e) {
            logger.error("请求结果返回值反序列化对象失败,url:{}", url, e);
        }
        return null;
    }

}
