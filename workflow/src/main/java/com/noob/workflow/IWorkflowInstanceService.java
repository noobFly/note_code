package com.noob.workflow;


/**
 * 流程Service接口
 */
public interface IWorkflowInstanceService {

    void add(WorkflowInstance workflowInstance);

    void updateById(WorkflowInstance flow);

    WorkflowInstance getById(Long flowId);
}
