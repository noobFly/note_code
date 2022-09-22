//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.noob.gateway.openApi;

import lombok.Data;

@Data
public class BizException extends PlatformException {
    private static final long serialVersionUID = 1L;
    private String innerMsg;


    public BizException(int code, String message) {
        super(code, message);
    }


    public BizException(int code, String message, String innerMsg, Throwable ex) {
        super(code, message, ex);
        this.innerMsg = innerMsg;
    }
}
