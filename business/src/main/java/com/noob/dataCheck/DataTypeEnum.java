package com.noob.dataCheck;

import com.google.common.base.Strings;
import com.noob.util.TimeUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;


// 映射的字段类型
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DataTypeEnum {
    NUMBER {
        public String clear(Object data, Map<String, String> properties) {
            String val = super.clear(data, properties);
            return Strings.nullToEmpty(val);
        }

        // 数字类型一定有配置小数位
        public boolean compare(String a, String b, Map<String, String> extraProperties) {
            boolean isSame = a.equals(b);
            if (!isSame) {
                int scale = Integer.parseInt(extraProperties.get(ExtraKey.scale));
                int model = Integer.parseInt(extraProperties.get(ExtraKey.rounding_model));
                try {
                    return new BigDecimal(a).setScale(scale, model).compareTo(new BigDecimal(b).setScale(scale, model)) == 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return isSame;
        }
    }, DATE {
        public String clear(Object data, Map<String, String> properties) {
            String val = super.clear(data, properties);
            if (!Strings.isNullOrEmpty(val) && MapUtils.isNotEmpty(properties)) {
                if ("array".equals(properties.get(ExtraKey.date_model))) {
                    String[] array = val.split(",");
                    if (array.length > 0) {
                        val = array[0];
                    }
                }

                val = TimeUtil.dateTime(TimeUtil.parseDate(val));
            }
            return Strings.nullToEmpty(val);
        }

        public boolean compare(String a, String b, Map<String, String> extraProperties) {
            try {
                return TimeUtil.parseDate(a).compareTo(TimeUtil.parseDate(b)) == 0;
            } catch (Exception e) {
                System.out.println(String.format("DATE比较异常: a: %s  b: %s", a, b));
                e.printStackTrace();
                return false;
            }
        }
    },

    DEFAULT {
        public boolean compare(String a, String b, Map<String, String> extraProperties) {
            return a.equals(b);
        }
    };

    public abstract boolean compare(String a, String b, Map<String, String> extraProperties);

    // 外部数据里不同的数据有差异化的清理规则! 需要让字符强制复核约定的类型
    public String clear(Object data, Map<String, String> properties) {
        String defaultValue = StringUtils.EMPTY;

        String clearRegex = null;
        boolean notEmpty = properties != null && !properties.isEmpty();
        if (notEmpty) {
            String defaultValueConfig = properties.get(ExtraKey.default_value);
            if (defaultValueConfig != null) {
                defaultValue = defaultValueConfig;
            }
            String clearChar = properties.get(ExtraKey.clear);
            if (!Strings.isNullOrEmpty(clearChar)) {
                clearRegex = clearChar;
            }
        }

        if (data == null) return defaultValue;

        String val = Strings.nullToEmpty(String.valueOf(data)).trim();
        if (!Strings.isNullOrEmpty(clearRegex)) {
            val = val.replaceAll(clearRegex, StringUtils.EMPTY);
        }
        val = val.trim();
        return Strings.isNullOrEmpty(val) ? defaultValue : val;
    }


    private interface ExtraKey {
        String clear = "clear";
        String default_value = "defaultValue";
        String date_model = "dateModel";
        String scale = "scale";
        String rounding_model = "roundingMode";

    }

}
