package com.noob.util.security;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 *  加签/验签 和 加密/解密 是2个不同逻辑细节的事！！
 *
 * 私钥加签 -> SignatureSpi#engineInitSign(PrivateKey privateKey) ！
 * 公钥验签 -> SignatureSpi#engineInitVerify(PublicKey publicKey) ！
 *
 * 在加解密过程中，要注意：
 *
 * 字节数组和字符串转换时的编码格式要保持一致！
 * 其次：
 *     公钥： 先用 X509EncodedKeySpec 编码转换  ！
 *     私钥： 先用 PKCS8EncodedKeySpec 编码转换 ！
 *
 */
public class MD5withRSAUtils {

    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 签名算法  RSASignature#engineVerify
     *  将明文通过MD5数字摘要后，将摘要再次通过生成的RSA私钥生成数字签名RSASignature#encodeSignature;
     *  将明文与数字签名发送给对方，（公钥提前给）对方拿到公钥对数字签名进行解签RSASignature#decodeSignature，与明文经过MD5加密后数据进行比较
     *  如果一致则通过
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * 获取公钥的key
     */
    public static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 获取私钥的key
     */
    public static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * <p>
     * 生成密钥对(公钥和私钥)
     * </p>
     *
     * @return
     * @throws Exception
     */
    public static Map<String, Key> genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Key> keyMap = new HashMap<String, Key>(2);
        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * <p>
     * 生成密钥对(公钥和私钥)
     * </p>
     * <p>
     * BASE64编码
     * </p>
     *
     * @return String[0] : private_key , String[1] : public_key
     * @throws Exception
     */
    public static String[] genKeyPairString() throws Exception {
        String[] keyPair = new String[2];
        Map<String, Key> keyMap = genKeyPair();
        keyPair[0] = MD5withRSAUtils.getPrivateKey(keyMap);
        keyPair[1] = MD5withRSAUtils.getPublicKey(keyMap);
        return keyPair;
    }

    /**
     * <p>
     * 用私钥对信息生成数字签名
     * </p>
     *
     * @param data       数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = base64Decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return base64Encode(signature.sign());
    }

    /**
     * <p>
     * 公钥校验数字签名！！！
     * </p>
     *
     * @param data      数据
     * @param publicKey 公钥(BASE64编码)
     * @param sign      数字签名
     * @return
     * @throws Exception
     */
    public static boolean verify(byte[] data, String publicKey, String sign) throws Exception {
        byte[] keyBytes = base64Decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(base64Decode(sign));
    }

    /**
     * <P>
     * 私钥解密
     * </p>
     *
     * @param encryptedData 已加密数据
     * @param privateKey    私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey) throws Exception {
        byte[] keyBytes = base64Decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        RSAPrivateKeySpec keySpec = keyFactory.getKeySpec(privateK, RSAPrivateKeySpec.class);
        BigInteger modulus = keySpec.getModulus();
        int keyLengtth = modulus.toString(2).length();//转换为二进制
        int maxDecryptBlock = keyLengtth >> 3; //最大解密长度
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxDecryptBlock) {
                cache = cipher.doFinal(encryptedData, offSet, maxDecryptBlock);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxDecryptBlock;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * <P>
     * 私钥解密
     * </p>
     *
     * @param base64EncryptedData 已加密数据，使用base64编码
     * @param privateKey          私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(String base64EncryptedData, String privateKey) throws Exception {
        byte[] base64DecodeData = base64Decode(base64EncryptedData);
        return decryptByPrivateKey(base64DecodeData, privateKey);
    }

    /**
     * <p>
     * 公钥解密
     * </p>
     *
     * @param encryptedData 已加密数据
     * @param publicKey     公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey) throws Exception {
        byte[] keyBytes = base64Decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        RSAPublicKeySpec  keySpec = keyFactory.getKeySpec(publicK, RSAPublicKeySpec.class);
        BigInteger modulus = keySpec.getModulus();
        int keyLengtth = modulus.toString(2).length();//转换为二进制
        int maxDecryptBlock = keyLengtth >> 3; //最大解密长度
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxDecryptBlock) {
                cache = cipher.doFinal(encryptedData, offSet, maxDecryptBlock);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxDecryptBlock;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * <p>
     * 公钥解密
     * </p>
     *
     * @param base64EncryptedData 已加密数据，使用base64编码
     * @param publicKey           公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(String base64EncryptedData, String publicKey) throws Exception {
        byte[] decodeData = base64Decode(base64EncryptedData);
        return decryptByPublicKey(decodeData, publicKey);
    }

    /**
     * <p>
     * 公钥加密
     * </p>
     *
     * @param data      源数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws Exception {
        byte[] keyBytes = base64Decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        RSAPublicKeySpec keySpec = keyFactory.getKeySpec(publicK, RSAPublicKeySpec.class);
        BigInteger modulus = keySpec.getModulus();
        int keyLengtth = modulus.toString(2).length();//转换为二进制
        int maxEncryptBlock = (keyLengtth >> 3) - 11; //最大加密长度
        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxEncryptBlock) {
                cache = cipher.doFinal(data, offSet, maxEncryptBlock);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxEncryptBlock;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * <p>
     * 公钥加密
     * </p>
     *
     * @param data      源数据
     * @param publicKey 公钥(BASE64编码)
     * @return 加密数据(BASE64编码)
     * @throws Exception
     */
    public static String encryptByPublicKeyString(byte[] data, String publicKey) throws Exception {
        byte[] encryptData = encryptByPublicKey(data, publicKey);
        return base64Encode(encryptData);
    }

