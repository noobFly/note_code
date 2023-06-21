package com.noob.workflow;


import java.util.List;
import java.util.Optional;

/**
 * 流程执行信息Service接口
 */
public interface IWorkflowProcessService {

    List<WorkflowProcess> list(Integer bizType, String name, Long opUserId, String type);

    WorkflowProcess getById(Long processId);

    void updateById(WorkflowProcess process);

    void add(WorkflowProcess process);

    // orderDesc("applyTime").limit(1). 可能多次回退,但只取最近一次！
    WorkflowProcess getUnique(Long flowId, String backProcessKey);
}
