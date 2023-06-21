package com.noob.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import lombok.Data;

import java.util.Date;

/**
 * 流程执行信息对象 workflow_process
 *
 * @author lfh
 * @date 2023-05-24
 */
@Data
public class WorkflowProcess {

    /**
     * 主键
     */

    private Long id;

    /**
     * 流程id
     */
    @JsonFormat(shape = Shape.STRING)
    private Long flowId;

    /**
     * 节点名称
     */
    private String processName;

    /**
     * 节点key
     */
    private String processKey;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date applyTime;

    /**
     * 审批人
     */
    @JsonFormat(shape = Shape.STRING)
    private Long auditUser;


    private Long realAuditUser;
    /**
     * 审批人角色
     */
    private String auditRole;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

    /**
     * 审批状态  0 处理中 1 审批通过 2 审批失败 3 回退
     */
    private Integer auditStatus;

    /**
     * 回退指定节点
     */
    private String backProcess;

    /**
     * 审批意见
     */
    private String auditRemark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


    /**
     * 流程名称
     */
    private String workflowName;

    /**
     * 流程申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date workflowApplyTime;


    //--------------------------@Transient---------------------------
    /**
     * 流程申请人
     */
    private Long workflowApplyUser;

    private String workflowApplyUserName;

    /**
     * 审批人
     */
    private String auditUserName;

    /**
     * 业务id
     */
    private Long bizId;
    /**
     * 业务类型
     */
    private Integer bizType;
    /**
     * 业务实例对象
     */
    private Object bizEntry;


}
