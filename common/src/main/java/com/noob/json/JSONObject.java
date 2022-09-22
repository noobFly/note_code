package com.noob.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.noob.util.TimeUtil;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用消息对象，基于Map实现的可嵌套数据结构。 支持JSON数据结构。
 */
public class JSONObject extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    private static final Pattern arrayNamePattern = Pattern.compile("(\\w+)((\\[\\d+\\])+)");

    public JSONObject() {
        super();
    }

    public JSONObject(final Map<String, Object> other) {
        super(other);
    }

    @Override
    public String toString() {
        return JSON.toPrettyJSONString(this);
    }

    /**
     * 转换为紧凑格式的字符串。
     *
     * @return 返回本对象紧凑格式字符串。
     */
    public String toCompactString() {
        return JSON.toJSONString(this);
    }

    /**
     * 获取指定字段的整数值。如果字段不存在，或者无法转换为整数，返回null。
     *
     * @param name 字段名，支持多级。
     * @return 返回指定的整数值，或者null。
     */
    public Integer intValue(final String name) {
        return valueAsInt(getValue(name));
    }

    /**
     * 获取指定字段的整数值。如果字段不存在，或者无法转换为整数，返回defaultValue。
     *
     * @param name         字段名，支持多级。
     * @param defaultValue 查询失败时，返回的值。
     * @return 返回指定的整数值，或者defaultValue。
     */
    public Integer intValue(final String name, final Integer defaultValue) {
        return ObjectUtils.defaultIfNull(intValue(name), defaultValue);
    }

    /**
     * 获取指定字段的长整数值。如果字段不存在，或者无法转换为长整数，返回null。
     *
     * @param name 字段名，支持多级。
     * @return 返回指定的长整数值，或者null。
     */
    public Long longValue(final String name) {
        return valueAsLong(getValue(name));
    }

    /**
     * 获取指定字段的长整数值。如果字段不存在，或者无法转换为长整数，返回defaultValue。
     *
     * @param name         字段名，支持多级。
     * @param defaultValue 查询失败时，返回的值。
     * @return 返回指定的长整数值，或者defaultValue。
     */
    public Long longValue(final String name, final Long defaultValue) {
        return ObjectUtils.defaultIfNull(longValue(name), defaultValue);
    }

    /**
     * 获取指定字段的布尔值。如果字段不存在，或者无法转换为布尔型，返回null。
     *
     * @param name 字段名，支持多级。
     * @return 返回指定的布尔值，或者null。
     */
    public Boolean booleanValue(final String name) {
        return valueAsBoolean(getValue(name));
    }

    /**
     * 获取指定字段的布尔值。如果字段不存在，或者无法转换为布尔型，返回defaultValue。
     *
     * @param name         字段名，支持多级。
     * @param defaultValue 查询失败时，返回的值。
     * @return 返回指定的布尔值，或者defaultValue。
     */
    public Boolean booleanValue(final String name, final Boolean defaultValue) {
        return ObjectUtils.defaultIfNull(booleanValue(name), defaultValue);
    }

    /**
     * 获取指定字段的字符串值。如果字段不存在，返回null。
     *
     * @param name 字段名，支持多级。
     * @return 返回指定的字符串值，或者null。
     */
    public String stringValue(final String name) {
        return valueAsString(getValue(name));
    }

    /**
     * 获取指定字段的字符串值。如果字段不存在，返回defaultValue。
     *
     * @param name         字段名，支持多级。
     * @param defaultValue 查询失败时，返回的值。
     * @return 返回指定的字符串值，或者defaultValue。
     */
    public String stringValue(final String name, final String defaultValue) {
        return ObjectUtils.defaultIfNull(stringValue(name), defaultValue);
    }

    /**
     * 获取指定字段的值。
     *
     * @param name 字段名，支持多级，支持数组下标。
     * @return 返回指定字段的值。
     */
    public Object getValue(final String name) {
        final int indexDot = name.indexOf('.');
        if (indexDot >= 0) {
            return obj(name.substring(0, indexDot)).getValue(name.substring(indexDot + 1));
        } else {
            final Matcher matcher = arrayNamePattern.matcher(name);
            if (matcher.find()) {
                return endArray(matcher.group(1), matcher.group(2), new EndArrayCallback<Object>() {
                    @Override
                    public Object callback(JSONArray arr, int index) {
                        return elementAt(arr, index);
                    }
                });
            } else {
                return get(name);
            }
        }
    }

    /**
     * 设置指定字段的值。
     *
     * @param name  字段名，支持多级，支持数组下标。
     * @param value 字段值。
     * @return 返回本对象。
     */
    public JSONObject setValue(final String name, final Object value) {
        final int indexDot = name.indexOf('.');
        if (indexDot >= 0) {
            obj(name.substring(0, indexDot)).setValue(name.substring(indexDot + 1), value);
        } else {
            final Matcher matcher = arrayNamePattern.matcher(name);
            if (matcher.find()) {
                endArray(matcher.group(1), matcher.group(2), new EndArrayCallback<Void>() {
                    @Override
                    public Void callback(JSONArray arr, int index) {
                        elementAt(arr, index, value);
                        return null;
                    }
                });
            } else {
                set(name, value);
            }
        }
        return this;
    }

    /**
     * 获取对象（非标量类型）字段。返回的数据是一个结构体。当不存在指定对象时，则为指定的名字创建一个空的MessageObject对象。
     *
     * @param name 字段名。不支持多级名字，支持数组下标。
     * @return 返回指定的对象。如果对象不存在，则为指定的名字创建一个空的MessageObject对象。
     */
    public JSONObject obj(final String name) {
        final Matcher matcher = arrayNamePattern.matcher(name);
        if (matcher.find()) {
            return endArray(matcher.group(1), matcher.group(2), new EndArrayCallback<JSONObject>() {
                @Override
                public JSONObject callback(JSONArray arr, int index) {
                    return objectAt(arr, index);
                }
            });
        } else {
            JSONObject obj = getObject(name);
            if (obj == null) {
                obj = new JSONObject();
                put(name, obj);
            }
            return obj;
        }
    }

    /**
     * 获取数组字段。将名字对应的对象以数组对象返回，当指定的字段不存在时，创建一个空的数组。
     *
     * @param name 字段名。不支持多级名字，不支持下标。
     * @return 返回一个数组（List）。
     */
    public JSONArray array(final String name) {
        JSONArray arr = getArray(name);
        if (arr == null) {
            arr = new JSONArray();
            put(name, arr);
        }
        return arr;
    }

    /**
     * 获取对象（非标量类型）字段。返回的数据是一个结构体。
     *
     * @param name 字段名。
     * @return 返回指定的对象字段。
     */
    public JSONObject getObject(final String name) {
        return (JSONObject) get(name);
    }

    /**
     * 获取对象（非标量类型）字段。转换成指定的类对象。
     *
     * @param name  字段名。
     * @param clazz 类名。
     * @return 返回指定的类对象。
     */
    public <T> T getObject(final String name, final Class<T> clazz) {
        Object obj = get(name);
        return JSON.parseObject(JSON.toJSONString(obj), clazz);
    }

    /**
     * 获取数组类型字段。
     *
     * @param name 字段名。
     * @return 返回数组类型字段。
     */
    public JSONArray getArray(final String name) {
        return (JSONArray) get(name);
    }

    /**
     * 返回字段整数值。如果不存在，返回null。
     *
     * @param name 字段名。
     * @return 返回指定字段整数值。
     */
    public Integer getInt(final String name) {
        return valueAsInt(get(name));
    }

    /**
     * 返回字段整数值。如果不存在，返回defaultValue。
     *
     * @param name         字段名。
     * @param defaultValue 字段不存在时，返回的值。
     * @return 返回指定字段整数值。
     */
    public Integer getInt(final String name, Integer defaultValue) {
        return ObjectUtils.defaultIfNull(getInt(name), defaultValue);
    }

    /**
     * 返回字段整数值。如果不存在，返回0。
     *
     * @param name 字段名。
     * @return 字段值。
     */
    public int getIntValue(final String name) {
        Object value = get(name);
        return value == null ? 0 : valueAsInt(value);
    }

    /**
     * 返回字段长整数值。如果不存在，返回null。
     *
     * @param name 字段名。
     * @return 返回指定字段长整数值。
     */
    public Long getLong(final String name) {
        return valueAsLong(get(name));
    }

    /**
     * 返回字段长整数值。如果不存在，返回defaultValue。
     *
     * @param name         字段名。
     * @param defaultValue 字段不存在时，返回的值。
     * @return 返回指定字段长整数值。
     */
    public Long getLong(final String name, Long defaultValue) {
        return ObjectUtils.defaultIfNull(getLong(name), defaultValue);
    }

    /**
     * 返回字段长整数值。如果不存在，返回0。
     *
     * @param name 字段名。
     * @return 字段值。
     */
    public long getLongValue(final String name) {
        Object value = get(name);
        return value == null ? 0L : valueAsLong(value);
    }

    /**
     * 返回字段字符串值。如果不存在，返回null。
     *
     * @param name 字段名。
     * @return 返回指定字段字符串值。
     */
    public String getString(final String name) {
        return valueAsString(get(name));
    }

    /**
     * 返回字段字符串值。如果不存在，返回defaultValue。
     *
     * @param name         字段名。
     * @param defaultValue 字段不存在时，返回的值。
     * @return 返回指定字段字符串值。
     */
    public String getString(final String name, final String defaultValue) {
        return ObjectUtils.defaultIfNull(getString(name), defaultValue);
    }

    /**
     * 字段值按照布尔类型返回。如果不存在，返回null。
     *
     * @param name 字段名。
     * @return 字段值。
     */
    public Boolean getBoolean(final String name) {
        return valueAsBoolean(get(name));
    }

    /**
     * 字段值按照布尔类型返回。如果不存在，返回defaultValue。
     *
     * @param name         字段名。
     * @param defaultValue 字段不存在时，返回的值。
     * @return 字段值。
     */
    public Boolean getBoolean(final String name, final Boolean defaultValue) {
        return ObjectUtils.defaultIfNull(getBoolean(name), defaultValue);
    }

    /**
     * 字段值按照布尔类型返回。如果不存在，返回false。
     *
     * @param name 字段名。
     * @return 字段值。
     */
    public boolean getBooleanValue(final String name) {
        Object value = get(name);
        return value == null ? false : valueAsBoolean(value);
    }

    /**
     * 返回字段双精度浮点数值。如果不存在，返回null。
     *
     * @param name 字段名。
     * @return 返回指定字段双精度浮点数值。
     */
    public Double getDouble(final String name) {
        return valueAsDouble(get(name));
    }

    /**
     * 返回字段双精度浮点数值。如果不存在，返回defaultValue。
     *
     * @param name         字段名。
     * @param defaultValue 字段不存在时，返回的值。
     * @return 返回指定字段双精度浮点数值。
     */
    public Double getDouble(final String name, final Double defaultValue) {
        return ObjectUtils.defaultIfNull(getDouble(name), defaultValue);
    }

    /**
     * 返回字段双精度浮点数值。如果不存在，0.0。
     *
     * @param name 字段名。
     * @return 返回指定字段双精度浮点数值。
     */
    public double getDoubleValue(final String name) {
        Object value = get(name);
        return value == null ? 0.0D : valueAsDouble(value);
    }

    /**
     * 获取指定字段的数字类型
     *
     * @param name
     * @return
     */
    public BigDecimal getBigDecimal(final String name) {
        Object value = getValue(name);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    /**
     * 获取指定字段的数字类型。如果字段不存在，返回defaultValue。
     *
     * @param name
     * @param defaultValue
     * @return
     */
    public BigDecimal getBigDecimal(final String name, final BigDecimal defaultValue) {
        return ObjectUtils.defaultIfNull(getBigDecimal(name), defaultValue);
    }

    /**
     * 获取指定字段的日期类型
     *
     * @param name
     * @return
     */
    public Date getDate(final String name) {
        Object value = getValue(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        return TimeUtil.parseDate(value.toString());
    }

    /**
     * 设置字段值
     *
     * @param name  字段名
     * @param value 字段值（标量：数字、字符串、布尔型；结构体：MessageObject）。
     *              如果是Map类型同时非MessageObject类型，则自动转换为MessageObject类型再存入
     *              （此时，再修改Map中的数据，将不会体现到本对象中）。
     * @return 返回本对象
     */
    public JSONObject set(final String name, final Object value) {
        put(name, value);
        return this;
    }

    /**
     * 将本对象转换为Java Bean。
     *
     * @param beanClass Java Bean的类对象。
     * @return 返回转换后的Java Bean。
     */
    public <T> T asBean(Class<T> beanClass) {
        return JSON.parseObject(JSON.toJSONString(this), beanClass);
    }

    /**
     * 将本对象转换为Java Bean。
     *
     * @param typeReference Java Bean的类对象。
     * @return 返回转换后的Java Bean。
     */
    public <T> T asBean(TypeReference<T> typeReference) {
        return JSON.parseObject(JSON.toJSONString(this), typeReference);
    }

    /**
     * 重载基类的方法。如果 value 是 Map 类型，但不是 MessageObject 类型，则创建一个包含内容等同于原 Map 的
     * MessageObject 作为 value（注意：此后再更改 Map 的内容，将不会反映到 MessageObject 中）。
     * 重载此方法的目的是为了使JSON能够正确地解析为MessageObject对象。不建议直接调用此方法，请使用 set(name,
     * value)方法设置字段值。
     */
    @Override
    public Object put(String key, Object value) {
        return super.put(key, transfer(value));
    }

    public static Integer valueAsInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return Integer.valueOf((String) value);
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        } else {
            return null;
        }
    }

    public static Long valueAsLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.valueOf((String) value);
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1L : 0L;
        } else {
            return null;
        }
    }

    public static String valueAsString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value instanceof JSONObject) {
            return ((JSONObject) value).toCompactString();
        } else if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    public static Boolean valueAsBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0.0;
        } else if (value instanceof String) {
            return Boolean.valueOf((String) value);
        } else {
            return null;
        }
    }

    public static Double valueAsDouble(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            return Double.valueOf((String) value);
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1.0D : 0.0D;
        } else {
            return null;
        }
    }

    /**
     * 将所有层次中凡是Map类型同时又不是MessageObject的类型，转换为MessageObject类型。
     *
     * @param value 值。
     * @return 返回转换后的值。
     */
    @SuppressWarnings("unchecked")
    private static Object transfer(final Object value) {
        if (!(value instanceof JSONObject) && value instanceof Map) {
            return toObject((Map<String, Object>) value);
        } else if (!(value instanceof JSONArray) && value instanceof Collection) {
            return toArray((Collection<Object>) value);
        } else {
            return value;
        }
    }

    private static JSONArray toArray(final Collection<Object> list) {
        final JSONArray arr = new JSONArray(list.size());
        for (final Object element : list) {
            arr.add(element);
        }
        return arr;
    }

    private static JSONObject toObject(final Map<String, Object> map) {
        final JSONObject obj = new JSONObject();
        for (final Map.Entry<String, Object> ent : map.entrySet()) {
            obj.put(ent.getKey(), transfer(ent.getValue()));
        }
        return obj;
    }

    /**
     * 将指定下标元素作为数组返回，如果不存在，则在该位置创建一个空的数组。
     *
     * @param arr   当前数组。
     * @param index 下标。
     * @return 返回当前数组指定下标的元素，该元素应该是一个数组。
     */
    private static JSONArray arrayAt(JSONArray arr, int index) {
        expand(arr, index);
        if (arr.get(index) == null) {
            arr.set(index, new JSONArray());
        }
        return (JSONArray) arr.get(index);
    }

    /**
     * 将指定下标元素作为结构体返回，如果不存在，则在该位置创建一个空的结构体。
     *
     * @param arr   当前数组。
     * @param index 下标。
     * @return 返回当前数组指定下标元素，该元素是一个结构体。
     */
    private static JSONObject objectAt(final JSONArray arr, int index) {
        expand(arr, index);
        if (arr.get(index) == null) {
            arr.set(index, new JSONObject());
        }
        return (JSONObject) arr.get(index);
    }

    /**
     * 设置数组指定下标位置的值。
     *
     * @param arr   数组。
     * @param index 下标。
     * @param value 值。
     */
    private static void elementAt(final JSONArray arr, final int index, final Object value) {
        expand(arr, index).set(index, value);
    }

    /**
     * 获取数组指定下标元素的值。
     *
     * @param arr   数组。
     * @param index 下标。
     * @return 值。
     */
    private static Object elementAt(final JSONArray arr, final int index) {
        return expand(arr, index).get(index);
    }

    /**
     * 扩展数组到指定下标，以防止访问时下标越界。
     *
     * @param arr   数组
     * @param index 下标
     * @return 返回传入的数组
     */
    private static JSONArray expand(final JSONArray arr, final int index) {
        while (arr.size() <= index) {
            arr.add(null);
        }
        return arr;
    }

    /**
     * 最后数组回调。
     *
     * @param <T> 回调返回数据类型。
     * @author Mike
     */
    private interface EndArrayCallback<T> {
        /**
         * 当定位到最后一级数组，将调用本方法。
         *
         * @param arr   最后一级数组对象。
         * @param index 最后一级索引。
         * @return 返回回调的返回值。
         */
        T callback(JSONArray arr, int index);
    }

    /**
     * 处理多维数组的工具函数（包括一维数组）。多维数组的名字如：arrary[1][2][3]，
     * 则name=array，indexStr=[1][2][3]，在callback中，endArr将是
     * array[1][2]指定的对象，indexe=3。
     *
     * @param name       不带下标的名字，不支持多级名字。
     * @param indexesStr 索引部分的字符串，如：[1][2][3]
     * @param callback   回调函数。
     * @return 返回回调函数的返回值。
     */
    private <T> T endArray(final String name, final String indexesStr, final EndArrayCallback<T> callback) {
        JSONArray endArr = array(name);
        final int[] indexes = parseIndexes(indexesStr);
        int i = 0;
        while (i < indexes.length - 1) {
            endArr = arrayAt(endArr, indexes[i++]);
        }
        return callback.callback(endArr, indexes[i]);
    }

    private static int[] parseIndexes(final String s) {
        int[] indexes = null;
        List<Integer> list = new ArrayList<Integer>();

        final StringTokenizer st = new StringTokenizer(s, "[]");
        while (st.hasMoreTokens()) {
            final int index = Integer.valueOf(st.nextToken());
            if (index < 0) {
                throw new RuntimeException(String.format("Illegal index %1$d in \"%2$s\"", index, s));
            }

            list.add(index);
        }

        indexes = new int[list.size()];
        int i = 0;
        for (Integer tmp : list.toArray(new Integer[list.size()])) {
            indexes[i++] = tmp;
        }

        return indexes;
    }
}
