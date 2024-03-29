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

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(2, TimeUnit.MINUTES);// 指定连接存活时长，如果未指定则是-1，表示Long.MAX_VALUE 基本可以说是永久有效了
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
                /**
                 *  #writeTo在最后会做releaseConnection() 进入连接复用回收处理：  因为默认是keep-alive, 所以在MainClientExec#execute里拿到response后被reusable被设置为true了。 DefaultClientConnectionReuseStrategy 。
                 *  最终执行到 PoolingHttpClientConnectionManager#releaseConnection 将连接从leased置换入available集合里
                 */
                entity.writeTo(outputStream);
                System.out.println(new String(outputStream.toByteArray(), "utf-8"));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    try {
                        ((CloseableHttpResponse) response).close(); // 这里从池化复用逻辑上来说是不需要了。 它会直接关闭掉该链接，不会连接池回收！!
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
   new HttpPostTest().init();
        URLConnection A = new URL("http://www.baidu.com/s?ie=utf-8&mod=1&isbd=1&isid=E186BCF2AF816405&ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=23213&fenlei=256&rsv_pq=ea2f8180000217a0&rsv_t=21e3CiGcKCqY0iRqzz4lKRjbjuKTnHAT1rtBQyl4jo/v6u1F1e3SCudyiqC9&rqlang=en&rsv_enter=0&rsv_dl=tb&rsv_sug3=5&rsv_sug1=2&rsv_sug7=100&rsv_btype=i&prefixsug=2%26lt%3B21%26lt%3B&rsp=4&inputT=3820&rsv_sug4=3821&rsv_sid=36553_36465_36454_36513_36414_36420_36165_36569_36519_26350_36467_36314&_ss=1&clist=&hsug=&f4s=1&csor=5&_cr1=36363").openConnection();        A.setDoOutput(true);
        A.setReadTimeout(1000000);
        A.connect();

        A.getOutputStream().write(12);
        A.getInputStream().read();
        new Thread( ()->{


            try {   URLConnection B = new URL("http://www.baidu.com/s?ie=utf-8&mod=1&isbd=1&isid=E186BCF2AF816405&ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=23213&fenlei=256&rsv_pq=ea2f8180000217a0&rsv_t=21e3CiGcKCqY0iRqzz4lKRjbjuKTnHAT1rtBQyl4jo/v6u1F1e3SCudyiqC9&rqlang=en&rsv_enter=0&rsv_dl=tb&rsv_sug3=5&rsv_sug1=2&rsv_sug7=100&rsv_btype=i&prefixsug=2%26lt%3B21%26lt%3B&rsp=4&inputT=3820&rsv_sug4=3821&rsv_sid=36553_36465_36454_36513_36414_36420_36165_36569_36519_26350_36467_36314&_ss=1&clist=&hsug=&f4s=1&csor=5&_cr1=36363").openConnection();
                B.setDoOutput(true);
                B.connect();
                B.getOutputStream().write(12);
                B.getInputStream().read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } ).start();
        new Thread( ()->{


            try {  URLConnection C = new URL("http://www.baidu.com/s?ie=utf-8&mod=1&isbd=1&isid=E186BCF2AF816405&ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&tn=baidu&wd=23213&fenlei=256&rsv_pq=ea2f8180000217a0&rsv_t=21e3CiGcKCqY0iRqzz4lKRjbjuKTnHAT1rtBQyl4jo/v6u1F1e3SCudyiqC9&rqlang=en&rsv_enter=0&rsv_dl=tb&rsv_sug3=5&rsv_sug1=2&rsv_sug7=100&rsv_btype=i&prefixsug=2%26lt%3B21%26lt%3B&rsp=4&inputT=3820&rsv_sug4=3821&rsv_sid=36553_36465_36454_36513_36414_36420_36165_36569_36519_26350_36467_36314&_ss=1&clist=&hsug=&f4s=1&csor=5&_cr1=36363").openConnection();
                C.setDoOutput(true);
                C.connect();
                C.getOutputStream().write(12);
               C.getInputStream().read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } ).start();
    }

}