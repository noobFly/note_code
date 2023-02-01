package com.noob;

import com.noob.json.JSON;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class JDSckill {

    static String UserAgent = "jdapp;android;11.4.2;;;appBuild/98639;ef/1;ep/%7B%22hdid%22%3A%22JM9F1ywUPwflvMIpYPok0tt5k9kW4ArJEU3lfLhxBqw%3D%22%2C%22ts%22%3A1673438401265%2C%22ridx%22%3A-1%2C%22cipher%22%3A%7B%22sv%22%3A%22CJK%3D%22%2C%22ad%22%3A%22DzO4D2U3D2Y3CWSyCNOmEG%3D%3D%22%2C%22od%22%3A%22CNKmCNKmCNKjCNKmCM0mCNKmBJKmCNKjCNKmCNKmCNKmCNKm%22%2C%22ov%22%3A%22Ctu%3D%22%2C%22ud%22%3A%22ENY5CtOzCNG5DzczDtu5BWZtEJGzDWO2DWYnDK%3D%3D%22%7D%2C%22ciphertype%22%3A5%2C%22version%22%3A%221.2.0%22%2C%22appname%22%3A%22com.jingdong.app.mall%22%7D;jdSupportDarkMode/0;Mozilla/5.0 (Linux; Android 10; ELE-AL00 Build/HUAWEIELE-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 MQQBrowser/6.2 TBS/046033 Mobile Safari/537.36";
    static String skuId = "100012043978"; // --------- 不会变
    static String cookiesString = "pt_pin=105711997-597973; pwdt_id=105711997-597973; __jdc=122270672; unpl=JF8EAKhnNSttCEwBABtRG0VCGFpTW1RbTB4AOmRRUA1eQgYAEgAZFRN7XlVdXhRLFB9sYBRUXVNKVw4bASsiEEpcVlZeDk0RA19XAlRUFQoGSCsBGyIRe11TWF0ASRQAbmQDVVxdTFMEHQESGhh7XGRZXQF7JzNqZwRVVFxIXQUaMhoiEkpcUVdVCUwUAF8sa1UQWExSBRMAGBERSFtVX1gPTBYFbG4NXG1Ze1c%7CADC_VH2tFFwz5%2F8jFaWiNxOZnokOUJAFdwhcjjv%2BOnz7%2Fmjq58rOgyu9OwRth9AU%2Bp6T404Dl2207diMORZQH14luSU50M3IP3wiG1ufP3FZeIqaoGL3MKKFXH52e0hlAdCUIu2skml7K9O1gx6BCcWrg61nREL6KSbU5cl0ksBf0vl37P3jpD6VqcaK%2BPgP6wF79h4frPbrVgLrAgUhITiiw0z4%2BlqDdaegrb5AO7PsE2GU%2FFnEA%2Ft%2BfESZ2Fl9%2F9dcx3WZwgFUejB4K4t%2BP0genmO8rPi1HvTfURBA80ViDMwzmEV62kl1Xfu0g94Up0C%2BpCcINTHwbWN9EuZ%2ByDLSJ%2BWanNnIaRDT607al5ycP%2BbR%2F9dwm5fNZGJ7lfsMG3AYuT9cBLqZSfg%2Fqb%2F7odZqSwBDMLTD8Ld%2FUpIc0JL00HZDYsdcnICGd408%2BDK%2BpeAwOxhrcSi3CTPXflr6JMOfp3N02LsNaMuVWYCcfIVouk730%2BuCcV9iPF9GZRgizp0zyIZdPoYJnvh3cfeiFfF08uLJk5kW78Lnia8%2Fh%2BhzvXsIYbDxKvJZLwtjn95VkWgJ; qid_uid=c358014d-ce73-4ca6-91b9-512e81d56bd7; qid_fs=1672035064942; qid_ls=1672035064942; qid_ts=1672035064971; qid_vis=1; 3AB9D23F7A4B3C9B=PVY3R3PEQDGD3KZMKRZ72YUUONPD6QYXKDBCYVEGIHBLM3YJTMZL3N5GBOID7QN5T2QIRRNAKWS5F3ZV2DQAEG3TXU; p-request-id=105711997-5979732022123020oepDQDGHHF; BATQW722QTLYVCRD={\"tk\":\"jdd0174RGTWSTWBI5G7QLSHFC5AHI3VPMTRAOWHAYHATYCC7PUZIC34TNJUMICC2A2K5OWJNUXYHGLHSRHXBBEK3TOHEJY5OBJT2LGYSA7TY01234567\",\"t\":1672678269911}; _gia_s_e_joint={\"eid\":\"PVY3R3PEQDGD3KZMKRZ72YUUONPD6QYXKDBCYVEGIHBLM3YJTMZL3N5GBOID7QN5T2QIRRNAKWS5F3ZV2DQAEG3TXU\",\"ma\":\"\",\"im\":\"\",\"os\":\"android\",\"osv\":\"\",\"ip\":\"113.111.10.139\",\"apid\":\"jdapp\",\"ia\":\"\",\"uu\":\"\",\"cv\":\"11.4.0\",\"nt\":\"UNKNOW\",\"at\":\"3\"}; _gia_s_local_fingerprint=be1c964351f29073352d9f0172328cb4; cid=8; __jdv=122270672%7Cweixin%7Ct_1000072662_17005_001%7Cweixin%7C-%7C1673150276216; pt_key=app_openAAJjvY1WADCYvtfWof1hqsSlXvJuhqtIYwJ-vAL_MlHjYd0BfEmS19NJpKgzTgHjeAEe1ILjIdo; sid=f3de44831884ac673731f8538f7a05bw; mid=QeqGg_eWDsK6OFSIXAA6KtFsMiCf3gldEte3e3hpsaQ; __jda=122270672.16720350542562103767739.1672035054.1673409603.1673438402.14; pre_session=A3o53EezN9OncES2tVZ80pdHrDEL3SXhVCVCEp2Yb7c=|2608; pre_seq=5; __jdb=122270672.13.16720350542562103767739|14.1673438402; mba_sid=1692.8; __jd_ref_cls=MSecKillBalance_Order_Submit; seckillSku=100012043978; seckillSid=; mba_muid=16720350542562103767739.1692." + new Date().getTime();

    static int count = 1;
    static int max = 10;
    static long period = 200l;

    public static void main(String[] args) throws ParseException {


        String hour = "12:00:00";


        String dateStr = new SimpleDateFormat("YYYY-MM-DD").format(new Date()) + " " + hour;
        System.out.println("抢茅台开始时间: " + dateStr + "  间隔：" + period);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (count > max) {
                    System.out.println("==== 京东茅台 不抢了！========");
                    return;
                }

                System.out.println("====  京东茅台 第" + count + "抢！========" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:sss").format(new Date()));
                execute();
                count++;
            }
        }, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr), period);
    }

    private static void execute() {


        try {
            CloseableHttpClient HttpClient = HttpClientBuilder.create().setDefaultCookieStore(null).build();

            HttpPost httpPost = new HttpPost(
                    "https://marathon.jd.com/seckillnew/orderService/submitOrder.action?skuId=" + skuId);

            // 转json参数
            String paramJson = "num=1&addressId=4815206246&name=%E8%B5%AB%E6%95%8F&provinceId=19&provinceName=%E5%B9%BF%E4%B8%9C&cityId=1601&cityName=%E5%B9%BF%E5%B7%9E%E5%B8%82&countyId=3633&countyName=%E5%A4%A9%E6%B2%B3%E5%8C%BA&townId=63247&townName=%E7%9F%B3%E7%89%8C%E8%A1%97%E9%81%93&addressDetail=%E5%A4%A9%E9%98%B3%E8%B7%AF134%E5%8F%B7%E7%A9%97%E5%9B%AD%E5%B0%8F%E5%8C%BAH%E6%A0%8B3003&mobile=186%2A%2A%2A%2A9971&mobileKey=167aa02bb8cea9d946b324e325b81b38&invoiceTitle=4&invoiceContent=1&invoicePhone=186%2A%2A%2A%2A9971&invoicePhoneKey=167aa02bb8cea9d946b324e325b81b38&invoice=true&password=&codTimeType=3&paymentType=4&overseas=0&phone=&areaCode=86&token=37bfa25c29f9d3f815d398fb7dd84ebf&sk=1SAHk4f1olekwlbedwp&skuId=100012043978";
            StringEntity stringEntity = new StringEntity(paramJson, ContentType.create("text/plain", "UTF-8"));
            httpPost.setEntity(stringEntity);

            // form形式参数
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.addHeader("Cookie", cookiesString);
            httpPost.addHeader("User-Agent", UserAgent);
            httpPost.addHeader("Origin", "https://marathon.jd.com");
            httpPost.addHeader("Referer", "https://marathon.jd.com/seckillM/seckill.action?skuId=" + skuId + "&num=1&rid=1673409634"); //没有会返回302重定向

		/*Set<Entry<String, Object>> entrySet = new Data().toMap().entrySet();
		List<NameValuePair> params = entrySet.stream()
				.map(t -> new BasicNameValuePair(String.valueOf(t.getKey()), String.valueOf(t.getValue())))
				.collect(Collectors.toList());
		httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
*/

            CloseableHttpResponse response = HttpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            System.out.println(response.getStatusLine() + "  " +  EntityUtils.toString(responseEntity));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

@Getter
class Data {
    private int num = 1;
    private boolean yuShou = true;
    private boolean isModifyAddress = false;
    private String name = "邹金儒";
    private int provinceId = 19;
    private int cityId = 1601;
    private int countyId = 3633;
    private int townId = 63251;
    private String addressDetail = "燕塘路燕塘大院燕塘路燕塘大院燕塘大院26号楼";
    private String mobile = "189****2471";
    private String mobileKey = "52f89a07644b2db544f66e77f389b8f4";
    private String email = null;
    private String postCode = null;
    private int invoiceTitle = 4;
    private String invoiceCompanyName = null;
    private int invoiceContent = 1;
    private String invoiceTaxpayerNO = null;
    private String invoiceEmail = null;
    private String invoicePhone = "189****2471";
    private String invoicePhoneKey = "52f89a07644b2db544f66e77f389b8f4";
    private boolean invoice = true;
    private String password = null;
    private int codTimeType = 3;
    private int paymentType = 4;
    private String areaCode = null;
    private int overseas = 0;
    private String phone = null;
    private String eid = "MC53DFTRBVAPFTIAS7VSS6EWXVDZXIYURCXACPSPZ6UA743R3CGXOBE6H64IT3E27Z6CC5XID3BTA42OGZJHJM4JF4";
    private String fp = "cd4d29213bea77b562c4fb336d21114c";
    private String token = "37bfa25c29f9d3f815d398fb7dd84ebf";
    private String pru = null;
    private String provinceName = "广东";
    private String cityName = "广州市";
    private String countyName = "天河区";
    private String townName = "兴华街道";

    public HashMap toMap() {
        return (HashMap) JSON.parseObject(JSON.toJSONString(this), HashMap.class);
    }
}