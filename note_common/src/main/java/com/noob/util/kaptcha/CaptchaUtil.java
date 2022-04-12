package com.noob.util.kaptcha;

import cn.hutool.core.io.FastByteArrayOutputStream;
import com.google.code.kaptcha.Producer;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CaptchaUtil {

    private Map<String, String> cacheMap = Maps.newConcurrentMap(); // 缓存验证码用来比对。通常使用Cache组件带过期时长

    Producer captchaProducerMath = CaptchaConfig.getKaptchaBeanMath();
    Producer captchaProducer = CaptchaConfig.getKaptchaBean();

    public Map<String, Object> getCode(String captchaType) {
        // 保存验证码信息
        String uuid = UUID.randomUUID().toString();

        String capStr = null, code = null;
        BufferedImage image = null;
        // 生成验证码
        if ("math".equals(captchaType)) {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        } else if ("char".equals(captchaType)) {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }

        cacheMap.put(uuid, code);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("img", Base64.encodeBase64String(os.toByteArray()));
        result.put("capStr", capStr); // 展示字符
        result.put("code", code); // 校验码
        return result;
    }

    public static void main(String arg[]) {
        System.out.println(new CaptchaUtil().getCode("char"));
        System.out.println(new CaptchaUtil().getCode("math"));

    }

}
