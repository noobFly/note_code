package com.noob.ipLimit;

import com.noob.json.JSON;
import com.noob.json.JSONObject;
import com.noob.util.HttpUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 太平洋电脑网IP所在地查询
 *
 */
public class PconlineIpLocationQuerier extends AbstractIpLocationLimitRateQuerier {
    private final static int PERMITS_PER_SECOND = 10; //未测试最大值，如果有必要可以调大
    // IP地址查询
    private static final String QUERY_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    public PconlineIpLocationQuerier() {
        super(PERMITS_PER_SECOND);
    }

    @Override
    protected String[] query(String ipAddr) throws Exception {
        String rspStr = HttpUtils.sendGet(QUERY_URL, "ip=" + ipAddr + "&json=true", "GBK");
        if (StringUtils.isEmpty(rspStr)) {
            return null;
        }
        JSONObject obj = JSON.parseObject(rspStr);
        String province = obj.getString("pro");
        String city = obj.getString("city");
        return new String[]{province, city};
    }

}
