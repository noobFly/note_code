
package com.noob.dataCheck;

import com.google.common.base.Strings;
import com.noob.json.JSON;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.Map;

@Data
public class DataCheckColumnMapping {
    //columns START
    /**
     * 编号
     */
    private Long id;
    /**
     * 主题
     */
    private Integer topic;
    /**
     * Excel列名
     */
    private String title;
    /**
     * 关联的字段名
     */
    private String columnName;
    /**
     * 数据类型 //取DataTypeEnum的名称
     */
    private String dataType;
    /**
     * 是否主键
     */
    private String primaryKey;
    /**
     * 额外特殊处理的json
     */
    private String extra;

    // 对应的字段方法
    private Method getter;

    public Map<String, String> getProperties(){
        String extraProperties = this.getExtra();
        return  Strings.isNullOrEmpty(extraProperties) ? null : JSON.parseMapString(extraProperties);
    }

}