    /**
     * <p>
     * 私钥加密
     * </p>
     *
     * @param data       源数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = base64Decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        RSAPrivateKeySpec keySpec = keyFactory.getKeySpec(privateK, RSAPrivateKeySpec.class);
        BigInteger modulus = keySpec.getModulus();
        int keyLengtth = modulus.toString(2).length();//转换为二进制
        int maxEncryptBlock = (keyLengtth >> 3) - 11; //最大加密长度
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxEncryptBlock) {
                cache = cipher.doFinal(data, offSet, maxEncryptBlock);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxEncryptBlock;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * <p>
     * 私钥加密
     * </p>
     *
     * @param data       源数据
     * @param privateKey 私钥(BASE64编码)
     * @return 加密数据(BASE64编码)
     * @throws Exception
     */
    public static String encryptByPrivateKeyString(byte[] data, String privateKey) throws Exception {
        byte[] encryptData = encryptByPrivateKey(data, privateKey);
        return base64Encode(encryptData);
    }

    /**
     * <p>
     * 获取私钥
     * </p>
     *
     * @param keyMap 密钥对
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Key> keyMap) throws Exception {
        Key key = keyMap.get(PRIVATE_KEY);
        return base64Encode(key.getEncoded());
    }

    /**
     * <p>
     * 获取公钥
     * </p>
     *
     * @param keyMap 密钥对
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Key> keyMap) throws Exception {
        Key key = keyMap.get(PUBLIC_KEY);
        return base64Encode(key.getEncoded());
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }

    private static byte[] base64Decode(String base64Data) {
        return Base64.decodeBase64(base64Data);
    }

    /**
     * 解密来自JS的加密信息，与security.js加密对应
     *
     * @param encrypttext
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String decryptStringFromJs(String encrypttext, String privateKey) throws Exception {
        if (StringUtils.isEmpty(encrypttext)) {
            return "";
        }

        byte[] encryptData = Hex.decodeHex(encrypttext.toCharArray());

        byte[] decryptData = decryptByPrivateKey(encryptData, privateKey);

        return StringUtils.reverse(new String(decryptData));
    }

    public static void main(String[] args) throws Exception {
        //getKey();

        String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJc/QDBb7iqCVrdIcFQPaVk+DxysaCo97WH7MKah7WYHv31J4G+GgFiO4D68nRjRe4z78d87nXwtCsp2BF/s9A06QwGkC7X26avcUmnM+ohULGM1lpUfGkjFOOSrpCqw+RQNwSogUimGzREGfakcRW7C9hi7z0Uf2AHw7f8DSFJlAgMBAAECgYBsqc/HLrUtK1l78vppGm145YJb+m4YM0fShnLMtTfnfxePvz0VQ1AA0B3CVSaVByQ6BWUgGieqaH4BOcso1Af+PzwcDaJxxjwkGW/982nzo+/a7w/2Cux//7CeOm7zp1l0YNsNrXvOVwOSiLDr+Bo6Vt2Ff8C6PjIGTIToIeLOPQJBAOMoVzufE9WCYh++b5XyJ0wpiDpDPXuuGeiULonriMkMeFdWOXpi4d+3cpwHZZfx9oOv0R/vA5+8w0lueMtM2hMCQQCqc3i+Fi2A7+EdfduQhTvC7R57dDKJfZ6u4ekWNzGi3l7LhY/fXkbWjh73Xp1fqHPi3lKgzlzgQgTAwz0AA7CnAkEAgoTzWnaU1uELsGGji+yPV9ulQpTQBviKOnCqLOUAu0RHASex7vhSAFFPsQcvAJcupYuoBUk4M9gp/U9UaLvHsQJAMkgwFTsrnz6kVPPcwoxbLcyPUnHbuq2BEyv3e6M6lEYvBrDW2VjRYte4ENcra52g2gslquRVh55SEp9FrmxoPwJAVQxTj10WEs4Y3jHFcQvnq4pHt+EBjx3kRRO94xkGUioj22WxEb0N67cnt+OXc39NWhLkCCIMUjcqztt2JkcF7w==";

        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCXP0AwW+4qgla3SHBUD2lZPg8crGgqPe1h+zCmoe1mB799SeBvhoBYjuA+vJ0Y0XuM+/HfO518LQrKdgRf7PQNOkMBpAu19umr3FJpzPqIVCxjNZaVHxpIxTjkq6QqsPkUDcEqIFIphs0RBn2pHEVuwvYYu89FH9gB8O3/A0hSZQIDAQAB";

        byte[] data = "T2687934173621741{\"mobile\":\"15036248519\",\"password\":\"123564\"}1639099349985".getBytes();
        byte[] data2 = "T2687934173621741{\"password\":\"123564\",\"mobile\":\"15036248519\"}1639099349985".getBytes();
        byte[] data3 = "T2687934173621741{\"mobile\":\"15036248519\",\"password\":\"123564\",\"no\":\"\"}1639099349985".getBytes();

        // 用私钥加签
        String signData = MD5withRSAUtils.sign(data, privateKey);
        System.out.println(new String(MD5withRSAUtils.decryptByPublicKey(signData, publicKey))); // 加签/验签 和 加密/解密 是2个不同的事情！！！
        System.out.println(new String(MD5withRSAUtils.decryptByPublicKey(MD5withRSAUtils.encryptByPrivateKey(data, privateKey),publicKey)));

        System.err.println("签名: " + signData);
        try {
            // 字符串有任何变动都会校验失败！ 所以最好是String传递；
            // 如果非要是对象，一定要去除掉null的属性，并按属性名称排序后加解签名! 因为版本更替时，字段可能调用方和服务方会不一致！
            System.out.println( MD5withRSAUtils.verify(data, publicKey, signData));
            System.out.println( MD5withRSAUtils.verify(data3, publicKey, signData));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}