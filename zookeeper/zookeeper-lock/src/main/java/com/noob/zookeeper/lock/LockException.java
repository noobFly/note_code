package com.noob.zookeeper.lock;

/**
 * Created by noob on 2017/4/5.
 */
public class LockException extends RuntimeException {

    public LockException(String e) {
        super(e);
    }

    public LockException(Exception e) {
        super(e);
    }
}
