package com.noob.ipLocation;

import com.noob.util.HttpUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网易126
 *
 */
public class Wy126IpLocationQuerier extends AbstractIpLocationLimitRateQuerier {
    private final static int PERMITS_PER_SECOND = 10; //未测试最大值，如果有必要可以调大
    // IP地址查询
    private static final String QUERY_URL = "http://ip.ws.126.net/ipquery";
    private static Pattern pattern = Pattern.compile("\"([\u4e00-\u9fa5]+)\"");

    public Wy126IpLocationQuerier() {
        super(PERMITS_PER_SECOND);
    }

    @Override
    protected String[] query(String ipAddr) throws Exception {
        String rspStr = HttpUtils.sendGet(QUERY_URL, "ip=" + ipAddr, "GBK");
        if (StringUtils.isEmpty(rspStr)) {
            return null;
        }
        //返回的数据格式：var lo="广东省", lc="广州市"; var localAddress={city:"广州市", province:"广东省"}
        String[] arry = rspStr.split(";")[0].split(",");
        String province = getQuoteBody(arry[0]);
        String city = getQuoteBody(arry[1]);
        return new String[]{province, city};
    }

    private String getQuoteBody(String str) {
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
