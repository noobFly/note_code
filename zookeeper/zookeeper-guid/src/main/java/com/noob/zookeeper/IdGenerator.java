package com.noob.zookeeper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.noob.util.NoCreator;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import com.noob.zookeeper.client.ZkConnectFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by noob on 2017/4/6.
 */
@Slf4j
public class IdGenerator {

    private final String domain = "/generator"; //域
    private final String node_tip = "_seq_";// 节点路径校验
    private String root = null; // 节点的根路径
    private String prefix = null;
    private String nodePath = null; // 根据业务可变的节点
    private boolean split = false; // 返回的值是否需要带入参前缀
    private ZooKeeper zk;
    private final DateFormat sdf = new SimpleDateFormat("yyMMddHH");
    private String slant = "/";

    private IdGenerator(String topic, String prefix, boolean split) {

        this.root = slant(topic).concat(domain);
        this.split = split;
        this.prefix = trimEmptyStr(prefix);
        this.nodePath = this.prefix.concat(sdf.format(Calendar.getInstance().getTime()));

        try {
            zk = new ZkConnectFactory().init(root);
        } catch (Exception e) {
            // do nothing
        }

    }

    public static IdGenerator newInstance(String topic, String prefix, boolean split) {
        return new IdGenerator(topic, prefix, split);
    }

    public String generator() {
        String id = null;

        if (zk != null) id = zkGenerator();
        if (id == null || id.trim().length() == 0) id = localGenerator();
        return id;
    }

    private String localGenerator() {
        return NoCreator.generateNumber("", "yyMMddHHmmssSSS");
    }

    private String zkGenerator() {
        String id = null;
        if (nodePath.indexOf(node_tip) < 0) {
            try {
                String currentLockNode = zk.create(root + slant + node_tip + nodePath,
                        new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                log.info("node：【{}】 is created.", currentLockNode);
                id = split ? currentLockNode.split(prefix)[1] : currentLockNode.split(node_tip)[1];

            } catch (Exception e) {
                log.info("zkGenerator exception", e);

            } finally {
                try {
                    zk.close();
                } catch (InterruptedException e) {
                    log.error("close zk exception", e);
                }
            }
        }
        return id;
    }

    public String slant(String path) {

        return path.startsWith(slant) ? path : slant.concat(path);
    }

    public String trimEmptyStr(String str) {
        return str == null ? "" : str.trim();
    }
}
