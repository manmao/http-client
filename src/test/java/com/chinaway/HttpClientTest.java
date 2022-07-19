package com.chinaway;

import com.china.http.client.HttpClient;
import org.junit.Test;

import java.util.HashMap;

/**
 * Unit test for simple App.
 */
public class HttpClientTest {

    @Test
    public void testHttpR() {
        HttpClient httpClient = new HttpClient();
        String res = httpClient.getForString("https://www.baidu.com",new HashMap<String, String>(),new HashMap<String, String>());
        System.out.println(res);
    }

    public static void main(String[] args) {
        HttpClient httpClient = new HttpClient();
        String res = httpClient.getForString("https://www.baidu.com",new HashMap<String, String>(),new HashMap<String, String>());
        System.out.println(res);
        httpClient.close();
    }

}
