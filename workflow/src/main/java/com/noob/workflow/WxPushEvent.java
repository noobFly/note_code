package com.noob.workflow;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

public class WxPushEvent extends ApplicationEvent {

    @Getter
    @Setter
    private Long pushId;
    @Getter
    @Setter
    private boolean approveSuccess;
    /**
     * @link WorkflowConstants.BizEnum
     */
    @Setter
    @Getter
    private Integer bizType;

    public WxPushEvent(Long flowId, Long pushId, boolean approveSuccess) {
        super(flowId);
        this.pushId = pushId;
        this.approveSuccess = approveSuccess;
    }
}
