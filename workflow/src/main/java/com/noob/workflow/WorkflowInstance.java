package com.noob.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 流程对象 workflow_instance
 *
 * @author lfh
 * @date 2023-05-24
 */
@Data
public class WorkflowInstance {

    private Long id;


    /**
     * 业务相关联的信息JSON
     * bizType 业务类型 1 资产报告上架审批、 2 资产报告下架审批 3、资产报告企业微信推送
     */
    private String bizInfo;

    /**
     * 流程名称
     */
    private String workflowName;

    /**
     * 流程图信息
     */
    private String flowInfo;

    /**
     * 申请时间
     */

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date applyTime;

    /**
     * 申请人
     */
    private Long applyUser;


    /**
     * 审批状态  0 处理中 1 处理成功 2 处理失败
     */
    private Integer approveStatus;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


}
