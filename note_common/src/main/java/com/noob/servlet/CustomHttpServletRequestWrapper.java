package com.noob.servlet;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 重写获取流的方式 .
 * TODO 这种wrap方式会占用额外内存, 需要在destroy时主动设置缓存的字节数组为空
 */
@Slf4j
public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private byte[] body;        // 真实的业务数据
    private String charsetName; // 编码字符集


    public CustomHttpServletRequestWrapper(HttpServletRequest request, String requestBody) {
        super(request);
        this.charsetName = request.getCharacterEncoding();
        if (StringUtils.isNotBlank(requestBody)) {
            try {
                body = requestBody.getBytes(getCharsetName());
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }


    public String getCharsetName() {
        return charsetName == null ? "UTF-8" : charsetName;
    }

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
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharsetName()));
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() {
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