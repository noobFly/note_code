package com.noob.workflow;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 流程执行信息Service业务层处理
 *
 * @author lfh
 * @date 2023-05-24
 */
@Service
public class WorkflowProcessServiceImpl implements IWorkflowProcessService {
    @Resource
    WorkflowProcessMapper workflowProcessMapper;

    @Override
    public List<WorkflowProcess> list(Integer bizType, String name, Long opUserId, String type) {
        List<Integer> statusList = null;
        if ("todo".equals(type)) {
            statusList = WorkflowConstants.ApproveStatus.DOING_LIST;
        } else if ("done".equals(type)) {
            statusList = WorkflowConstants.ApproveStatus.DONE_LIST;
        }

        return workflowProcessMapper.selectByParam(bizType, name, opUserId, statusList);
    }

    @Override
    public WorkflowProcess getById(Long processId) {
        //TODO
        return null;
    }

    @Override
    public void updateById(WorkflowProcess process) {
        //TODO

    }

    @Override
    public void add(WorkflowProcess process) {
        //TODO

    }

    @Override
    public WorkflowProcess getUnique(Long flowId, String backProcessKey) {
        //TODO

        return null;
    }
}
