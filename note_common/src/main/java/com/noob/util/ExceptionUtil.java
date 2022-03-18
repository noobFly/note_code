package com.noob.util;

import java.util.function.Supplier;

public interface ExceptionUtil {

    static void throwException(boolean isFail, String failMsg, Exception e) {
        if (isFail) {
            throw new RuntimeException(failMsg, e);
        }
    }

    static void throwException(boolean isFail, String failMsg) {
        if (isFail) {
            throw new RuntimeException(failMsg);
        }
    }

    static void throwException(boolean isFail, Supplier<String> failMsg, Exception e) {
        if (isFail) {
            throw new RuntimeException(failMsg.get(), e);
        }
    }

    static void RuntimeException(boolean isFail, Supplier<String> failMsg) {
        if (isFail) {
            throw new RuntimeException(failMsg.get());
        }
    }
}
