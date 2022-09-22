package com.noob.encryptedEnv;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EncryptedPropertySourceWrapper<T extends PropertySource> extends MapPropertySource {
    private AesEncoder encoder = new AesEncoder();
    public final static String PREFIX = "$$AES$$";
    private MapPropertySource target;

    public EncryptedPropertySourceWrapper(MapPropertySource delegate) {
        super(delegate.getName(), delegate.getSource());
        target = delegate;
    }


    @Override
    public Object getProperty(String name) {
        Object value = target.getProperty(name);
        if (value != null && String.valueOf(value).startsWith(PREFIX)) {
            String result = encoder.decode(String.valueOf(value));
            log.debug("解密后：{}", result);
            return result;
        }
        return value;
    }


    public static class AesEncoder {

        private static final String DEFAULT_CREDENTIAL = "u&t%r#u!s@t&^()*";

        private final AES aes;

        public AesEncoder() {
            aes = SecureUtil.aes(buildKey(DEFAULT_CREDENTIAL));
        }

        public String encode(String content) {
            return PREFIX + aes.encryptHex(content);
        }

        public String decode(String encodedContent) {
            return aes.decryptStr(encodedContent.startsWith(PREFIX) ? encodedContent.substring(PREFIX.length()) : encodedContent);
        }

        // 16个字节
        private static byte[] buildKey(String credential) {
            byte[] bytes = StringUtils.defaultIfBlank(credential, DEFAULT_CREDENTIAL).getBytes(StandardCharsets.UTF_8);
            int length = bytes.length;
            if (length == 16) {
                return bytes;
            }
            if (length > 16) {
                throw new RuntimeException("AES密钥长度过长");
            }

            byte[] keyBytes = new byte[16];

            for (int i = 0; i < 16; i++) {
                if (i < length) {
                    keyBytes[i] = bytes[i];
                } else {
                    keyBytes[i] = 0;
                }
            }

            return keyBytes;
        }
    }

    public static void main(String[] args) {
        System.out.println(new AesEncoder().encode("YcjkRedis@2022"));
    }
}
