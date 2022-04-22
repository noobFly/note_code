//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.noob.controller.gateway.openApi;

public class PlatformException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    protected int code;

    public PlatformException() {
        super("操作失败");
        this.code = -1;
    }

    public PlatformException(String message) {
        super(message);
        this.code = -1;
    }

    public PlatformException(int code, String message) {
        super(message);
        this.code = code;
    }

    public PlatformException(int code, String message, Throwable ex) {
        super(message, ex);
        this.code = code;
    }

    public PlatformException(String message, Throwable ex) {
        super(message, ex);
        this.code = -1;
    }

    public PlatformException(Throwable ex) {
        super(ex);
        this.code = -1;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
