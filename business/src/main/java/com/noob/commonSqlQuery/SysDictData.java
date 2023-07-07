package com.noob.commonSqlQuery;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.Data;

@Data
public class SysDictData {

    /**
     * 字典编码
     */
    @JsonFormat(shape = Shape.STRING)
    private Long dictCode;

    /**
     * 字典排序
     */
    @JsonFormat(shape = Shape.STRING)
    private Long dictSort;

    /**
     * 字典标签
     */
    private String dictLabel;

    private String dictValue;

    private String dictType;

}
