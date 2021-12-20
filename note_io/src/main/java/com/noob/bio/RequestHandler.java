package com.noob.bio;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Scanner;

import lombok.extern.slf4j.Slf4j;

/**
 * 与客户端通讯处理
 */
@Slf4j
public class RequestHandler implements Runnable {

    private Socket        clientSocket;       //客户端socket

    private InputStream   inputStream;        // 输入请求
    private OutputStream  outputStream;       // 输出响应
    private SocketAddress clientSocketAddress;

    public RequestHandler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        init();

    }

    private void init() throws IOException {
        inputStream = clientSocket.getInputStream();
        outputStream = clientSocket.getOutputStream();
        clientSocketAddress = clientSocket.getRemoteSocketAddress();

    }

    /**
     * 接收请求并返回响应。 接收、响应的字符中是否带\n以读取方式有关
     */
    public void run() {
        log.info(String.format("客户端连接: %s", clientSocketAddress));

        try { 
        	
        	while(true) {
    	 
              handleWithoutLineBreak();
             // handleWithLineBreak1();
              // handleWithLineBreak2();
              keepOut();
          }
      }  catch (Exception e) {
          e.printStackTrace();
          try {
        	    clientSocket.shutdownInput();
        	    clientSocket.shutdownOutput();
				clientSocket.close(); // 只是关闭服务端这边的socket输入和输出， 客户端也需要自己主动关闭。
			} catch (IOException e1) {
				e1.printStackTrace();
			}
       }
    }

	private void keepOut() throws Exception {
		while (true) {
			String msg = "飞机 大炮 坦克 \n";
			outputStream.write(msg.getBytes());
			outputStream.flush();
			Thread.sleep(100);
		}
	}

	/**
     * 读入时，不要求客户端传入换行。
     * <p>
     * 客户端异常退出时，服务端read()抛出异常： java.net.SocketException: Connection reset
     */
    private void handleWithoutLineBreak() throws IOException {
        BufferedInputStream br = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[1024];
        int turns = 1;
        while (br.read(buffer) != -1) { //阻塞直到有数据到来!  可自定义读取缓存的begin和end: read(b, 11, 50); 并不会清空原有的数据!
            outwrite(turns, new String(buffer));
            turns++;
        }
    }

    /**
     * 读入时，需要客户端传入换行！
     * <p>
     * 如果客户端传递的消息中没有"\n"， 只有在客户端close、shutdownOutput后消息才会全部打印处理, 表示:接收到所有消息;
     * <p>
     * 所以 客户端异常退出时，在outputStream.write()还抛出异常： java.net.SocketException:
     * Connection reset by peer: socket write error
     */
    private void handleWithLineBreak1() throws IOException {
        Scanner input = new Scanner(inputStream);
        int turns = 1;
        while (true) {
            if (input.hasNext()) { // 阻塞，等待客户端数据！！
                outwrite(turns, input.nextLine()); // nextLine(): he position is set to the beginning of the next line.
                turns++;
            }
        }

    }

    /**
     * 读入时，需要客户端传入换行！
     * <p>
     * 如果客户端传递的消息中没有"\n"
     * <p>
     * 当客户端正常shutdownOutput、close 时才会进入当前行执行，得到的结果是null;
     * <p>
     * 客户端异常退出时，服务端readLine()抛出异常： java.net.SocketException: Connection reset
     */
    private void handleWithLineBreak2() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        int turns = 1;
        while (br.readLine() != null) {
            outwrite(turns, br.readLine()); // readLine()会定位nextLine的起始索引
            turns++;
        }
    }

    private void outwrite(int turns, String request) throws IOException {
        log.info("接收到客户端" + clientSocketAddress + "的信息： " + request);

        //在本测试用例中，是否需要输出"\n" 由客户端的读取方式而定！
        String msg = "服务端的感谢" + turns + ", 因为: " + request + "\n";
        log.info("发出信息->>>>" + msg);

        outputStream.write(msg.getBytes());
        outputStream.flush();
    }
}
