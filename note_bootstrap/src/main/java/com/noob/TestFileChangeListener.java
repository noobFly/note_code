package com.noob;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.*;

/**
 * linux上的tail命令用到的底层技术就是inotify: 内核提供的监控文件变更事件的系统调用.
 * 通过文件的变化，来达到准实时系统刷新配置。
 */
public class TestFileChangeListener {
    public static void main(String[] args) throws Exception {
        Thread thread = new Thread(() -> watchDir("C:\\Users\\xiongwenjun\\Desktop\\file"));
        thread.start();
        Thread.sleep(2000);
        for (int i = 0; i < 3; i++) {
            String fileName = "C:\\Users\\xiongwenjun\\Desktop\\file/" + "name" + i;
            FileWriter fileWriter = new FileWriter(fileName);
            File file = new File(fileName);

            fileWriter.write(i); // 也会触发监听
            fileWriter.flush(); // 也会触发监听
            System.out.println(fileName + " lastModified 1  " + file.lastModified());


            fileWriter.close(); // 也会触发监听
        }
    }

    public static void watchDir(String dir) {
        Path path = Paths.get(dir);
        // WindowsWatchService  linux上是class sun.nio.fs.LinuxWatchService，用到了inotify系统调用
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.OVERFLOW);
            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("create..." + watchEvent.context() + ":  " + System.currentTimeMillis());
                    } else if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.println("modify..." + watchEvent.context() + ":  " + System.currentTimeMillis());
                    } else if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        System.out.println("delete..." + watchEvent.context() + ":  " + System.currentTimeMillis());
                    } else if (watchEvent.kind() == StandardWatchEventKinds.OVERFLOW) {
                        System.out.println("overflow..." + watchEvent.context() + ":  " + System.currentTimeMillis());
                    }
                }
                if (!key.reset()) {
                    System.out.println("reset false");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
