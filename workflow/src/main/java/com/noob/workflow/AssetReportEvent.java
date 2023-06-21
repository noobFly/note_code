package com.noob.workflow;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

public class AssetReportEvent extends ApplicationEvent {
    @Getter
    @Setter
    private Long reportId;
    @Getter
    @Setter
    private boolean approveSuccess;
    /**
     * @link WorkflowConstants.BizEnum
     */
    @Setter
    @Getter
    private Integer bizType;

    public AssetReportEvent(Long flowId, Long reportId, boolean approveSuccess, Integer bizType) {
        super(flowId);
        this.reportId = reportId;
        this.approveSuccess = approveSuccess;
        this.bizType = bizType;
    }


}
