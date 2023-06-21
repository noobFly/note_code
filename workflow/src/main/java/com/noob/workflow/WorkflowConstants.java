package com.noob.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public interface WorkflowConstants {

    // 审批状态
    interface ApproveStatus {
        // 处理中
        int DOING = 0;
        // 审批通过
        int SUCCESS = 1;
        // 审批失败
        int FAIL = 2;
        // 回退
        int BACK = 3;

        //
        static boolean isFinal(int auditStatus) {
            return auditStatus != DOING;
        }

        List<Integer> DOING_LIST = Lists.newArrayList(DOING);
        List<Integer> DONE_LIST = Lists.newArrayList(SUCCESS, FAIL, BACK);


    }


    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum BizEnum {
        REPORT_PUBLISH(1, "报告上架", buildNodeLink(
                new Node("apply", "经办发起", null),
                new Node("apply_manager", "经办部门负责人审批", "apply_manager"),
                new Node("trade", "交易服务部审批", "trade"),
                new Node("trade_manager", "交易服务部分管领导审批", "trade_manager"),
                new Node("president", "总经理审批", "president"))),
        REPORT_RELEASE(2, "报告下架", REPORT_PUBLISH.processNode),
        WX_PUSH(3, "企业微信推送", buildNodeLink(
                new Node("apply", "经办发起", null),
                new Node("apply_manager", "经办部门负责人审批", "apply_manager"),
                new Node("trade", "交易服务部审批", "trade"),
                new Node("trade_manager", "交易服务部操作岗", "trade_manager")));

        private int type;
        private String desc;
        private Node processNode;

        public static boolean isReport(int bizType) {
            return BizEnum.REPORT_PUBLISH.getType() == bizType || BizEnum.REPORT_RELEASE.getType() == bizType;
        }

        static Node buildNodeLink(Node... nodes) {
            Map<String, Node> map = Maps.newHashMap();
            Node pre = null;

            for (Node node : nodes) {
                map.put(node.key, node);
                node.map = map;
                if (pre != null) {
                    pre.next = node;
                    node.pre = node;
                } else {
                    pre = node;
                }

            }

            return nodes[0];

        }
    }

    @Getter
    class Node {
        // 节点key
        String key;
        // 节点名称
        String name;
        // 指定的审批角色
        String approveRole;
        // 指定的审批特权(提交人、提交人上级、提交人部门领导、提交人) 可扩展 TODO
        String approveTag;
        Node next;
        Node pre;
        Map<String, Node> map;

        public Node(String key, String name, String approveRole) {
            this.key = key;
            this.name = name;
            this.approveRole = approveRole;

        }


    }


}
