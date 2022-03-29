package com.noob.controller.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SentinelHandler {

    // sentinel指定的限制阻断后的处理方法的入参需要和主体方法是一样的，多一个BlockException ; 返回要一样; 外部Class需要static方法
    public static void handle(HttpServletRequest request, HttpServletResponse response, BlockException blockException) throws IOException {
        System.out.print("Sentinel Block" + blockException.getMessage());
        response.getWriter().print(blockException.getMessage());
        response.flushBuffer();
    }

    public static void fallback( HttpServletResponse response) throws IOException {
        response.getWriter().print("fallback");

    }
}
