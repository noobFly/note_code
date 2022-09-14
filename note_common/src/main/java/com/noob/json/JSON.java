package com.noob.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.noob.json.exception.JsonParseException;
import com.noob.json.exception.JsonSerializeException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * JSON解析处理，替换fastjson库
 * 
 */
public class JSON {

    private static final Logger logger = LoggerFactory.getLogger(JSON.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();

    static {
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 设置为中国上海时区
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 空值不序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 一般而言 控制也需要序列化
        // 去掉默认的时间戳格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    }
	
	public static void toJSONString(OutputStream os, Object value) {
        try {
            objectMapper.writeValue(os, value);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    public static String toJSONString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }
    
    public static String toPrettyJSONString(Object value) {
        try {
            return objectWriter.writeValueAsString(value);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    public static byte[] toJSONBytes(Object value) {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new JsonSerializeException(e);
        }
    }

    public static <T> T parseObject(InputStream is, Class<T> valueType) {
        try {
            return objectMapper.readValue(is, valueType);
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }

    public static <T> T parseObject(String str, Class<T> valueType) {
        try {
            return objectMapper.readValue(str, valueType);
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }

    public static <T> T parseObject(byte[] bytes, Class<T> valueType) {
        try {
            if (bytes == null) {
                bytes = new byte[0];
            }
            return objectMapper.readValue(bytes, 0, bytes.length, valueType);
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }
    
    public static <T> T parseObject(String str, Type valueType) {
        try {
            return objectMapper.readValue(str, TypeFactory.defaultInstance().constructType(valueType));
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }
    
    public static <T> List<T> parseArray(String str, Class<T> rawType) {
        try {
            Type type = new ParameterizedType() {
                
                @Override
                public Type getRawType() {
                    return ArrayList.class;
                }
                
                @Override
                public Type getOwnerType() {
                    return null ;
                }
                
                @Override
                public Type[] getActualTypeArguments() {
                    return new Class[] {rawType};
                }
            };
            return objectMapper.readValue(str, TypeFactory.defaultInstance().constructType(type));
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }
    
    public static <T> T parseObject(String str, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(str, typeReference);
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }
    
    public static JSONObject parseObject(String str) {
    	if(StringUtils.isBlank(str)) {
    		return null;
    	}
        try {
            return objectMapper.readValue(str, JSONObject.class);
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }
    
    public static Map<String, String> parseMapString(String str) {
        try {
            return objectMapper.readValue(str, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }
    
    public static Map<String, Object> parseMapObject(String str) {
        try {
            return objectMapper.readValue(str, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }

    public static <T> T toJavaObject(Object value, Class<T> valueType) {
        return parseObject(toJSONString(value), valueType);
    }

    /**
     * 把对象转换成json数据
     *
     * @param bean
     * @param ignoreVar
     * @return
     */
    public static String toJson(Object bean, String... ignoreVar) {

        BeanInfo beanInfo = null;
        StringBuilder sBuilder = null;
        try {
            sBuilder = new StringBuilder();
            beanInfo = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor propertyDescriptors[] = beanInfo.getPropertyDescriptors();
            sBuilder.append("{");
            for (PropertyDescriptor property : propertyDescriptors) {
                String propertyName = property.getName();
                if (!propertyName.equals("class") && !isContains(propertyName, ignoreVar)) {
                    Method readMethod = property.getReadMethod();
                    String result = (String) readMethod.invoke(bean, new Object[0]);
                    if (result == null) {
                        result = "";
                    }
                    sBuilder.append("\"" + propertyName + "\":\"" + result + "\",");
                    logger.debug("\"" + propertyName + "\":\"" + result + "\"");
                }
            }
            String temp = sBuilder.toString();
            if (temp.length() > 0) {
                String result = temp.substring(0, temp.lastIndexOf(","));
                return result + "}";
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.debug("exception" + e.getMessage());
            return null;
        }
    }
    /**
     * 判断被忽略的字段是否等于当前字段
     *
     * @param propertyName
     * @param ignoreVar
     * @return
     */
    private static boolean isContains(String propertyName, String[] ignoreVar) {
        if (ignoreVar != null && ignoreVar.length > 0) {
            for (String str : ignoreVar) {
                if (propertyName.equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }
}
