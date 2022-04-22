package com.noob.controller.gateway.openApi;

// 请求的入参key
    interface RequestParamKey {
        /**
         * 由开发者中心分配
         */
        String APP_ID = "appId";
        /**
         * 请求访问时间戳 , 排除时序相差太多的数据请求
         * <p>
         * 用来预防网络重试、阻塞等导致延时过长此时的请求数据很有可能被发起方主动重新构建并发起，设容忍间隔时间，超出则返回数据超时！
         * 一般通常会用ReuqestId做幂等处理，暂没用
         */
        String TIMESTAMP = "timestamp";
        /**
         * 业务字段合成的jsonString
         */
        String DATA = "data";
        /**
         * 数据签名。
         * <p>
         * 将 appId + data + timestamp拼接成一个字符串(utf-8编码)；对该字符串进行MD5withRSA签名算法运算，加密因子采用应用的私钥，得到即为本次通讯的签名值；
         */
        String SIGN = "sign"; //
    }