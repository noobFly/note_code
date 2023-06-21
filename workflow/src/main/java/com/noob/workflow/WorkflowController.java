package com.noob.workflow;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 流程审批相关
 */
@RequestMapping("/workflow")
@RestController
@Slf4j
@Validated
public class WorkflowController {

    @Resource
    WorkflowHandler workflowHandler;


    /**
     * 工作流结构信息
     */
    @GetMapping("/type/list")
    public WorkflowConstants.BizEnum[] typeList() {
        return WorkflowConstants.BizEnum.values();
    }


    /**
     * 审批  approveStatus状态： 1 审批通过 2 审批失败 3 回退
     *
     * @param processId
     * @param approveMsg
     * @param approveStatus
     * @param backProcessKey
     */
    @GetMapping("/approve")
    public void approve(@RequestParam Long processId,
                        @RequestParam String approveMsg,
                        @RequestParam Integer approveStatus, String backProcessKey) {
        if (!WorkflowConstants.ApproveStatus.DONE_LIST.contains(approveStatus)) {
            throw new RuntimeException("审批状态无效");
        }
        workflowHandler.approve(processId, approveMsg, approveStatus, backProcessKey);
    }


    /**
     * 流程列表 type: todo 待办 done 已办
     *
     * @param bizType
     * @param listType
     * @param assetName
     * @return
     */
    @GetMapping("/task/list")
    public List<WorkflowProcess> todoList(@RequestParam Integer bizType, String listType, String assetName) {

        SysUser sysUser = null;//当前登录用户  TODO
        List<WorkflowProcess> list = workflowHandler.list(bizType, assetName, sysUser, listType);
        if (CollectionUtils.isNotEmpty(list)) {

            list.forEach(t -> {
                t.setWorkflowApplyUserName(getNickName(t.getWorkflowApplyUser()));
                t.setAuditUserName(getNickName(t.getAuditUser()));
            });

        }
        return list;
    }

    private String getNickName(Long userId) {
        //TODO 获取用户中文名
        return null;
    }
}
