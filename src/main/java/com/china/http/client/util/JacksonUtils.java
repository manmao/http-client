package com.china.http.client.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Jackson工具类
 *
 * @author mao.man@rootcloud.com<br>
 * @version 1.0<br>
 * @date 2021-06-01 <br>
 */
public class JacksonUtils {

    private static final Logger log = LoggerFactory.getLogger(JacksonUtils.class);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /* 设置默认属性 */
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static String bean2Json(Object data) {
        try {
            return OBJECT_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("bean to json exception", e);
        }
        return null;
    }

    public static <T> T jsonToBean(String jsonData, Class<T> beanType) {
        try {
            return OBJECT_MAPPER.readValue(jsonData, beanType);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T json2Bean(String jsonData, Class<T> beanType) {
        try {
            return OBJECT_MAPPER.readValue(jsonData, beanType);
        } catch (Exception e) {
            log.error("json to bean exception", e);
        }

        return null;
    }


    public static <T> T json2Bean(String jsonData, JavaType javaType) {
        try {
            return OBJECT_MAPPER.readValue(jsonData, javaType);
        } catch (Exception e) {
            log.error("json to bean exception", e);
        }

        return null;
    }

    public static <T> List<T> json2List(String jsonData, Class<T> beanType) {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(List.class, beanType);

        try {
            if (StringUtils.isNotBlank(jsonData)){
                return OBJECT_MAPPER.readValue(jsonData, javaType);
            }
        } catch (Exception e) {
            log.error("json to list exception", e);
        }

        return null;
    }

    public static <K, V> Map<K, V> json2Map(String jsonData, Class<K> keyType, Class<V> valueType) {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType);

        try {
            return OBJECT_MAPPER.readValue(jsonData, javaType);
        } catch (Exception e) {
            log.error("json to map exception", e);
        }

        return null;
    }

    public static <T> T json2TypeReference(String jsonData, TypeReference<T> tTypeReference) {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructType(tTypeReference);
        try {
            return OBJECT_MAPPER.readValue(jsonData, javaType);
        } catch (Exception e) {
            log.error("json to typeReference exception", e);
        }
        return null;
    }

    public static JsonNode jsonNode(String jsonData) {
        try {
            return OBJECT_MAPPER.readTree(jsonData);
        } catch (JsonProcessingException e) {
            log.error("json string to json node exception", e);
        }
        return null;
    }
}