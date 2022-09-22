package com.noob.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 数组结构。
 */
public class JSONArray extends ArrayList<Object> {

    private static final long serialVersionUID = 1L;

    public JSONArray() {
        super();
    }

    public JSONArray(int size) {
        super(size);
    }

    @Override
    public String toString() {
        try {
            return JSON.toJSONString(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object set(int index, Object element) {
        return super.set(index, transfer(element));
    }

    @Override
    public boolean add(Object element) {
        return super.add(transfer(element));
    }

    @Override
    public void add(int index, Object element) {
        super.add(index, transfer(element));
    }

    public JSONObject getJSONObject(int i) {
        Object value = get(i);
        if (value == null) {
            return null;
        }

        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }

        return JSON.parseObject(JSON.toJSONString(value));
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

}
