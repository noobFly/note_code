package com.noob.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

// 如果每一次都要如此操作class会很低效！ 需要增加缓存
public class ReflectHelper {

    /**
     * 获取obj对象fieldName的Field
     */
    public static Field getFieldByFieldName(Object obj, String fieldName) {
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass =
                superClass.getSuperclass()) {
            try {

                return superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
            }
        }
        return null;
    }

    /**
     * 获取obj对象fieldName的属性值
     *
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static Object getValueByFieldName(Object obj, String fieldName)
            throws SecurityException, IllegalArgumentException,
            IllegalAccessException {
        Field field = getFieldByFieldName(obj, fieldName);
        Object value = null;
        if (field != null) {
            if (field.isAccessible()) {
                value = field.get(obj);
            } else {
                field.setAccessible(true);
                value = field.get(obj);
                field.setAccessible(false);
            }
        }
        return value;
    }

    /**
     * 设置obj对象fieldName的属性值
     *
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public static void setValueByFieldName(Object obj, String fieldName, Object value)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        if (field.isAccessible()) {
            field.set(obj, value);
        } else {
            field.setAccessible(true);
            field.set(obj, value);
            field.setAccessible(false);
        }
    }

    /**
     * map 结构  fieldName->fieldValue
     *
     * @param obj
     */
    public static Map<String, Object> getFieldMap(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass =
                superClass.getSuperclass()) {
            Field[] fields = superClass.getDeclaredFields();
            if (null != fields && 0 < fields.length) {
                try {
                    for (Field field : fields) {
                        String fieldName = field.getName();
                        Object value = getValueByFieldName(obj, fieldName);

                        if (null != value) {
                            map.put(fieldName, value);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        return map;
    }
}
