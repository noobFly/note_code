package com.noob.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CustomHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private PrintWriter printWriter;
    private HttpServletResponse servletResponse;
    private byte[] byteData = new byte[0];

    public CustomHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        this.servletResponse = response;
    }

    private static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

    private CustomOutputStream outputStream = new CustomOutputStream() {

        @Override
        public void flush() throws IOException {
            byte[] resultData = this.toByteArray();
            if (resultData.length == 0) {
                return;
            }

            servletResponse.getOutputStream().write(resultData);
            servletResponse.getOutputStream().flush();
            this.reset();

            if (byteData.length == 0) {
                byteData = resultData;
            } else {
                byteData = byteMerger(byteData, resultData);
            }
        }
    };


    @Override
    public ServletOutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * 重写父类的 getWriter() 方法，将响应数据缓存在 PrintWriter 中
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter == null ? printWriter = new PrintWriter(outputStream) : printWriter;
    }

    public byte[] getContent() {
        return byteData;
    }

    public static class CustomOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream outputStream;

        public CustomOutputStream() {
            this.outputStream = new ByteArrayOutputStream();
        }

        @Override
        public void write(int b) {
            outputStream.write(b); // 该方法是write(byte b[])的底层遍历数组所使用方式

        }

        public byte[] toByteArray() {
            return outputStream.toByteArray();
        }

        public void reset() {
            outputStream.reset();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener listener) {

        }
    }
}