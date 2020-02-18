package com.chinaway;

import com.alibaba.fastjson.JSON;
import com.chinaway.http.client.HttpClient;
import com.chinaway.http.client.HttpClientResponse;
import com.chinaway.http.client.model.Response;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class HttpClientTest {

    final String body = "{\"code\": \"\",\"message\":\"\",\"pushTime\":1550802627796,\"data\":[{\n" +
            "  \"type\": \"engineOverSpeed\",\n" +
            "  \"plateNum\": \"闽 C86807\",\n" +
            "  \"vin\": \"LVCB4L4D4GM003741\",\n" +
            "  \"seconds\": 0,\n" +
            "  \"imei\": \"107015090009697\",\n" +
            "  \"startTime\": 1513937593000,\n" +
            "  \"startLng\": 118.61661988484408,\n" +
            "  \"startLat\": 24.715075829169116,\n" +
            "  \"endTime\": 1513937593000,\n" +
            "  \"endLng\": 0,\n" +
            "  \"endLat\": 0,\n" +
            "  \"additionalInfo\": {\n" +
            "    \"title\": \"转速过高\"\n" +
            "  },\n" +
            "  \"canDetail\": [\n" +
            "    {\n" +
            "      \"speed\": 0.0,\n" +
            "      \"time\": 1508999113000,\n" +
            "      \"totalFuel\": 1234.1,\n" +
            "      \"totalMileage\": 103452.123,\n" +
            "      \"engineTime\": 1234,\n" +
            "      \"engineSpeed\": 1234,\n" +
            "      \"engineStatus\": 1,\n" +
            "      \"brake\": 0,\n" +
            "      \"totalBrakeTime\": 123,\n" +
            "      \"totalBrakeCount\": 234,\n" +
            "      \"totalBrakeMileage\": 1234.5,\n" +
            "      \"accelerator\": 1,\n" +
            "      \"coolantTemperature\": 50,\n" +
            "      \"tempFuel\": 30.0,\n" +
            "      \"gear\": 5,\n" +
            "      \"torque\": 10,\n" +
            "      \"brakePedalPosition\": 0,\n" +
            "      \"batteryVoltage\": 50,\n" +
            "      \"oilPressure\": 10,\n" +
            "      \"intakePressure\": 60,\n" +
            "      \"atmoPressure\": 50,\n" +
            "      \"temperature\": 27.1\n" +
            "    },\n" +
            "    {\n" +
            "      \"speed\": 0.0,\n" +
            "      \"time\": 1508999114000,\n" +
            "      \"totalFuel\": 1234.1,\n" +
            "      \"totalMileage\": 103452.123,\n" +
            "      \"engineTime\": 1234,\n" +
            "      \"engineSpeed\": 1234,\n" +
            "      \"engineStatus\": 1,\n" +
            "      \"brake\": 0,\n" +
            "      \"totalBrakeTime\": 123,\n" +
            "      \"totalBrakeCount\": 234,\n" +
            "      \"totalBrakeMileage\": 1234.5,\n" +
            "      \"accelerator\": 1,\n" +
            "      \"coolantTemperature\": 50,\n" +
            "      \"tempFuel\": 30.0,\n" +
            "      \"gear\": 5,\n" +
            "      \"torque\": 10,\n" +
            "      \"brakePedalPosition\": 0,\n" +
            "      \"batteryVoltage\": 50,\n" +
            "      \"oilPressure\": 10,\n" +
            "      \"intakePressure\": 60,\n" +
            "      \"atmoPressure\": 50,\n" +
            "      \"temperature\": 27.1\n" +
            "    }\n" +
            "  ]\n" +
            "}]}";

    @Test
    public void TestextractionData() throws InterruptedException {

        final Map params = new HashMap<String, String>();
        params.put("gpsnos", "11007319");
        params.put("from", "2019-03-06 10:00:00");
        params.put("to", "2019-03-06 11:00:00");

        final Map header = new HashMap<String, String>();
        header.put("X-G7-OpenAPI-OrgCode", "#90007");
        header.put("Content-Type", "application/json");

        final HttpClient httpClient = new HttpClient<String>();
        for (int i = 0; i < 40; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        long start = System.currentTimeMillis();
                        httpClient.get("http://172.22.34.230:8013/vega/ems/ems_travel", params, new HashMap<String, String>());
                        httpClient.get("http://172.16.18.237:8013/vega/ems/ems_travel", params, new HashMap<String, String>());
                        //System.out.println(httpClient.post("http://172.22.35.208:8084/openapi/ems/ems_stat", body, header));
                        long end = System.currentTimeMillis();
                        //System.out.println("耗时" + (end - start));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }

        while (true) ;

    }

    @Test
    public void testHttpClientGetResponse() {
        final Map params = new HashMap<String, String>();
        params.put("gpsnos", "11007319");
        params.put("from", "2019-03-06 10:00:00");
        params.put("to", "2019-03-06 11:00:00");

        final HttpClientResponse httpClient = new HttpClientResponse<String>();
        for (int i = 0; i < 20; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        Response<List<String>> response = httpClient.get("http://172.22.34.230:8013/vega/ems/ems_travel", params, new HashMap<String, String>());
                        System.out.println(JSON.toJSONString(response.getData()));
                        System.out.println(JSON.toJSONString(response));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        while (true) ;
    }

    @Test
    public void testHttpClientPostResponse() {

        final Map params = new HashMap<String, String>();
        params.put("gpsnos", "11007319");
        params.put("from", "2019-03-06 10:00:00");
        params.put("to", "2019-03-06 11:00:00");

        final Map header = new HashMap<String, String>();
        header.put("X-G7-OpenAPI-OrgCode", "#90007");
        header.put("Content-Type", "application/json");

        final HttpClientResponse httpClient = new HttpClientResponse<String>();

        for (int i = 0; i < 20; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        Response<String> response = httpClient.post("http://172.22.35.208:8084/openapi/ems/ems_stat", body, header);
                        System.out.println(JSON.toJSONString(response.getData()));
                        System.out.println(JSON.toJSONString(response));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }

        while (true) ;
    }
}
