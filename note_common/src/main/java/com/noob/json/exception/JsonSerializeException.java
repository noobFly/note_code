package com.noob.json.exception;

public class JsonSerializeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JsonSerializeException(Throwable e) {
        super(e);
    }
    
    public JsonSerializeException(String message, Throwable e) {
        super(message, e);
    }
}
