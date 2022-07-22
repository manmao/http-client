package com.chinaway;

import com.china.http.client.HttpClient;
import com.china.http.client.HttpClientWithDeserialize;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class HttpClientTest {

    @Test
    public void testHttpR() throws Exception {
        HttpClientWithDeserialize<Map> httpClient = new HttpClientWithDeserialize<>();
        Map res = httpClient.post("https://api.github.com/repos/emqx/emqx",new HashMap<String, String>(),new HashMap<String, String>());
        System.out.println(res);
        httpClient.close();
    }

    @Test
    public void testHttp() throws Exception {
        HttpClient  httpClient = new HttpClient();
        String res = httpClient.getForString("https://api.github.com/repos/emqx/emqx",new HashMap<String, String>(),new HashMap<String, String>());
        System.out.println(res);
        httpClient.close();
    }



}
