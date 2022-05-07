package com;

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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpPostUtil {


    public void init() {

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(2, TimeUnit.MINUTES);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        poolingConnectionManager.setMaxTotal(2 * availableProcessors + 3); // 连接池最大连接数
        poolingConnectionManager.setDefaultMaxPerRoute(2 * availableProcessors); // 每个主机的并发
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(poolingConnectionManager).disableAutomaticRetries()
                .setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(10000)
                        .setConnectTimeout(10000).setSocketTimeout(30000).build()).build();
        test(httpClient);
    }

    public static void test(CloseableHttpClient httpClient) {
        HttpResponse response = null;
        try { // httpClient 和 HttpResponse  都要关闭
            // Post请求
            HttpPost httppost = new HttpPost("http://www.baidu.com");

            //设置post编码
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

    public static void main(String[] args) {
        new HttpPostUtil().init();
    }

}