package com.noob.json;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
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
 */
public class JSON {

    private static final Logger logger = LoggerFactory.getLogger(JSON.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter(); // 也可以： mapper.enable(SerializationFeature.INDENT_OUTPUT);


    static {
        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 设置为中国上海时区
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 空值不序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 一般而言：空值也需要序列化
        // 去掉默认的时间戳格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // objectMapper.registerModule(new JavaTimeModule());
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
                    return null;
                }

                @Override
                public Type[] getActualTypeArguments() {
                    return new Class[]{rawType};
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
        if (StringUtils.isBlank(str)) {
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
            return objectMapper.readValue(str, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }

    public static Map<String, Object> parseMapObject(String str) {
        try {
            return objectMapper.readValue(str, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
    }

    public static <T> T toJavaObject(Object value, Class<T> valueType) {
        return parseObject(toJSONString(value), valueType);
    }

    /**
     * 忽略指定对象属性！  非线程安全
     *
     * @param bean
     * @param ignoreVar
     * @return
     */
    public static String toJSON(Object bean, Class cla, String... ignoreVar) {
        /**
         * 声明式 缺点：无法动态指定
         * 1、@JsonIgnore 可以直接放在field上面表示要忽略的filed
         * 2、@JsonIgnoreProperties(value = { "id",  "firstName"}) 类级别忽略特定字段
         * 3、 @JsonIgnoreType 忽略整个bean ，忽略指定类型class的所有字段
         * 4、@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // 1.9之后： 在Setter方法上加@Jsonignore会导致整个属性在(反)序列化过程中被忽略。所以： 通过设置JsonProperty的access属性来确定当前属性是不是需要自动序列化/反序列化
         * 4、设定日期格式序列化、反序列化的格式 @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd",  timezone = "GMT+8")
         */

        FilterProvider filterProvider = new SimpleFilterProvider()
                .addFilter("ignoreVarFilter", SimpleBeanPropertyFilter.serializeAllExcept(ignoreVar)); // 定义一个过滤器ignoreVarFilter
        objectMapper.setFilters(filterProvider); // 非线程安全

        /**该过滤器要生效，可以：
         * 第一种方式: 在bean的class上指定： @JsonFilter("ignoreVarFilter") ;
         * 第二种方式: 定义一个申明过滤器ignoreVarFilter的接口类，并把它和实体class绑定
         */
       // objectMapper.addMixInAnnotations(cla, JsonFilterMixIn.class);

        return toJSONString(bean);
    }

    @JsonFilter("ignoreVarFilter")
    public  interface JsonFilterMixIn {

    }
}
