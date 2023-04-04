
package com.noob.dataCheck;

import com.google.common.base.Strings;
import com.noob.json.JSON;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    @NotNull(message = "表的类型不能为空")
    private Integer topic;
    /**
     * Excel列名
     */
    @NotBlank(message = "Excel列名不能为空")
    private String title;
    /**
     * 关联的字段名
     */
    @NotBlank(message = "关联的表字段名不能为空")
    private String columnName;
    /**
     * 数据类型  取DataTypeEnum的名称
     */
    @NotBlank(message = "关联的表字段数据类型不能为空")
    private String dataType;
    /**
     * 是否主键
     */
    @NotBlank(message = "关联的表字段数据[是否主键]不能为空")
    private String primaryKey;
    /**
     * 额外特殊处理的json
     */
    private String extra;

    // 对应的字段方法
    private Method getter;


    public DataCheckColumnMapping(String title) {
        this.title = title;
    }

    public Map<String, String> getProperties() {
        String extraProperties = this.getExtra();
        return Strings.isNullOrEmpty(extraProperties) ? null : JSON.parseMapString(extraProperties);
    }

    public boolean isPk() {
        return "Y".equals(this.primaryKey);
    }


}
