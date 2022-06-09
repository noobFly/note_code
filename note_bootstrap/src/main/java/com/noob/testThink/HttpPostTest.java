package com.noob.testThink;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpPostTest {


    public void init() {

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(2, TimeUnit.MINUTES);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        poolingConnectionManager.setMaxTotal(2); // 连接池最大连接数
        poolingConnectionManager.setDefaultMaxPerRoute(1); // 每个访问服务端主机的并发
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(poolingConnectionManager).disableAutomaticRetries()
                .setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(10000)
                        .setConnectTimeout(10000).setSocketTimeout(30000).build()).build();
        test(httpClient);

    }

    public static void test(CloseableHttpClient httpClient) {
        for (int i = 0; i < 100; i++) {
            HttpResponse response = null;
            try {
                // Post请求
                HttpPost httppost = new HttpPost(i % 2 == 0 ? "http://www.baidu.com/find/test?password=34" : "http://www.360.com");

                //设置post
                httppost.getParams().setParameter("http.protocol.content-charset", HTTP.UTF_8);
                httppost.getParams().setParameter(HTTP.CONTENT_ENCODING, HTTP.UTF_8);
                httppost.getParams().setParameter(HTTP.CHARSET_PARAM, HTTP.UTF_8);
                httppost.getParams().setParameter(HTTP.DEFAULT_PROTOCOL_CHARSET, HTTP.UTF_8);
                //设置post编码
                // 设置参数
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("appid", "xxxxx"));
                httppost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                //设置报文头
                httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                // 发送请求
                response = httpClient.execute(httppost);
                // 获取返回数据
                HttpEntity entity = response.getEntity();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                entity.writeTo(outputStream);
                System.out.println(new String(outputStream.toByteArray(), "utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    try {
                        ((CloseableHttpResponse) response).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        URLConnection A = new URL("http://www.baidu.com/find/test?password=34").openConnection();        A.setDoOutput(true);

        A.connect();
        A.getOutputStream().write(12);
        new HttpPostTest().init();
    }

}