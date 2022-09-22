package com.noob.responseAutoWrapper;

import lombok.Data;

// 要区分开： 处理操作状态 和业务状态 分开
@Data
public class Result<T> {
    private int code;
    private String msg;
    private T obj;

    private String innerDetail;

    public Result() {
    }

    public Result(int code, String msg, T res) {
        this.code = code;
        this.msg = msg;
        this.obj = res;
    }


    public static <T> Result successResult(T obj) {
        Result result = new Result();
        result.setCode(0);
        result.setObj(obj);
        return result;
    }
}
