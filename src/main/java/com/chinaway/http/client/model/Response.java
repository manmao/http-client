package com.chinaway.http.client.model;

/**
 * Response
 *
 * @param <T>
 * @author manmao
 * @since 2019-03-13
 */
public class Response<T> {

    private int code;
    private String message;
    private String msg;
    private T data;

    public Response() {
    }

    public Response(T d) {
        data = d;
        code = 0;
    }

    public Response(int c, String msg) {
        message = msg;
        code = c;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}