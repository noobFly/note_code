package com.noob.json.exception;

public class JsonParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JsonParseException(Throwable e) {
        super(e);
    }
    
    public JsonParseException(String message, Throwable e) {
        super(message, e);
    }
}
