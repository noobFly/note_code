package com.noob.servlet;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 重写获取流的方式 . 这种wrap方式会占用额外内存, 在destroy时主动设置缓存的字节数组为空 TODO
 */
public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private byte[] body;       // 报文体
    private String charsetName; // 编码字符集

    public CustomHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        charsetName = request.getCharacterEncoding();
        try {
            body = IOUtils.toByteArray(request.getInputStream());
            byte[] body2 = IOUtils.toByteArray(request.getInputStream());
            System.out.println(body2); // 请求流不能重复读！一次性的
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), charsetName));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // do nothing
            }
        };
    }
}