package com.noob.workflow;

import com.google.common.collect.Maps;
import com.noob.json.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
@Component
public class WorkflowHandler implements ApplicationContextAware {

    @Resource
    IWorkflowInstanceService instanceService;
    @Resource
    IWorkflowProcessService processService;

    ApplicationContext applicationContext;

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long processId, String approveMsg, Integer approveStatus, String backProcessKey) {
        log.info("流程过程审批开始. processId:{}, approveMsg:{}, approveStatus:{}, backProcessKey:{}", processId, approveMsg, approveMsg, backProcessKey);

        WorkflowInstance flow = null;
        switch (approveStatus) {
            case WorkflowConstants.ApproveStatus.SUCCESS:
                flow = approveSuccess(processId, approveMsg);
                break;
            case WorkflowConstants.ApproveStatus.FAIL:
                flow = approveFail(processId, approveMsg);
                break;
            case WorkflowConstants.ApproveStatus.BACK:
                flow = approveBack(processId, approveMsg, backProcessKey);
                break;
            default:
                break;
        }

        if (flow != null && WorkflowConstants.ApproveStatus.isFinal(flow.getApproveStatus())) {
            applicationContext.publishEvent(event(flow));


        }

    }
    private ApplicationEvent event(WorkflowInstance flow) {
        Integer flowApproveStatus = flow.getApproveStatus();
        Map<String, Object> bizInfo = JSON.parseObject(flow.getBizInfo());
        Integer bizType = (Integer) bizInfo.get("bizType");
        Long bizId = (Long) bizInfo.get("bizId");
        // 根据不同的业务创建不同的Event; 处理类实现ApplicationListener接口处理不同的业务！！TODO
        if (WorkflowConstants.BizEnum.isReport(bizType)) {
            return new AssetReportEvent(flow.getId(), bizId, WorkflowConstants.ApproveStatus.SUCCESS == flowApproveStatus, bizType);
        } else if (WorkflowConstants.BizEnum.WX_PUSH.getType() == bizType) {
            return new WxPushEvent(flow.getId(), bizId, WorkflowConstants.ApproveStatus.SUCCESS == flowApproveStatus);
        }
        throw new RuntimeException("不确定的业务类型！");
    }


    // 开启审批流程
    @Transactional(rollbackFor = Exception.class)
    public void apply(SysUser applyUser, Long bizId, String bizName, WorkflowConstants.BizEnum bizEnum) {

        Date now = new Date();
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setApplyTime(now);
        workflowInstance.setApplyUser(applyUser.getUserId());
        workflowInstance.setApproveStatus(WorkflowConstants.ApproveStatus.DOING);
        Map<String, Object> bizInfo = Maps.newHashMap();
        bizInfo.put("bizId", bizId);
        bizInfo.put("bizType", bizEnum.getType());

        workflowInstance.setBizInfo(JSON.toJSONString(bizInfo));
        workflowInstance.setFlowInfo(JSON.toJSONString(bizEnum.getProcessNode()));
        workflowInstance.setWorkflowName(bizName + "_" + bizEnum.getDesc());
        workflowInstance.setCreateBy(applyUser.getUserName());
        workflowInstance.setCreateTime(now);

        log.info("创建审批流程 {}", JSON.toJSONString(workflowInstance));

        instanceService.add(workflowInstance);

        WorkflowConstants.Node firstNode = bizEnum.getProcessNode();
        Long workflowId = workflowInstance.getId();
        initAndSaveProcess(workflowId, firstNode, WorkflowConstants.ApproveStatus.SUCCESS, null, false);
        initAndSaveProcess(workflowId, firstNode.getNext(), WorkflowConstants.ApproveStatus.DOING, null, true);
    }

    private WorkflowInstance approveSuccess(Long processId, String approveMsg) {
        return execute(processId, approveMsg, WorkflowConstants.ApproveStatus.SUCCESS, (flow, process) -> {
            WorkflowConstants.Node processNodeLink = JSON.parseObject(flow.getFlowInfo(), WorkflowConstants.Node.class);
            WorkflowConstants.Node currentNode = processNodeLink.getMap().get(process.getProcessKey());
            WorkflowConstants.Node nextNode = currentNode.getNext();
            if (nextNode == null) {
                Date now = new Date();
                flow.setEndTime(now);
                flow.setUpdateTime(now);
                flow.setApproveStatus(WorkflowConstants.ApproveStatus.SUCCESS);
                instanceService.updateById(flow);
            } else {
                initAndSaveProcess(flow.getId(), nextNode, WorkflowConstants.ApproveStatus.DOING, null, false);
            }
            return flow;
        });
    }


    private WorkflowInstance approveFail(Long processId, String approveMsg) {
        return execute(processId, approveMsg, WorkflowConstants.ApproveStatus.FAIL, (flow, process) -> {
            Date now = new Date();
            flow.setEndTime(now);
            flow.setUpdateTime(now);
            flow.setApproveStatus(WorkflowConstants.ApproveStatus.FAIL);
            instanceService.updateById(flow);
            return flow;
        });
    }


    private WorkflowInstance approveBack(Long processId, String approveMsg, String backProcessKey) {
        return execute(processId, approveMsg, WorkflowConstants.ApproveStatus.FAIL, (flow, process) -> {

            WorkflowConstants.Node processNodeLink = JSON.parseObject(flow.getFlowInfo(), WorkflowConstants.Node.class);
            WorkflowConstants.Node node = processNodeLink.getMap().get(process.getProcessKey());
            do {
                node = node.getPre();
                if (node != null && node.getKey().equals(backProcessKey)) {
                    break;
                }
            } while (node != null);

            if (node == null) {
                throw new RuntimeException("回退的流程节点不存在！");
            }

            WorkflowProcess backProcess = processService.getUnique(flow.getId(), backProcessKey);
            ;

            if (backProcess == null) {
                throw new RuntimeException("回退的流程节点执行实例不存在！");
            }

            initAndSaveProcess(flow.getId(), node, WorkflowConstants.ApproveStatus.DOING, getUser(backProcess.getAuditUser()), false);
            return flow;
        });


    }

    private SysUser getUser(Long userId) {
        return null; // 获取用户信息
    }


    public WorkflowInstance execute(Long processId, String approveMsg, Integer approveStatus, BiFunction<WorkflowInstance, WorkflowProcess, WorkflowInstance> executeFunc) {
        WorkflowProcess process = processService.getById(processId);
        if (process == null) {
            new RuntimeException("找不到审批流程执行实例" + processId);
        }
        if (WorkflowConstants.ApproveStatus.isFinal(process.getAuditStatus())) {
            throw new RuntimeException("该审批节点已被处理" + processId);
        }

        Long flowId = process.getFlowId();
        WorkflowInstance flow = instanceService.getById(flowId);
        if (flow == null) {
            new RuntimeException(processId + "找不到审批流" + flowId);
        }

        SysUser loginUser = getCurrentLoginUser();
        // 判定当前用户是否有审批权限
        if (!(loginUser.isAdmin() || loginUser.getUserId().equals(process.getAuditUser()))) {
            throw new RuntimeException("当前用户没有审批权限！");
        }

        Date now = new Date();

        process.setAuditRemark(approveMsg);
        process.setAuditTime(now);
        process.setAuditStatus(approveStatus);

        process.setRealAuditUser(loginUser.getUserId());
        process.setUpdateTime(now);
        processService.updateById(process);
        return executeFunc.apply(flow, process);


    }

    private SysUser getCurrentLoginUser() {
        return null; //TODO 拿当前登录人
    }

    private WorkflowProcess initAndSaveProcess(Long workflowId, WorkflowConstants.Node nextNode, int approveStatus, SysUser approveUser, boolean delay) {
        WorkflowProcess process = null;
        if (nextNode != null) {
            Date now = delay ? DateUtils.addSeconds(new Date(), 1) : new Date(); // 刚申请时创建的2个workflowProcess的申请时间不能一致，后续要通过申请时间来排序
            process = new WorkflowProcess();

            String roleKey = nextNode.getApproveRole(); //TODO  对于审批人， 可以扩展： 提交人、上一节点处理人、提交人部门领导...

            if (approveUser == null) {
                List<SysUser> userList = getUserByRole(roleKey);
                approveUser = CollectionUtils.isNotEmpty(userList) ? userList.get(0) : null;
                /** TODO TODO TODO
                 * 1、  对于按规则其实可以匹配多个人时， 可以以竞选的方式由第一个点击待办的具体人竞得审批！  这里复杂的是： 如果给多个人展示同一个单子，竞选后又只能给但一人展示。（作废流程process）
                 * 2、  支持会签，配置会签比例P 超过P则流转下一节点，否则回退！
                 */
            }
            if (approveUser == null) {
                throw new RuntimeException(nextNode.getName() + "节点处理人为空！");
            }
            process.setAuditRole(roleKey);
            process.setAuditUser(approveUser.getUserId());
            process.setProcessKey(nextNode.getKey());
            process.setProcessName(nextNode.getName());
            process.setFlowId(workflowId);
            process.setApplyTime(now);
            process.setCreateTime(now);
            process.setAuditStatus(approveStatus);
            processService.add(process);
        }

        return process;
    }

    private List<SysUser> getUserByRole(String roleKey) {
        return null; // TODO 拿角色所属的用户
    }

    public List<WorkflowProcess> list(Integer bizType, String assetName, SysUser loginUser, String listType) {
        return processService.list(bizType, assetName, loginUser == null ? null : loginUser.getUserId(), listType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
